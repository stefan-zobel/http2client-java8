/*
 * Written by Stefan Zobel and released to the
 * public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package javax.net.ssl;

import java.security.AlgorithmConstraints;
import java.util.Collection;
import java.util.List;

/**
 * A compilation stub only - <b>must not</b> be included in the binary
 * distribution!
 */
public final class SSLParameters {

    /**
     * Throws {@link AssertionError} always.
     */
    public SSLParameters() {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 9
     */
    public void setEnableRetransmissions(boolean enableRetransmissions) {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 9
     */
    public boolean getEnableRetransmissions() {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 9
     */
    public void setMaximumPacketSize(int maximumPacketSize) {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 9
     */
    public int getMaximumPacketSize() {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 9
     */
    public String[] getApplicationProtocols() {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 9
     */
    public void setApplicationProtocols(String[] protocols) {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     */
    public String[] getCipherSuites() {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     */
    public void setCipherSuites(String[] cipherSuites) {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     */
    public String[] getProtocols() {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     */
    public void setProtocols(String[] protocols) {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     */
    public boolean getWantClientAuth() {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     */
    public void setWantClientAuth(boolean wantClientAuth) {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     */
    public boolean getNeedClientAuth() {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     */
    public void setNeedClientAuth(boolean needClientAuth) {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 1.7
     */
    public AlgorithmConstraints getAlgorithmConstraints() {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 1.7
     */
    public void setAlgorithmConstraints(AlgorithmConstraints constraints) {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 1.7
     */
    public String getEndpointIdentificationAlgorithm() {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 1.7
     */
    public void setEndpointIdentificationAlgorithm(String algorithm) {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 1.8
     */
    public final void setServerNames(List<SNIServerName> serverNames) {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 1.8
     */
    public final List<SNIServerName> getServerNames() {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 1.8
     */
    public final void setSNIMatchers(Collection<SNIMatcher> matchers) {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 1.8
     */
    public final Collection<SNIMatcher> getSNIMatchers() {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 1.8
     */
    public final void setUseCipherSuitesOrder(boolean honorOrder) {
        throw new AssertionError();
    }

    /**
     * Throws {@link AssertionError} always.
     * 
     * @since 1.8
     */
    public final boolean getUseCipherSuitesOrder() {
        throw new AssertionError();
    }
}
