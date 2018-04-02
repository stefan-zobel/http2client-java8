/*
 * Written by Stefan Zobel and released to the
 * public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.net;

/**
 * A compilation stub only - <b>must not</b> be included in the binary
 * distribution!
 */
public abstract class Authenticator {

    /**
     * Throws {@link AssertionError} always.
     */
    public Authenticator() {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 9
     */
    public final PasswordAuthentication requestPasswordAuthenticationInstance(String host, InetAddress addr, int port,
            String protocol, String prompt, String scheme, URL url, RequestorType reqType) {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 9
     */
    public static PasswordAuthentication requestPasswordAuthentication(Authenticator authenticator, String host,
            InetAddress addr, int port, String protocol, String prompt, String scheme, URL url, RequestorType reqType) {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 9
     */
    public static Authenticator getDefault() {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 1.5
     */
    public static PasswordAuthentication requestPasswordAuthentication(String host, InetAddress addr, int port,
            String protocol, String prompt, String scheme, URL url, RequestorType reqType) {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 1.5
     */
    protected final URL getRequestingURL() {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 1.5
     */
    protected final RequestorType getRequestorType() {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 1.4
     */
    public static PasswordAuthentication requestPasswordAuthentication(String host, InetAddress addr, int port,
            String protocol, String prompt, String scheme) {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 1.4
     */
    protected final String getRequestingHost() {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     */
    public static void setDefault(Authenticator a) {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     */
    public static PasswordAuthentication requestPasswordAuthentication(InetAddress addr, int port, String protocol,
            String prompt, String scheme) {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     */
    protected PasswordAuthentication getPasswordAuthentication() {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     */
    protected final InetAddress getRequestingSite() {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     */
    protected final int getRequestingPort() {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     */
    protected final String getRequestingProtocol() {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     */
    protected final String getRequestingPrompt() {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     */
    protected final String getRequestingScheme() {
        throw new AssertionError();
    }

    /**
     * The type of the entity requesting authentication.
     *
     * @since 1.5
     */
    public enum RequestorType {
        /**
         * Entity requesting authentication is a HTTP proxy server.
         */
        PROXY,
        /**
         * Entity requesting authentication is a HTTP origin server.
         */
        SERVER
    }
}
