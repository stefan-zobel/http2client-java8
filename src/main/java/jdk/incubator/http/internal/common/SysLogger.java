/*
 * Copyright (c) 1994, 2017, Oracle and/or its affiliates. All rights reserved.
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
package jdk.incubator.http.internal.common;

import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Supplier;

/**
 * {@code SysLogger} instances log messages that will be routed to the
 * underlying logging framework.
 * <p>
 * {@code SysLogger} instances are typically obtained by calling
 * {@link SysLogger#getLogger(String) SysLogger.getLogger(loggerName)}.
 *
 * @see SysLogger#getLogger(String)
 *
 * @since 9
 */
public interface SysLogger {
    /**
     * System {@linkplain SysLogger loggers} levels.
     * <p>
     * A level has a {@linkplain #getName() name} and {@linkplain #getSeverity()
     * severity}. Level values are {@link #ALL}, {@link #TRACE}, {@link #DEBUG},
     * {@link #INFO}, {@link #WARNING}, {@link #ERROR}, {@link #OFF}, by order of
     * increasing severity. <br>
     * {@link #ALL} and {@link #OFF} are simple markers with severities mapped
     * respectively to {@link java.lang.Integer#MIN_VALUE Integer.MIN_VALUE} and
     * {@link java.lang.Integer#MAX_VALUE Integer.MAX_VALUE}.
     * <p>
     * <b>Severity values and Mapping to {@code java.util.logging.Level}.</b>
     * <p>
     * {@linkplain SysLogger.Level System logger levels} are mapped to
     * {@linkplain java.util.logging.Level java.util.logging levels} of
     * corresponding severity. <br>
     * The mapping is as follows: <br>
     * <br>
     * <table class="striped">
     * <caption>System.Logger Severity Level Mapping</caption> <thead>
     * <tr>
     * <th scope="col">System.Logger Levels</th>
     * <th scope="col">java.util.logging Levels</th> </thead> <tbody>
     * <tr>
     * <th scope="row">{@link SysLogger.Level#ALL ALL}</th>
     * <td>{@link java.util.logging.Level#ALL ALL}</td>
     * <tr>
     * <th scope="row">{@link SysLogger.Level#TRACE TRACE}</th>
     * <td>{@link java.util.logging.Level#FINER FINER}</td>
     * <tr>
     * <th scope="row">{@link SysLogger.Level#DEBUG DEBUG}</th>
     * <td>{@link java.util.logging.Level#FINE FINE}</td>
     * <tr>
     * <th scope="row">{@link SysLogger.Level#INFO INFO}</th>
     * <td>{@link java.util.logging.Level#INFO INFO}</td>
     * <tr>
     * <th scope="row">{@link SysLogger.Level#WARNING WARNING}</th>
     * <td>{@link java.util.logging.Level#WARNING WARNING}</td>
     * <tr>
     * <th scope="row">{@link SysLogger.Level#ERROR ERROR}</th>
     * <td>{@link java.util.logging.Level#SEVERE SEVERE}</td>
     * <tr>
     * <th scope="row">{@link SysLogger.Level#OFF OFF}</th>
     * <td>{@link java.util.logging.Level#OFF OFF}</td> </tbody>
     * </table>
     *
     * @since 9
     *
     * @see jdk.incubator.http.internal.common.SysLogger
     */
    public enum Level {
        // for convenience, we're reusing java.util.logging.Level int values
        // the mapping logic in sun.util.logging.PlatformLogger depends
        // on this.
        /**
         * A marker to indicate that all levels are enabled. This level
         * {@linkplain #getSeverity() severity} is {@link Integer#MIN_VALUE}.
         */
        ALL(Integer.MIN_VALUE), // typically mapped to/from j.u.l.Level.ALL
        /**
         * {@code TRACE} level: usually used to log diagnostic information. This level
         * {@linkplain #getSeverity() severity} is {@code 400}.
         */
        TRACE(400), // typically mapped to/from j.u.l.Level.FINER
        /**
         * {@code DEBUG} level: usually used to log debug information traces. This level
         * {@linkplain #getSeverity() severity} is {@code 500}.
         */
        DEBUG(500), // typically mapped to/from j.u.l.Level.FINEST/FINE/CONFIG
        /**
         * {@code INFO} level: usually used to log information messages. This level
         * {@linkplain #getSeverity() severity} is {@code 800}.
         */
        INFO(800), // typically mapped to/from j.u.l.Level.INFO
        /**
         * {@code WARNING} level: usually used to log warning messages. This level
         * {@linkplain #getSeverity() severity} is {@code 900}.
         */
        WARNING(900), // typically mapped to/from j.u.l.Level.WARNING
        /**
         * {@code ERROR} level: usually used to log error messages. This level
         * {@linkplain #getSeverity() severity} is {@code 1000}.
         */
        ERROR(1000), // typically mapped to/from j.u.l.Level.SEVERE
        /**
         * A marker to indicate that all levels are disabled. This level
         * {@linkplain #getSeverity() severity} is {@link Integer#MAX_VALUE}.
         */
        OFF(Integer.MAX_VALUE); // typically mapped to/from j.u.l.Level.OFF

