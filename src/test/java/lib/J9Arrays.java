package lib;

public final class J9Arrays {
    /**
     * Compares two {@code byte} arrays lexicographically.
     *
     * <p>
     * If the two arrays share a common prefix then the lexicographic comparison
     * is the result of comparing two elements, as if by
     * {@link Byte#compare(byte, byte)}, at an index within the respective
     * arrays that is the prefix length. Otherwise, one array is a proper prefix
     * of the other and, lexicographic comparison is the result of comparing the
     * two array lengths. (See {@link #mismatch(byte[], byte[])} for the
     * definition of a common and proper prefix.)
     *
     * <p>
     * A {@code null} array reference is considered lexicographically less than
     * a non-{@code null} array reference. Two {@code null} array references are
     * considered equal.
     *
     * <p>
     * The comparison is consistent with {@link #equals(byte[], byte[]) equals},
     * more specifically the following holds for arrays {@code a} and {@code b}:
     * 
     * <pre>
     * {@code
     *     Arrays.equals(a, b) == (Arrays.compare(a, b) == 0)
     * }
     * </pre>
     *
     * @apiNote
     *          <p>
     *          This method behaves as if (for non-{@code null} array
     *          references):
     * 
     *          <pre>
     * {@code
     *     int i = Arrays.mismatch(a, b);
     *     if (i >= 0 && i < Math.min(a.length, b.length))
     *         return Byte.compare(a[i], b[i]);
     *     return a.length - b.length;
     * }
     *          </pre>
     *
     * @param a
     *            the first array to compare
     * @param b
     *            the second array to compare
     * @return the value {@code 0} if the first and second array are equal and
     *         contain the same elements in the same order; a value less than
     *         {@code 0} if the first array is lexicographically less than the
     *         second array; and a value greater than {@code 0} if the first
     *         array is lexicographically greater than the second array
     * @since 9
     */
    public static int compare(byte[] a, byte[] b) {
        if (a == b)
            return 0;
        if (a == null || b == null)
            return a == null ? -1 : 1;

        int i = mismatch(a, b, Math.min(a.length, b.length));
        if (i >= 0) {
            return Byte.compare(a[i], b[i]);
        }

        return a.length - b.length;
    }

    /**
     * Find the index of a mismatch between two arrays.
     *
     * <p>
     * This method does not perform bounds checks. It is the responsibility of
     * the caller to perform such bounds checks before calling this method.
     *
     * @param a
     *            the first array to be tested for a mismatch
     * @param b
     *            the second array to be tested for a mismatch
     * @param length
     *            the number of bytes from each array to check
     * @return the index of a mismatch between the two arrays, otherwise -1 if
     *         no mismatch. The index will be within the range of (inclusive) 0
     *         to (exclusive) the smaller of the two array lengths.
     */
    private static int mismatch(byte[] a, byte[] b, int length) {
        for (int i = 0; i < length; i++) {
            if (a[i] != b[i]) {
                return i;
            }
        }
        return -1;
    }

    private J9Arrays() {
        throw new AssertionError();
    }
}
