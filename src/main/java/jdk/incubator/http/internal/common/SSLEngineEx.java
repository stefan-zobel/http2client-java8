/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package jdk.incubator.http.internal.common;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

/**
 * SSLEngine wrapper that provides some super hacky ALPN support on JDK8.
 *
 * Even though this is a nasty hack that relies on JDK internals it is still
 * preferable to modifying the boot class path.
 *
 * It is expected to work with all JDK8 versions, however this cannot be
 * guaranteed if the SSL internals are changed in an incompatible way.
 *
 * @author Stuart Douglas
 */
public final class SSLEngineEx extends SSLEngine {

    private final SSLEngine delegate;
    // ALPN Java 8 hack specific variables
    private String selectedApplicationProtocol;
    private boolean unwrapHelloSeen = false;
    private boolean ourHelloSent = false;
    private ALPNHackServerBAOS alpnHackServerBAOS;
    private ALPNHackClientBAOS alpnHackClientBAOS;
    private List<String> applicationProtocols;
    private ByteBuffer bufferedWrapData;

    public SSLEngineEx(SSLEngine delegate) {
        this.delegate = delegate;
    }

    @Override
    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts, int offset, int length) throws SSLException {
        if (!IS_JAVA8) {
            return delegate.unwrap(src, dsts, offset, length);
        }

        if (!unwrapHelloSeen) {
            if (!delegate.getUseClientMode() && applicationProtocols != null) {
                try {
                    List<String> result = ALPNHackClientHelloExplorer.exploreClientHello(src.duplicate());
                    if (result != null) {
                        for (String protocol : applicationProtocols) {
                            if (result.contains(protocol)) {
                                selectedApplicationProtocol = protocol;
                                break;
                            }
                        }
                    }
                    unwrapHelloSeen = true;
                } catch (BufferUnderflowException e) {
                    return new SSLEngineResult(SSLEngineResult.Status.BUFFER_UNDERFLOW,
                            SSLEngineResult.HandshakeStatus.NEED_UNWRAP, 0, 0);
                }
            } else if (delegate.getUseClientMode() && alpnHackClientBAOS != null) {
                if (!src.hasRemaining()) {
                    return delegate.unwrap(src, dsts, offset, length);
                }
                try {
                    ByteBuffer dup = src.duplicate();
                    int type = dup.get();
                    int major = dup.get();
                    int minor = dup.get();
                    if (type == 22 && major == 3 && minor == 3) {
                        // we only care about TLS 1.2
                        // split up the records, there may be multiple
                        // when doing a fast session resume
                        List<ByteBuffer> records = ALPNHackServerHelloExplorer.extractRecords(src.duplicate());

                        // this will be the handshake record
                        ByteBuffer firstRecord = records.get(0);

                        AtomicReference<String> alpnResult = new AtomicReference<>();
                        ByteBuffer dupFirst = firstRecord.duplicate();
                        dupFirst.position(firstRecord.position() + 5);
                        ByteBuffer firstLessFraming = dupFirst.duplicate();

                        byte[] result = ALPNHackServerHelloExplorer.removeAlpnExtensionsFromServerHello(dupFirst,
                                alpnResult);
                        firstLessFraming.limit(dupFirst.position());
                        unwrapHelloSeen = true;
                        if (result != null) {
                            selectedApplicationProtocol = alpnResult.get();
                            int newFirstRecordLength = result.length + dupFirst.remaining();
                            byte[] newFirstRecord = new byte[newFirstRecordLength];
                            System.arraycopy(result, 0, newFirstRecord, 0, result.length);
                            dupFirst.get(newFirstRecord, result.length, dupFirst.remaining());
                            src.position(src.limit());

                            byte[] originalFirstRecord = new byte[firstLessFraming.remaining()];
                            firstLessFraming.get(originalFirstRecord);

                            ByteBuffer newData = ALPNHackServerHelloExplorer.createNewOutputRecords(newFirstRecord,
                                    records);
                            src.clear();
                            src.put(newData);
                            src.flip();
                            alpnHackClientBAOS.setReceivedServerHello(originalFirstRecord);
                        }
                    }
                } catch (BufferUnderflowException e) {
                    return new SSLEngineResult(SSLEngineResult.Status.BUFFER_UNDERFLOW,
                            SSLEngineResult.HandshakeStatus.NEED_UNWRAP, 0, 0);
                }
            }
        }

        SSLEngineResult result = delegate.unwrap(src, dsts, offset, length);
        if (!delegate.getUseClientMode() && selectedApplicationProtocol != null && alpnHackServerBAOS == null) {
            alpnHackServerBAOS = replaceServerByteOutput(delegate, selectedApplicationProtocol);
        }
        return result;
    }

    @Override
    public SSLEngineResult wrap(ByteBuffer[] srcs, int offset, int length, ByteBuffer dst) throws SSLException {
        if (!IS_JAVA8) {
            return delegate.wrap(srcs, offset, length, dst);
        }

        if (bufferedWrapData != null) {
            int prod = bufferedWrapData.remaining();
            dst.put(bufferedWrapData);
            bufferedWrapData = null;
            return new SSLEngineResult(SSLEngineResult.Status.OK, SSLEngineResult.HandshakeStatus.NEED_WRAP, 0, prod);
        }
        int pos = dst.position();
        int limit = dst.limit();
        SSLEngineResult result = delegate.wrap(srcs, offset, length, dst);
        if (!ourHelloSent && result.bytesProduced() > 0) {
            if (delegate.getUseClientMode() && applicationProtocols != null && !applicationProtocols.isEmpty()) {
                ourHelloSent = true;
                alpnHackClientBAOS = replaceClientByteOutput(delegate);
                ByteBuffer newBuf = dst.duplicate();
                newBuf.flip();
                byte[] data = new byte[newBuf.remaining()];
                newBuf.get(data);
                byte[] newData = ALPNHackClientHelloExplorer.rewriteClientHello(data, applicationProtocols);
                if (newData != null) {
                    byte[] clientHelloMesage = new byte[newData.length - 5];
                    System.arraycopy(newData, 5, clientHelloMesage, 0, clientHelloMesage.length);
                    alpnHackClientBAOS.setSentClientHello(clientHelloMesage);
                    dst.clear();
                    dst.put(newData);
                }
            } else if (!getUseClientMode()) {
                if (selectedApplicationProtocol != null && alpnHackServerBAOS != null) {
                    // this is the new server hello, it will be part of the first
                    // TLS plain-text record
                    byte[] newServerHello = alpnHackServerBAOS.getServerHello();
                    if (newServerHello != null) {
                        dst.flip();
                        List<ByteBuffer> records = ALPNHackServerHelloExplorer.extractRecords(dst);
                        ByteBuffer newData = ALPNHackServerHelloExplorer.createNewOutputRecords(newServerHello,
                                records);
                        dst.position(pos); // erase the data
                        dst.limit(limit);
                        if (newData.remaining() > dst.remaining()) {
                            int old = newData.limit();
                            newData.limit(newData.position() + dst.remaining());
                            result = new SSLEngineResult(result.getStatus(), result.getHandshakeStatus(),
                                    result.bytesConsumed(), newData.remaining());
                            dst.put(newData);
                            newData.limit(old);
                            bufferedWrapData = newData;
                        } else {
                            result = new SSLEngineResult(result.getStatus(), result.getHandshakeStatus(),
                                    result.bytesConsumed(), newData.remaining());
                            dst.put(newData);
                        }
                    }
                }
            }
        }
        if (result.bytesProduced() > 0) {
            ourHelloSent = true;
        }
        return result;
    }

    public void setApplicationProtocols(String[] protocols) {
        if (protocols != null) {
            this.applicationProtocols = Arrays.asList(Arrays.copyOf(protocols, protocols.length));
        }
    }

    public String[] getApplicationProtocols() {
        if (applicationProtocols == null) {
            return new String[] {};
        }
        return applicationProtocols.toArray(new String[] {});
    }

    private static ALPNHackServerBAOS replaceServerByteOutput(SSLEngine sslEngine, String selectedAlpnProtocol) {
        try {
            Object handshaker = HANDSHAKER.get(sslEngine);
            Object hash = HANDSHAKE_HASH.get(handshaker);
            ByteArrayOutputStream existing = (ByteArrayOutputStream) HANDSHAKE_HASH_DATA.get(hash);
            ALPNHackServerBAOS out = new ALPNHackServerBAOS(sslEngine, existing.toByteArray(), selectedAlpnProtocol);
            HANDSHAKE_HASH_DATA.set(hash, out);
            return out;
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static ALPNHackClientBAOS replaceClientByteOutput(SSLEngine sslEngine) {
        try {
            Object handshaker = HANDSHAKER.get(sslEngine);
            Object hash = HANDSHAKE_HASH.get(handshaker);
            ALPNHackClientBAOS out = new ALPNHackClientBAOS(sslEngine);
            HANDSHAKE_HASH_DATA.set(hash, out);
            return out;
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    static void regenerateHashes(SSLEngine sslEngineToHack, ByteArrayOutputStream data, byte[]... hashBytes) {
        // hack up the SSL engine internal state
        try {
            Object handshaker = HANDSHAKER.get(sslEngineToHack);
            Object hash = HANDSHAKE_HASH.get(handshaker);
            data.reset();
            Object protocolVersion = HANDSHAKER_PROTOCOL_VERSION.get(handshaker);
            HANDSHAKE_HASH_VERSION.set(hash, -1);
            HANDSHAKE_HASH_PROTOCOL_DETERMINED.invoke(hash, protocolVersion);
            MessageDigest digest = (MessageDigest) HANDSHAKE_HASH_FIN_MD.get(hash);
            if (digest != null) {
                digest.reset();
            } else {
                // this path can only be reached for a few tests when we run
                // on a Java 9 VM and we deliberately pretend that we run on
                // a Java 8 VM even though we know that it's not true 
            }
            for (byte[] bytes : hashBytes) {
                HANDSHAKE_HASH_UPDATE.invoke(hash, bytes, 0, bytes.length);
            }
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * @since 9
     */
    public String getApplicationProtocol() {
        if (IS_JAVA8) {
            return selectedApplicationProtocol;
        } else {
            return getApplicationProtocol(delegate);
        }
    }

    /**
     * @since 9
     */
    public String getHandshakeApplicationProtocol() {
        if (IS_JAVA8) {
            throw new UnsupportedOperationException();
        }
        return getHandshakeApplicationProtocol(delegate);
    }

    /**
     * @since 9
     */
    public void setHandshakeApplicationProtocolSelector(BiFunction<SSLEngine, List<String>, String> selector) {
        if (IS_JAVA8) {
            throw new UnsupportedOperationException();
        }
        setHandshakeApplicationProtocolSelector(delegate, selector);
    }

    /**
     * @since 9
     */
    public BiFunction<SSLEngine, List<String>, String> getHandshakeApplicationProtocolSelector() {
        if (IS_JAVA8) {
            throw new UnsupportedOperationException();
        }
        return getHandshakeApplicationProtocolSelector(delegate);
    }

    /**
     * @since 1.7
     */
    @Override
    public SSLSession getHandshakeSession() {
        return delegate.getHandshakeSession();
    }

    /**
     * @since 1.6
     */
    @Override
    public void setSSLParameters(SSLParameters params) {
        delegate.setSSLParameters(params);
    }

    /**
     * @since 1.6
     */
    @Override
    public SSLParameters getSSLParameters() {
        return delegate.getSSLParameters();
    }

    @Override
    public void beginHandshake() throws SSLException {
        delegate.beginHandshake();
    }

    @Override
    public void closeInbound() throws SSLException {
        delegate.closeInbound();
    }

    @Override
    public void closeOutbound() {
        delegate.closeOutbound();
    }

    @Override
    public Runnable getDelegatedTask() {
        return delegate.getDelegatedTask();
    }

    @Override
    public boolean getEnableSessionCreation() {
        return delegate.getEnableSessionCreation();
    }

    @Override
    public String[] getEnabledCipherSuites() {
        return delegate.getEnabledCipherSuites();
    }

    @Override
    public String[] getEnabledProtocols() {
        return delegate.getEnabledProtocols();
    }

    @Override
    public HandshakeStatus getHandshakeStatus() {
        return delegate.getHandshakeStatus();
    }

    @Override
    public boolean getNeedClientAuth() {
        return delegate.getNeedClientAuth();
    }

    @Override
    public SSLSession getSession() {
        return delegate.getSession();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    @Override
    public String[] getSupportedProtocols() {
        return delegate.getSupportedProtocols();
    }

    @Override
    public boolean getUseClientMode() {
        return delegate.getUseClientMode();
    }

    @Override
    public boolean getWantClientAuth() {
        return delegate.getWantClientAuth();
    }

    @Override
    public boolean isInboundDone() {
        return delegate.isInboundDone();
    }

    @Override
    public boolean isOutboundDone() {
        return delegate.isOutboundDone();
    }

    @Override
    public void setEnableSessionCreation(boolean flag) {
        delegate.setEnableSessionCreation(flag);
    }

    @Override
    public void setEnabledCipherSuites(String[] suites) {
        delegate.setEnabledCipherSuites(suites);
    }

    @Override
    public void setEnabledProtocols(String[] protocols) {
        delegate.setEnabledProtocols(protocols);
    }

    @Override
    public void setNeedClientAuth(boolean need) {
        delegate.setNeedClientAuth(need);
    }

    @Override
    public void setUseClientMode(boolean mode) {
        delegate.setUseClientMode(mode);
    }

    @Override
    public void setWantClientAuth(boolean want) {
        delegate.setWantClientAuth(want);
    }

    @Override
    public String getPeerHost() {
        return delegate.getPeerHost();
    }

    @Override
    public int getPeerPort() {
        return delegate.getPeerPort();
    }

    private static String getApplicationProtocol(SSLEngine engine) {
        try {
            return (String) ENGINE_APP_PROTOCOL.invoke(engine);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static String getHandshakeApplicationProtocol(SSLEngine engine) {
        try {
            return (String) HANDSHAKE_APP_PROTOCOL.invoke(engine);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static BiFunction<SSLEngine, List<String>, String> getHandshakeApplicationProtocolSelector(
            SSLEngine engine) {
        try {
            return (BiFunction<SSLEngine, List<String>, String>) HANDSHAKE_APP_PROTOCOL_SELECTOR_GET.invoke(engine);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static void setHandshakeApplicationProtocolSelector(SSLEngine engine,
            BiFunction<SSLEngine, List<String>, String> selector) {
        try {
            HANDSHAKE_APP_PROTOCOL_SELECTOR_SET.invoke(engine, selector);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static final boolean IS_JAVA8 = isJava8();
    // Java 8
    private static final Field HANDSHAKER;
    private static final Field HANDSHAKER_PROTOCOL_VERSION;
    private static final Field HANDSHAKE_HASH;
    private static final Field HANDSHAKE_HASH_VERSION;
    private static final Field HANDSHAKE_HASH_DATA;
    private static final Field HANDSHAKE_HASH_FIN_MD;
    private static final Method HANDSHAKE_HASH_UPDATE;
    private static final Method HANDSHAKE_HASH_PROTOCOL_DETERMINED;
    // Java 9 or higher
    private static final Method ENGINE_APP_PROTOCOL;
    private static final Method HANDSHAKE_APP_PROTOCOL;
    private static final Method HANDSHAKE_APP_PROTOCOL_SELECTOR_GET;
    private static final Method HANDSHAKE_APP_PROTOCOL_SELECTOR_SET;
    static {
        if (IS_JAVA8) {
            try {
                // Java 8
                Class<?> protVersionClass = Class.forName("sun.security.ssl.ProtocolVersion", true,
                        ClassLoader.getSystemClassLoader());
                Class<?> engineImplClass = Class.forName("sun.security.ssl.SSLEngineImpl", true,
                        ClassLoader.getSystemClassLoader());
                HANDSHAKER = engineImplClass.getDeclaredField("handshaker");
                HANDSHAKER.setAccessible(true);
                HANDSHAKE_HASH = HANDSHAKER.getType().getDeclaredField("handshakeHash");
                HANDSHAKE_HASH.setAccessible(true);
                HANDSHAKER_PROTOCOL_VERSION = HANDSHAKER.getType().getDeclaredField("protocolVersion");
                HANDSHAKER_PROTOCOL_VERSION.setAccessible(true);
                HANDSHAKE_HASH_VERSION = HANDSHAKE_HASH.getType().getDeclaredField("version");
                HANDSHAKE_HASH_VERSION.setAccessible(true);
                HANDSHAKE_HASH_UPDATE = HANDSHAKE_HASH.getType().getDeclaredMethod("update", byte[].class, int.class,
                        int.class);
                HANDSHAKE_HASH_UPDATE.setAccessible(true);
                HANDSHAKE_HASH_PROTOCOL_DETERMINED = HANDSHAKE_HASH.getType().getDeclaredMethod("protocolDetermined",
                        protVersionClass);
                HANDSHAKE_HASH_PROTOCOL_DETERMINED.setAccessible(true);
                HANDSHAKE_HASH_DATA = HANDSHAKE_HASH.getType().getDeclaredField("data");
                HANDSHAKE_HASH_DATA.setAccessible(true);
                HANDSHAKE_HASH_FIN_MD = HANDSHAKE_HASH.getType().getDeclaredField("finMD");
                HANDSHAKE_HASH_FIN_MD.setAccessible(true);
            } catch (Exception e) {
                throw new Error(e);
            }
            // not needed: Java 9 or higher
            ENGINE_APP_PROTOCOL = null;
            HANDSHAKE_APP_PROTOCOL = null;
            HANDSHAKE_APP_PROTOCOL_SELECTOR_GET = null;
            HANDSHAKE_APP_PROTOCOL_SELECTOR_SET = null;
        } else {
            try {
                // Java 9 or higher
                ENGINE_APP_PROTOCOL = SSLEngine.class.getDeclaredMethod("getApplicationProtocol");
                HANDSHAKE_APP_PROTOCOL = SSLEngine.class.getDeclaredMethod("getHandshakeApplicationProtocol");
                HANDSHAKE_APP_PROTOCOL_SELECTOR_GET = SSLEngine.class
                        .getDeclaredMethod("getHandshakeApplicationProtocolSelector");
                HANDSHAKE_APP_PROTOCOL_SELECTOR_SET = SSLEngine.class
                        .getDeclaredMethod("setHandshakeApplicationProtocolSelector", BiFunction.class);
            } catch (Exception e) {
                throw new Error(e);
            }
            // not needed: Java 8
            HANDSHAKER = null;
            HANDSHAKE_HASH = null;
            HANDSHAKE_HASH_PROTOCOL_DETERMINED = null;
            HANDSHAKE_HASH_VERSION = null;
            HANDSHAKE_HASH_UPDATE = null;
            HANDSHAKE_HASH_DATA = null;
            HANDSHAKE_HASH_FIN_MD = null;
            HANDSHAKER_PROTOCOL_VERSION = null;
        }
    }

    private static boolean isJava8() {
        try {
            return "52.0".equals(System.getProperty("java.class.version"));
        } catch (Exception ignore) {
        }
        return false;
    }
}
