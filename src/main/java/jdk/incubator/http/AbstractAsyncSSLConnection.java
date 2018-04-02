/*
 * Copyright (c) 2015, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package jdk.incubator.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLParameters;

import jdk.incubator.http.internal.common.SSLTube;
import jdk.incubator.http.internal.common.Log;
import jdk.incubator.http.internal.common.SSLEngineEx;
import jdk.incubator.http.internal.common.Utils;

import java9.util.Lists;
import java9.util.concurrent.CompletableFuture;

/**
 * Asynchronous version of SSLConnection.
 *
 * There are two concrete implementations of this class: AsyncSSLConnection
 * and AsyncSSLTunnelConnection.
 * This abstraction is useful when downgrading from HTTP/2 to HTTP/1.1 over
 * an SSL connection. See ExchangeImpl::get in the case where an ALPNException
 * is thrown.
 *
 * Note: An AsyncSSLConnection wraps a PlainHttpConnection, while an
 *       AsyncSSLTunnelConnection wraps a PlainTunnelingConnection.
 *       If both these wrapped classes where made to inherit from a
 *       common abstraction then it might be possible to merge
 *       AsyncSSLConnection and AsyncSSLTunnelConnection back into
 *       a single class - and simply use different factory methods to
 *       create different wrappees, but this is left up for further cleanup.
 */
abstract class AbstractAsyncSSLConnection extends HttpConnection
{
    private static final boolean IS_JAVA8 = isJava8();
    protected final SSLEngineEx engine;
    protected final String serverName;
    protected final SSLParametersEx sslParameters;

    AbstractAsyncSSLConnection(InetSocketAddress addr,
                               HttpClientImpl client,
                               String serverName,
                               String[] alpn) {
        super(addr, client);
        this.serverName = serverName;
        SSLContext context = client.theSSLContext();
        sslParameters = createSSLParameters(client, serverName, alpn);
        logParams(sslParameters);
        engine = createEngine(context, sslParameters);
    }

    abstract HttpConnection plainConnection();
    abstract SSLTube getConnectionFlow();

    final CompletableFuture<String> getALPN() {
        assert connected();
        return getConnectionFlow().getALPN();
    }

    final SSLEngineEx getEngine() { return engine; }

    private static SSLParametersEx createSSLParameters(HttpClientImpl client,
                                                       String serverName,
                                                       String[] alpn) {
        SSLParameters sslp = client.sslParameters();
        SSLParametersEx sslParameters = copyToSSLParametersEx(sslp);
        if (alpn != null) {
            Log.logSSL("AbstractAsyncSSLConnection: Setting application protocols: {0}",
                       Arrays.toString(alpn));
            sslParameters.setApplicationProtocols(alpn);
        } else {
            Log.logSSL("AbstractAsyncSSLConnection: no applications set!");
        }
        if (serverName != null) {
            sslParameters.setServerNames(Lists.of(new SNIHostName(serverName)));
        }
        return sslParameters;
    }

    private static SSLEngineEx createEngine(SSLContext context,
                                            SSLParametersEx sslParameters) {
        SSLEngineEx engine = new SSLEngineEx(context.createSSLEngine());
        engine.setUseClientMode(true);
        engine.setApplicationProtocols(sslParameters.getApplicationProtocols());
        engine.setSSLParameters(sslParameters.getDelegate());
        return engine;
    }

    private static SSLParametersEx copyToSSLParametersEx(SSLParameters p) {
        Objects.requireNonNull(p);
        SSLParametersEx p2 = new SSLParametersEx(new SSLParameters());
        p2.setAlgorithmConstraints(p.getAlgorithmConstraints());
        p2.setCipherSuites(p.getCipherSuites());
        // JDK 8 EXCL START
        if (!IS_JAVA8) {
            copyJava9SSLParameters(p, p2);
        }
        // JDK 8 EXCL END
        p2.setEndpointIdentificationAlgorithm(p.getEndpointIdentificationAlgorithm());
        p2.setNeedClientAuth(p.getNeedClientAuth());
        String[] protocols = p.getProtocols();
        if (protocols != null) {
            p2.setProtocols(protocols);
        }
        p2.setSNIMatchers(p.getSNIMatchers());
        p2.setServerNames(p.getServerNames());
        p2.setUseCipherSuitesOrder(p.getUseCipherSuitesOrder());
        p2.setWantClientAuth(p.getWantClientAuth());
        return p2;
    }

