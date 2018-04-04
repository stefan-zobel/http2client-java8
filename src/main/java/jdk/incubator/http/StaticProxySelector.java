/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

import java9.util.Lists;

/**
 * A ProxySelector which uses the given proxy address for all HTTP and HTTPS
 * requests. If proxy is {@code null} then proxying is disabled.
 *
 * @since 9
 */
public final class StaticProxySelector extends ProxySelector {

   private static final List<Proxy> NO_PROXY_LIST = Lists.of(Proxy.NO_PROXY);
   final List<Proxy> list;

   StaticProxySelector(InetSocketAddress address){
       Proxy p;
       if (address == null) {
           p = Proxy.NO_PROXY;
       } else {
           p = new Proxy(Proxy.Type.HTTP, address);
       }
       list = Lists.of(p);
   }

   /**
    * Returns a ProxySelector which uses the given proxy address for all HTTP
    * and HTTPS requests. If proxy is {@code null} then proxying is disabled.
    *
    * @param proxyAddress
    *        The address of the proxy
    *
    * @return a ProxySelector
    *
    * @since 9
    */
   public static ProxySelector of(InetSocketAddress proxyAddress) {
       return new StaticProxySelector(proxyAddress);
   }

   @Override
   public void connectFailed(URI uri, SocketAddress sa, IOException e) {
       /* ignore */
   }

   @Override
   public synchronized List<Proxy> select(URI uri) {
       String scheme = uri.getScheme().toLowerCase();
       if (scheme.equals("http") || scheme.equals("https")) {
           return list;
       } else {
           return NO_PROXY_LIST;
       }
   }
}
