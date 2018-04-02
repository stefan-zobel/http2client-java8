/*
 * Written by Stefan Zobel and released to the
 * public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package jdk.incubator.http.internal.common;

/**
 * Exception thrown from the undertow ALPN hack during SSL handshake failures.
 */
@SuppressWarnings("serial")
final class SSLHandshakeException extends RuntimeException {

    static final String NOT_HANDSHAKE_RECORD = "Initial SSL/TLS data is not a handshake record";
    static final String MULTIRECORD_HANDSHAKE = "Initial SSL/TLS handshake spans multiple records";

    public SSLHandshakeException(String message) {
        super(message);
    }
}