        private final int severity;

        private Level(int severity) {
            this.severity = severity;
        }

        /**
         * Returns the name of this level.
         * 
         * @return this level {@linkplain #name()}.
         */
        public final String getName() {
            return name();
        }

        /**
         * Returns the severity of this level. A higher severity means a more severe
         * condition.
         * 
         * @return this level severity.
         */
        public final int getSeverity() {
            return severity;
        }
    }

    /**
     * Returns the name of this logger.
     *
     * @return the logger name.
     */
    public String getName();

    /**
     * Checks if a message of the given level would be logged by this logger.
     *
     * @param level
     *            the log message level.
     * @return {@code true} if the given log message level is currently being
     *         logged.
     *
     * @throws NullPointerException
     *             if {@code level} is {@code null}.
     */
    public boolean isLoggable(SysLogger.Level level);

    /**
     * Logs a message.
     *
     * <p>
     * <b>Implementation Requirements:</b><br>
     * The default implementation for this method calls
     * {@code this.log(level, (ResourceBundle)null, msg, (Object[])null);}
     *
     * @param level
     *            the log message level.
     * @param msg
     *            the string message; can be {@code null}.
     *
     * @throws NullPointerException
     *             if {@code level} is {@code null}.
     */
    public default void log(SysLogger.Level level, String msg) {
        log(level, (ResourceBundle) null, msg, (Object[]) null);
    }

    /**
     * Logs a lazily supplied message.
     * <p>
     * If the logger is currently enabled for the given log message level then a
     * message is logged that is the result produced by the given supplier function.
     * Otherwise, the supplier is not operated on.
     *
     * <p>
     * <b>Implementation Requirements:</b><br>
     * When logging is enabled for the given level, the default implementation for
     * this method calls
     * {@code this.log(level, (ResourceBundle)null, msgSupplier.get(), (Object[])null);}
     *
     * @param level
     *            the log message level.
     * @param msgSupplier
     *            a supplier function that produces a message.
     *
     * @throws NullPointerException
     *             if {@code level} is {@code null}, or {@code msgSupplier} is
     *             {@code null}.
     */
    public default void log(SysLogger.Level level, Supplier<String> msgSupplier) {
        Objects.requireNonNull(msgSupplier);
        if (isLoggable(Objects.requireNonNull(level))) {
            log(level, (ResourceBundle) null, msgSupplier.get(), (Object[]) null);
        }
    }

    /**
     * Logs a message produced from the given object.
     * <p>
     * If the logger is currently enabled for the given log message level then a
     * message is logged that, by default, is the result produced from calling
     * toString on the given object. Otherwise, the object is not operated on.
     *
     * <p>
     * <b>Implementation Requirements:</b><br>
     * When logging is enabled for the given level, the default implementation for
     * this method calls
     * {@code this.log(level, (ResourceBundle)null, obj.toString(), (Object[])null);}
     *
     * @param level
     *            the log message level.
     * @param obj
     *            the object to log.
     *
     * @throws NullPointerException
     *             if {@code level} is {@code null}, or {@code obj} is {@code null}.
     */
    public default void log(SysLogger.Level level, Object obj) {
        Objects.requireNonNull(obj);
        if (isLoggable(Objects.requireNonNull(level))) {
            this.log(level, (ResourceBundle) null, obj.toString(), (Object[]) null);
        }
    }

