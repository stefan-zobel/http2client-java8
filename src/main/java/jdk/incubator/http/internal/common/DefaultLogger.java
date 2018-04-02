/*
 * Written by Stefan Zobel and released to the
 * public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package jdk.incubator.http.internal.common;

import java.util.ResourceBundle;

/**
 * {@link SysLogger} stub that currently does nothing.
 */
final class DefaultLogger implements SysLogger {

    DefaultLogger(String name) {
        // XXX
    }

    @Override
    public String getName() {
        // XXX
        return "no-op stub logger: name not set";
    }

    @Override
    public boolean isLoggable(Level level) {
        // XXX
        return false;
    }

    @Override
    public void log(Level level, ResourceBundle bundle, String msg, Throwable thrown) {
        // XXX
    }

    @Override
    public void log(Level level, ResourceBundle bundle, String format, Object... params) {
        // XXX
    }
}
