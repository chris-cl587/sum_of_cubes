package org.cliu;

import com.squareup.jnagmp.Gmp;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TWO;
import static java.math.BigInteger.ZERO;

public class GenericUtils {
    static long goodMask; // 0xC840C04048404040 computed below
    {
        for (int i = 0; i < 64; ++i) GenericUtils.goodMask |= Long.MIN_VALUE >>> (i * i);
    };

    // Sourced and adapted from https://github.com/randombit/botan/blob/c32ac80d130be64ce5357b29a5fa82cad7aa2564/src/lib/math/numbertheory/mod_inv.cpp#L27-L90
    // Adapted from C++ to Java.
    public static long montgomery_inverse(long a,
                                   long p) {
        long k = 0;

        long u = p, v = a, r = 0, s = 1;

        while (v > 0) {
            if (u % 2 == 0) {
                u >>= 1;
                s <<= 1;
            } else if (v % 2 == 0) {
                v >>= 1;
                r <<= 1;
            } else if (u > v) {
                u -= v;
                u >>= 1;
                r += s;
                s <<= 1;
            } else {
                v -= u;
                v >>= 1;
                s += r;
                r <<= 1;
            }

            ++k;
        }

        if (r >= p) {
            r -= p;
        }

        var result = p - r;


        for (int i = 0; i != k; ++i) {
            if (result % 2 == 1) {
                result += p;
            }
            result >>= 1;
        }

        return result;
    }

    // TODO: Investigate into https://eprint.iacr.org/2017/411.pdf
    static long inverse_mod_prime_power(long a, long p, long e) {
        long montyinv = montgomery_inverse(a, p);
        if (e == 1) return montyinv;
        long tmp;
        // Reference: https://hal.archives-ouvertes.fr/file/index/docid/736701/filename/invmodpk.pdf
        // Algorithm 2, there looks to be a typo, should increment `i` rather than bit-shift...
        for (int i = 0; i < e; i += 1) {
            tmp = 2-a*montyinv;
            tmp = Math.floorMod(tmp, (long)Math.pow(p, e));
            montyinv *= tmp;
            montyinv = Math.floorMod(montyinv, (long)Math.pow(p, e));
        }
        return montyinv;
    }

    static long pow(long b, long e, long m) {
        // REMARK: Appears LibGMP is slower than BigInteger for our setup, re-investigate later.
//        var gmpPow = Gmp.modPowInsecure(BigInteger.valueOf(b), BigInteger.valueOf(e), BigInteger.valueOf(m)).longValueExact();
        var biPow = BigInteger.valueOf(b).modPow(BigInteger.valueOf(e), BigInteger.valueOf(m)).longValue();
//        if (gmpPow != biPow) {
//            throw new RuntimeException(String.format("GMP modPowInsecure did not match BI pow for b=%s, e=%s, m=%s", b,e,m));
//        }
        return biPow;
    }

    /**
     * Taken from  `bouncycastle` library,
     * See https://github.com/bcgit/bc-java/blob/07604208a773d2334fb09276796288404804e557/core/src/main/java/org/bouncycastle/pqc/math/linearalgebra/IntegerFunctions.java
     *
     * Computes the square root of a BigInteger modulo a prime employing the
     * Shanks-Tonelli algorithm.
     *
     * @param a value out of which we extract the square root
     * @param p prime modulus that determines the underlying field
     * @return a number <tt>b</tt> such that b<sup>2</sup> = a (mod p) if
     * <tt>a</tt> is a quadratic residue modulo <tt>p</tt>.
     * @throws IllegalArgumentException if <tt>a</tt> is a quadratic non-residue modulo <tt>p</tt>
     */
    public static BigInteger squareRootModuloPrime(BigInteger a, BigInteger p)
            throws IllegalArgumentException {

        BigInteger v = null;

        if (a.compareTo(ZERO) < 0) {
            a = a.add(p);
        }

        if (a.equals(ZERO)) {
            return ZERO;
        }

        if (p.equals(TWO)) {
            return a;
        }

        // p = 3 mod 4
        if (p.testBit(0) && p.testBit(1)) {
            if (jacobi(a, p) == 1) { // a quadr. residue mod p
                v = p.add(ONE); // v = p+1
                v = v.shiftRight(2); // v = v/4
                return a.modPow(v, p); // return a^v mod p
                // return --> a^((p+1)/4) mod p
            }
            throw new IllegalArgumentException("No quadratic residue: " + a + ", " + p);
        }

        long t = 0;

        // initialization
        // compute k and s, where p = 2^s (2k+1) +1

        BigInteger k = p.subtract(ONE); // k = p-1
        long s = 0;
        while (!k.testBit(0)) { // while k is even
            s++; // s = s+1
            k = k.shiftRight(1); // k = k/2
        }

        k = k.subtract(ONE); // k = k - 1
        k = k.shiftRight(1); // k = k/2

        // initial values
        BigInteger r = a.modPow(k, p); // r = a^k mod p

        BigInteger n = r.multiply(r).remainder(p); // n = r^2 % p
        n = n.multiply(a).remainder(p); // n = n * a % p
        r = r.multiply(a).remainder(p); // r = r * a %p

        if (n.equals(ONE)) {
            return r;
        }

        // non-quadratic residue
        BigInteger z = TWO; // z = 2
        while (jacobi(z, p) == 1) {
            // while z quadratic residue
            z = z.add(ONE); // z = z + 1
        }

        v = k;
        v = v.multiply(TWO); // v = 2k
        v = v.add(ONE); // v = 2k + 1
        BigInteger c = z.modPow(v, p); // c = z^v mod p

        // iteration
        while (n.compareTo(ONE) == 1) { // n > 1
            k = n; // k = n
            t = s; // t = s
            s = 0;

            while (!k.equals(ONE)) { // k != 1
                k = k.multiply(k).mod(p); // k = k^2 % p
                s++; // s = s + 1
            }

            t -= s; // t = t - s
            if (t == 0) {
                throw new IllegalArgumentException("No quadratic residue: " + a + ", " + p);
            }

            v = ONE;
            for (long i = 0; i < t - 1; i++) {
                v = v.shiftLeft(1); // v = 1 * 2^(t - 1)
            }
            c = c.modPow(v, p); // c = c^v mod p
            r = r.multiply(c).remainder(p); // r = r * c % p
            c = c.multiply(c).remainder(p); // c = c^2 % p
            n = n.multiply(c).mod(p); // n = n * c % p
        }
        return r;
    }

