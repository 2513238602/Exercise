public class Medians {
    /**
     * Compute the median of three integers. The median is the
     * number that appears in the middle when the three numbers
     * are sorted by size.
     * @param a First integer
     * @param b Second integer
     * @param c Third integer
     * @return The median of a, b, and c
     */
    static int medianOf3(int a, int b, int c) {
        if (a >= b)
            if (b >= c)
                return b;
            else if (a <= c)
                return c;
            else
                return a;
        else {
            if (c >= b)
                return b;
            else if (a <= c)
                return c;
            else
                return a;
        }
    }

    /**
     * Compute the median of five integers.
     * @param a First integer
     * @param b Second integer
     * @param c Third integer
     * @param d Fourth integer
     * @param e Fifth integer
     * @return The median of a, b, c, d and e
     */
    static int medianOf5(int a, int b, int c, int d, int e) {
        if (a == maxOf5(a, b, c, d, e)) {
            if (b == minOf5(a, b, c, d, e))
                return medianOf3(c, d, e);
            else if (c == minOf5(a, b, c, d, e))
                return medianOf3(b, d, e);
            else if (d == minOf5(a, b, c, d, e))
                return medianOf3(b, c, e);
            else
                return medianOf3(b, c, d);
        }
        else if (b == maxOf5(a, b, c, d, e)) {
            if (a == minOf5(a, b, c, d, e))
                return medianOf3(c, d, e);
            else if (c == minOf5(a, b, c, d, e))
                return medianOf3(a, d, e);
            else if (d == minOf5(a, b, c, d, e))
                return medianOf3(a, c, e);
            else
                return medianOf3(a, c, d);
        }
        else if (c == maxOf5(a, b, c, d, e)) {
            if (a == minOf5(a, b, c, d, e))
                return medianOf3(b, d, e);
            else if (b == minOf5(a, b, c, d, e))
                return medianOf3(a, d, e);
            else if (d == minOf5(a, b, c, d, e))
                return medianOf3(a, b, e);
            else
                return medianOf3(a, b, d);
        }
        else if (d == maxOf5(a, b, c, d, e)) {
            if (a == minOf5(a, b, c, d, e))
                return medianOf3(b, c, e);
            else if (b == minOf5(a, b, c, d, e))
                return medianOf3(a, c, e);
            else if (c == minOf5(a, b, c, d, e))
                return medianOf3(a, b, e);
            else
                return medianOf3(a, b, c);
        }
        else {
            if (a == minOf5(a, b, c, d, e))
                return medianOf3(b, c, d);
            else if (b == minOf5(a, b, c, d, e))
                return medianOf3(a, c, d);
            else if (c == minOf5(a, b, c, d, e))
                return medianOf3(a, b, d);
            else
                return medianOf3(a, b, c);
        }
    }

    /**
     * Return the maximum of two integers.
     * @return max(a, b)
     */
    static int maxOf2(int a, int b) {
        return (a >= b ? a : b);
    }

    /**
     * Return the maximum of three integers.
     * @return max(a, b, c)
     */
    static int maxOf3(int a, int b, int c) {
        return maxOf2(maxOf2(a, b), c);
    }

    /**
     * Return the maximum of five integers.
     * @return max(a, b, c, d, e)
     */
    static int maxOf5(int a, int b, int c, int d, int e) {
        return maxOf2(maxOf2(a, b), maxOf2(maxOf2(c, d), e));
    }

    /**
     * Return the minimum of two integers.
     * @return min(a, b)
     */
    static int minOf2(int a, int b) {
        return (a <= b ? a : b);
    }

    /**
     * Return the minimum of three integers.
     * @return min(a, b, c)
     */
    static int minOf3(int a, int b, int c) {
        return minOf2(minOf2(a, b), c);
    }

    /**
     * Return the minimum of five integers.
     * @return min(a, b, c, d, e)
     */
    static int minOf5(int a, int b, int c, int d, int e) {
        return minOf2(minOf2(a, b), minOf3(c, d, e));
    }

    public static void main(String[] arg) {
        // The median of 1,2,3,4 and 5 is 3, no matter what order they're in:
        System.out.println(medianOf5(1,2,3,4,5));
        System.out.println(medianOf5(5,4,3,2,1));
        System.out.println(medianOf5(1,5,2,3,4));
        System.out.println(medianOf5(1,4,5,2,3));
        System.out.println(medianOf5(5,1,4,3,2));
        System.out.println(medianOf5(5,2,1,4,3));
        System.out.println(medianOf5(2,1,3,4,5));
        System.out.println(medianOf5(2,5,1,3,4));
        System.out.println(medianOf5(2,5,4,1,3));
    }
}