    private static void copyJava9SSLParameters(SSLParameters src, SSLParametersEx dst) {
       dst.setEnableRetransmissions(src.getEnableRetransmissions());
       dst.setMaximumPacketSize(src.getMaximumPacketSize());
    }

    @Override
    final boolean isSecure() {
        return true;
    }

    // Support for WebSocket/RawChannelImpl which unfortunately
    // still depends on synchronous read/writes.
    // It should be removed when RawChannelImpl moves to using asynchronous APIs.
    static final class SSLConnectionChannel extends DetachedConnectionChannel {
        final DetachedConnectionChannel delegate;
        final SSLDelegate sslDelegate;
        SSLConnectionChannel(DetachedConnectionChannel delegate, SSLDelegate sslDelegate) {
            this.delegate = delegate;
            this.sslDelegate = sslDelegate;
        }

        SocketChannel channel() {
            return delegate.channel();
        }

        @Override
        ByteBuffer read() throws IOException {
            SSLDelegate.WrapperResult r = sslDelegate.recvData(ByteBuffer.allocate(8192));
            // TODO: check for closure
            int n = r.result.bytesProduced();
            if (n > 0) {
                return r.buf;
            } else if (n == 0) {
                return Utils.EMPTY_BYTEBUFFER;
            } else {
                return null;
            }
        }
        @Override
        long write(ByteBuffer[] buffers, int start, int number) throws IOException {
            long l = SSLDelegate.countBytes(buffers, start, number);
            SSLDelegate.WrapperResult r = sslDelegate.sendData(buffers, start, number);
            if (r.result.getStatus() == SSLEngineResult.Status.CLOSED) {
                if (l > 0) {
                    throw new IOException("SSLHttpConnection closed");
                }
            }
            return l;
        }
        @Override
        public void shutdownInput() throws IOException {
            delegate.shutdownInput();
        }
        @Override
        public void shutdownOutput() throws IOException {
            delegate.shutdownOutput();
        }
        @Override
        public void close() {
            delegate.close();
        }
    }

    // Support for WebSocket/RawChannelImpl which unfortunately
    // still depends on synchronous read/writes.
    // It should be removed when RawChannelImpl moves to using asynchronous APIs.
    @Override
    DetachedConnectionChannel detachChannel() {
        assert client() != null;
        DetachedConnectionChannel detachedChannel = plainConnection().detachChannel();
        SSLDelegate sslDelegate = new SSLDelegate(engine,
                                                  detachedChannel.channel());
        return new SSLConnectionChannel(detachedChannel, sslDelegate);
    }

    private static void logParams(SSLParametersEx p) {
        if (!Log.ssl()) {
            return;
        }
        if (p == null) {
            Log.logSSL("SSLParameters: Null params");
            return;
        }
        final StringBuilder sb = new StringBuilder("SSLParameters:");
        final List<Object> params = new ArrayList<>();
        if (p.getCipherSuites() != null) {
            for (String cipher : p.getCipherSuites()) {
                sb.append("\n    cipher: {")
                        .append(params.size()).append("}");
                params.add(cipher);
            }
        }

        // SSLParameters.getApplicationProtocols() can't return null
        // JDK 8 EXCL START
        for (String approto : p.getApplicationProtocols()) {
            sb.append("\n    application protocol: {")
                    .append(params.size()).append("}");
            params.add(approto);
        }
        // JDK 8 EXCL END

        if (p.getProtocols() != null) {
            for (String protocol : p.getProtocols()) {
                sb.append("\n    protocol: {")
                        .append(params.size()).append("}");
                params.add(protocol);
            }
        }
        if (p.getServerNames() != null) {
            for (SNIServerName sname : p.getServerNames()) {
                sb.append("\n    server name: {")
                        .append(params.size()).append("}");
                params.add(sname.toString());
            }
        }
        sb.append('\n');
        Log.logSSL(sb.toString(), params.toArray());
    }

    private static boolean isJava8() {
        try {
            return "52.0".equals(System.getProperty("java.class.version"));
        } catch (Exception ignore) {
        }
        return false;
   }
}