    public static int jacobi(BigInteger A, BigInteger B) {
        BigInteger a, b, v;
        long k = 1;

        k = 1;

        // test trivial cases
        if (B.equals(ZERO)) {
            a = A.abs();
            return a.equals(ONE) ? 1 : 0;
        }

        if (!A.testBit(0) && !B.testBit(0)) {
            return 0;
        }

        a = A;
        b = B;

        if (b.signum() == -1) { // b < 0
            b = b.negate(); // b = -b
            if (a.signum() == -1) {
                k = -1;
            }
        }

        v = ZERO;
        while (!b.testBit(0)) {
            v = v.add(ONE); // v = v + 1
            b = b.divide(TWO); // b = b/2
        }

        if (v.testBit(0)) {
            k = k * Utils.jacobiTable[a.intValue() & 7];
        }

        if (a.signum() < 0) { // a < 0
            if (b.testBit(1)) {
                k = -k; // k = -k
            }
            a = a.negate(); // a = -a
        }

        // main loop
        while (a.signum() != 0) {
            v = ZERO;
            while (!a.testBit(0)) { // a is even
                v = v.add(ONE);
                a = a.divide(TWO);
            }
            if (v.testBit(0)) {
                k = k * Utils.jacobiTable[b.intValue() & 7];
            }

            if (a.compareTo(b) < 0) { // a < b
                // swap and correct intermediate result
                BigInteger x = a;
                a = b;
                b = x;
                if (a.testBit(1) && b.testBit(1)) {
                    k = -k;
                }
            }
            a = a.subtract(b);
        }

        return b.equals(ONE) ? (int) k : 0;
    }

    public static <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
        List<List<T>> combinations = Arrays.asList(Arrays.asList());
        for (List<T> list : lists) {
            List<List<T>> extraColumnCombinations = new ArrayList<>();
            for (List<T> combination : combinations) {
                for (T element : list) {
                    List<T> newCombination = new ArrayList<>(combination);
                    newCombination.add(element);
                    extraColumnCombinations.add(newCombination);
                }
            }
            combinations = extraColumnCombinations;
        }
        return combinations;
    }

    public static boolean isSquareCandidate(long d, long z, int k) {
        var threed = BigInteger.valueOf(3 * d);
        var zcubed = BigInteger.valueOf(z).pow(3);
        var dcubed = BigInteger.valueOf(d).pow(3);
        var absKMinusZCubed = BigInteger.valueOf(k).subtract(zcubed).abs();
        var candidate = threed.multiply(
                (BigInteger.valueOf(4).multiply(absKMinusZCubed)).subtract(dcubed)
        );
        // REMARK: Any optimization needs to be using BigInt as the number can very well be greater than
        // a max Long.
        var isSquareBigInt = candidate.signum() == 1 && candidate.sqrtAndRemainder()[1].equals(ZERO);
        return isSquareBigInt;
    }

    static long legendreSymbol(long n, long p) {
        long count, temp;
        long legendre = 1;
        if (n == 0)
            return 0;
        if (n < 0) {
            n = -n;
            if (p % 4 == 3)
                legendre = -1;
        }
        do {
            count = 0;
            while (n % 2 == 0) {
                n = n / 2;
                count = 1 - count;
            }
            if ((count * (p * p - 1)) % 16 == 8)
                legendre = -legendre;
            if (((n - 1) * (p - 1)) % 8 == 4)
                legendre = -legendre;
            temp = n;
            n = p % n;
            p = temp;
        } while (n > 1);
        return legendre;
    }
}
