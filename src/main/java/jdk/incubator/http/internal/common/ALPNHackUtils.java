/*
 * Written by Stefan Zobel and released to the
 * public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package jdk.incubator.http.internal.common;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Some static helper methods for the undertow ALPN hack. 
 */
final class ALPNHackUtils {

    static int getInt8(ByteBuffer input) {
        return input.get();
    }

    static int getInt16(ByteBuffer input) {
        return (input.get() & 0xFF) << 8 | input.get() & 0xFF;
    }

    static int getInt24(ByteBuffer input) {
        return (input.get() & 0xFF) << 16 | (input.get() & 0xFF) << 8 | input.get() & 0xFF;
    }

    static String readByteVector8(ByteBuffer input) {
        int length = getInt8(input);
        byte[] data = new byte[length];
        input.get(data);
        return new String(data, StandardCharsets.US_ASCII);
    }

    private ALPNHackUtils() {
    }
}
