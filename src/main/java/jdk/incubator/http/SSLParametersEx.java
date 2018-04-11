/*
 * Written by Stefan Zobel and released to the
 * public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package jdk.incubator.http;

import java.lang.reflect.Method;
import java.security.AlgorithmConstraints;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.SNIMatcher;
import javax.net.ssl.SSLParameters;

import jdk.incubator.http.internal.common.Utils;

import javax.net.ssl.SNIServerName;

/**
 * Wrapper for a Java 8 {@link SSLParameters} delegate. Provides the new Java 9
 * methods in addition.
 */
final class SSLParametersEx {

    private final SSLParameters delegate;

    private int maximumPacketSize;
    private String applicationProtocols[];
    private boolean enableRetransmissions;

    public SSLParametersEx(SSLParameters delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    public SSLParameters getDelegate() {
        return delegate;
    }

    /**
     * @since 9
     */
    public void setEnableRetransmissions(boolean enableRetransmissions) {
        this.enableRetransmissions = enableRetransmissions;
        if (!IS_JAVA8) {
            setEnableRetransmissions(delegate, enableRetransmissions);
        }
    }

    /**
     * @since 9
     */
    public boolean getEnableRetransmissions() {
        if (!IS_JAVA8) {
            return getEnableRetransmissions(delegate);
        }
        return enableRetransmissions;
    }

    /**
     * @since 9
     */
    public void setMaximumPacketSize(int maximumPacketSize) {
        if (maximumPacketSize < 0) {
            throw new IllegalArgumentException("The maximum packet size cannot be negative");
        } else {
            this.maximumPacketSize = maximumPacketSize;
        }
        if (!IS_JAVA8) {
            setMaximumPacketSize(delegate, maximumPacketSize);
        }
    }

    /**
     * @since 9
     */
    public int getMaximumPacketSize() {
        if (!IS_JAVA8) {
            return getMaximumPacketSize(delegate);
        }
        return maximumPacketSize;
    }

    /**
     * @since 9
     */
    public String[] getApplicationProtocols() {
        if (!IS_JAVA8) {
            return getApplicationProtocols(delegate);
        }
        return (applicationProtocols != null) ? (String[]) applicationProtocols.clone() : new String[] {};
    }

    /**
     * @since 9
     */
    public void setApplicationProtocols(String[] protocols) {
        if (protocols == null) {
            throw new IllegalArgumentException("protocols was null");
        }
        String[] tempProtocols = protocols.clone();
        for (String p : tempProtocols) {
            if (p == null || p.equals("")) {
                throw new IllegalArgumentException("An element of protocols was null/empty");
            }
        }
        applicationProtocols = tempProtocols;
        if (!IS_JAVA8) {
            setApplicationProtocols(delegate, tempProtocols);
        }
    }

    public String[] getCipherSuites() {
        return delegate.getCipherSuites();
    }

    public void setCipherSuites(String[] cipherSuites) {
        delegate.setCipherSuites(cipherSuites);
    }

    public String[] getProtocols() {
        return delegate.getProtocols();
    }

    public void setProtocols(String[] protocols) {
        delegate.setProtocols(protocols);
    }

    public boolean getWantClientAuth() {
        return delegate.getWantClientAuth();
    }

    public void setWantClientAuth(boolean wantClientAuth) {
        delegate.setWantClientAuth(wantClientAuth);
    }

    public boolean getNeedClientAuth() {
        return delegate.getNeedClientAuth();
    }

    public void setNeedClientAuth(boolean needClientAuth) {
        delegate.setNeedClientAuth(needClientAuth);
    }

    /**
     * @since 1.7
     */
    public AlgorithmConstraints getAlgorithmConstraints() {
        return delegate.getAlgorithmConstraints();
    }

    /**
     * @since 1.7
     */
    public void setAlgorithmConstraints(AlgorithmConstraints constraints) {
        delegate.setAlgorithmConstraints(constraints);
    }

    /**
     * @since 1.7
     */
    public String getEndpointIdentificationAlgorithm() {
        return delegate.getEndpointIdentificationAlgorithm();
    }

    /**
     * @since 1.7
     */
    public void setEndpointIdentificationAlgorithm(String algorithm) {
        delegate.setEndpointIdentificationAlgorithm(algorithm);
    }

    /**
     * @since 1.8
     */
    public final void setServerNames(List<SNIServerName> serverNames) {
        delegate.setServerNames(serverNames);
    }

    /**
     * @since 1.8
     */
    public final List<SNIServerName> getServerNames() {
        return delegate.getServerNames();
    }

    /**
     * @since 1.8
     */
    public final void setSNIMatchers(Collection<SNIMatcher> matchers) {
        delegate.setSNIMatchers(matchers);
    }

    /**
     * @since 1.8
     */
    public final Collection<SNIMatcher> getSNIMatchers() {
        return delegate.getSNIMatchers();
    }

    /**
     * @since 1.8
     */
    public final void setUseCipherSuitesOrder(boolean honorOrder) {
        delegate.setUseCipherSuitesOrder(honorOrder);
    }

    /**
     * @since 1.8
     */
    public final boolean getUseCipherSuitesOrder() {
        return delegate.getUseCipherSuitesOrder();
    }

    private static void setEnableRetransmissions(SSLParameters params, boolean enableRetransmissions) {
        try {
            ENABLE_RETRANS_SET.invoke(params, enableRetransmissions);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static boolean getEnableRetransmissions(SSLParameters params) {
        try {
            return (boolean) ENABLE_RETRANS_GET.invoke(params);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static void setMaximumPacketSize(SSLParameters params, int maximumPacketSize) {
        try {
            MAX_PACKSIZE_SET.invoke(params, maximumPacketSize);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static int getMaximumPacketSize(SSLParameters params) {
        try {
            return (int) MAX_PACKSIZE_GET.invoke(params);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static String[] getApplicationProtocols(SSLParameters params) {
        try {
            return (String[]) APP_PROTOCOLS_GET.invoke(params);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static void setApplicationProtocols(SSLParameters params, String[] protocols) {
        try {
            APP_PROTOCOLS_SET.invoke(params, new Object[] { protocols });
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static final boolean IS_JAVA8 = Utils.isJava8();
    private static final Method ENABLE_RETRANS_SET;
    private static final Method ENABLE_RETRANS_GET;
    private static final Method MAX_PACKSIZE_SET;
    private static final Method MAX_PACKSIZE_GET;
    private static final Method APP_PROTOCOLS_GET;
    private static final Method APP_PROTOCOLS_SET;
    static {
        try {
            if (!IS_JAVA8) {
                // assume it's Java 9 or higher
                ENABLE_RETRANS_SET = SSLParameters.class.getDeclaredMethod("setEnableRetransmissions", boolean.class);
                ENABLE_RETRANS_SET.setAccessible(true);
                ENABLE_RETRANS_GET = SSLParameters.class.getDeclaredMethod("getEnableRetransmissions");
                ENABLE_RETRANS_GET.setAccessible(true);
                MAX_PACKSIZE_SET = SSLParameters.class.getDeclaredMethod("setMaximumPacketSize", int.class);
                MAX_PACKSIZE_SET.setAccessible(true);
                MAX_PACKSIZE_GET = SSLParameters.class.getDeclaredMethod("getMaximumPacketSize");
                MAX_PACKSIZE_GET.setAccessible(true);
                APP_PROTOCOLS_GET = SSLParameters.class.getDeclaredMethod("getApplicationProtocols");
                APP_PROTOCOLS_GET.setAccessible(true);
                APP_PROTOCOLS_SET = SSLParameters.class.getDeclaredMethod("setApplicationProtocols", String[].class);
                APP_PROTOCOLS_SET.setAccessible(true);
            } else {
                // Java 8
                ENABLE_RETRANS_SET = null;
                ENABLE_RETRANS_GET = null;
                MAX_PACKSIZE_SET = null;
                MAX_PACKSIZE_GET = null;
                APP_PROTOCOLS_GET = null;
                APP_PROTOCOLS_SET = null;
            }
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