    /**
     * Logs a message associated with a given throwable.
     *
     * <p>
     * <b>Implementation Requirements:</b><br>
     * The default implementation for this method calls
     * {@code this.log(level, (ResourceBundle)null, msg, thrown);}
     *
     * @param level
     *            the log message level.
     * @param msg
     *            the string message; can be {@code null}.
     * @param thrown
     *            a {@code Throwable} associated with the log message; can be
     *            {@code null}.
     *
     * @throws NullPointerException
     *             if {@code level} is {@code null}.
     */
    public default void log(SysLogger.Level level, String msg, Throwable thrown) {
        this.log(level, null, msg, thrown);
    }

    /**
     * Logs a lazily supplied message associated with a given throwable.
     * <p>
     * If the logger is currently enabled for the given log message level then a
     * message is logged that is the result produced by the given supplier function.
     * Otherwise, the supplier is not operated on.
     *
     * <p>
     * <b>Implementation Requirements:</b><br>
     * When logging is enabled for the given level, the default implementation for
     * this method calls
     * {@code this.log(level, (ResourceBundle)null, msgSupplier.get(), thrown);}
     *
     * @param level
     *            one of the log message level identifiers.
     * @param msgSupplier
     *            a supplier function that produces a message.
     * @param thrown
     *            a {@code Throwable} associated with log message; can be
     *            {@code null}.
     *
     * @throws NullPointerException
     *             if {@code level} is {@code null}, or {@code msgSupplier} is
     *             {@code null}.
     */
    public default void log(SysLogger.Level level, Supplier<String> msgSupplier, Throwable thrown) {
        Objects.requireNonNull(msgSupplier);
        if (isLoggable(Objects.requireNonNull(level))) {
            this.log(level, null, msgSupplier.get(), thrown);
        }
    }

    /**
     * Logs a message with an optional list of parameters.
     *
     * <p>
     * <b>Implementation Requirements:</b><br>
     * The default implementation for this method calls
     * {@code this.log(level, (ResourceBundle)null, format, params);}
     *
     * @param level
     *            one of the log message level identifiers.
     * @param format
     *            the string message format in {@link java.text.MessageFormat}
     *            format, can be {@code null}.
     * @param params
     *            an optional list of parameters to the message (may be none).
     *
     * @throws NullPointerException
     *             if {@code level} is {@code null}.
     */
    public default void log(SysLogger.Level level, String format, Object... params) {
        this.log(level, null, format, params);
    }

    /**
     * Logs a localized message associated with a given throwable.
     * <p>
     * If the given resource bundle is non-{@code null}, the {@code msg} string is
     * localized using the given resource bundle. Otherwise the {@code msg} string
     * is not localized.
     *
     * @param level
     *            the log message level.
     * @param bundle
     *            a resource bundle to localize {@code msg}; can be {@code null}.
     * @param msg
     *            the string message (or a key in the message catalog, if
     *            {@code bundle} is not {@code null}); can be {@code null}.
     * @param thrown
     *            a {@code Throwable} associated with the log message; can be
     *            {@code null}.
     *
     * @throws NullPointerException
     *             if {@code level} is {@code null}.
     */
    public void log(SysLogger.Level level, ResourceBundle bundle, String msg, Throwable thrown);

    /**
     * Logs a message with resource bundle and an optional list of parameters.
     * <p>
     * If the given resource bundle is non-{@code null}, the {@code format} string
     * is localized using the given resource bundle. Otherwise the {@code format}
     * string is not localized.
     *
     * @param level
     *            the log message level.
     * @param bundle
     *            a resource bundle to localize {@code format}; can be {@code null}.
     * @param format
     *            the string message format in {@link java.text.MessageFormat}
     *            format, (or a key in the message catalog if {@code bundle} is not
     *            {@code null}); can be {@code null}.
     * @param params
     *            an optional list of parameters to the message (may be none).
     *
     * @throws NullPointerException
     *             if {@code level} is {@code null}.
     */
    public void log(SysLogger.Level level, ResourceBundle bundle, String format, Object... params);

    public static SysLogger getLogger(String name) {
        Objects.requireNonNull(name);
        // XXX
        return new DefaultLogger(name);
     }
}
