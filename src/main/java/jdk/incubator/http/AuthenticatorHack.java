/*
 * Written by Stefan Zobel and released to the
 * public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package jdk.incubator.http;

import java.lang.reflect.Method;
import java.net.Authenticator;
import java.net.Authenticator.RequestorType;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Objects;

import jdk.incubator.http.internal.common.Utils;

/**
 * Reflection utility that emulates the logic of the
 * {@code requestPasswordAuthenticationInstance()} method in
 * {@link Authenticator} that has been introduced in Java 9.
 */
final class AuthenticatorHack {

    private AuthenticatorHack() {
           throw new AssertionError();
    }

    public static PasswordAuthentication requestPasswordAuthenticationInstance(Authenticator auth, String host,
            InetAddress addr, int port, String protocol, String prompt, String scheme, URL url, RequestorType reqType) {
        Objects.requireNonNull(auth);

        if (!IS_JAVA8) {
            return auth.requestPasswordAuthenticationInstance(host, addr, port, protocol, prompt, scheme, url, reqType);
        }

        synchronized (auth) {
            reset(auth);
            setPort(auth, port);
            setObject(auth, REQ_HOST, host);
            setObject(auth, REQ_SITE, addr);
            setObject(auth, REQ_PROT, protocol);
            setObject(auth, REQ_PRMPT, prompt);
            setObject(auth, REQ_SCHEME, scheme);
            setObject(auth, REQ_URL, url);
            setObject(auth, REQ_TYPE, reqType);
            return getPasswordAuthentication(auth);
        }
    }

    private static void setObject(Authenticator auth, long offset, Object value) {
        try {
            U.putObject(auth, offset, value);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static void setPort(Authenticator auth, int value) {
        try {
            U.putInt(auth, REQ_PORT, value);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static void reset(Authenticator auth) {
        try {
            RESET.invoke(auth);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static PasswordAuthentication getPasswordAuthentication(Authenticator auth) {
        try {
            return (PasswordAuthentication) PWD_AUTH_GET.invoke(auth);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static final boolean IS_JAVA8 = Utils.isJava8();
    private static final sun.misc.Unsafe U = UnsafeAccess.unsafe;
    private static final Method RESET;
    private static final Method PWD_AUTH_GET;
    private static final long REQ_HOST; // String
    private static final long REQ_SITE; // InetAddress
    private static final long REQ_PORT; // int
    private static final long REQ_PROT; // String
    private static final long REQ_PRMPT; // String
    private static final long REQ_SCHEME; // String
    private static final long REQ_URL; // URL
    private static final long REQ_TYPE; // RequestorType
    static {
        try {
            Class<?> authClass = Authenticator.class;
            RESET = authClass.getDeclaredMethod("reset");
            RESET.setAccessible(true);
            PWD_AUTH_GET = authClass.getDeclaredMethod("getPasswordAuthentication");
            PWD_AUTH_GET.setAccessible(true);
            REQ_HOST = U.objectFieldOffset(authClass.getDeclaredField("requestingHost"));
            REQ_SITE = U.objectFieldOffset(authClass.getDeclaredField("requestingSite"));
            REQ_PORT = U.objectFieldOffset(authClass.getDeclaredField("requestingPort"));
            REQ_PROT = U.objectFieldOffset(authClass.getDeclaredField("requestingProtocol"));
            REQ_PRMPT = U.objectFieldOffset(authClass.getDeclaredField("requestingPrompt"));
            REQ_SCHEME = U.objectFieldOffset(authClass.getDeclaredField("requestingScheme"));
            REQ_URL = U.objectFieldOffset(authClass.getDeclaredField("requestingURL"));
            REQ_TYPE = U.objectFieldOffset(authClass.getDeclaredField("requestingAuthType"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
