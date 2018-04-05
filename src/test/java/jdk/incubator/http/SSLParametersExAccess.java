package jdk.incubator.http;

import javax.net.ssl.SSLParameters;

/**
 * Helper to make {@code SSLParametersEx} accessible to tests that live in other
 * packages.
 */
public final class SSLParametersExAccess {

    public static SSLParameters init(SSLParameters sslParams, String[] applicationProtocols) {
        SSLParametersEx ex = new SSLParametersEx(sslParams);
        ex.setApplicationProtocols(applicationProtocols);
        return ex.getDelegate();
    }

    private SSLParametersExAccess() {
        throw new AssertionError();
    }
}
