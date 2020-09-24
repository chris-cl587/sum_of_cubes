package org.cliu;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.scravy.primes.Primes;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Pair;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.Math.addExact;
import static java.lang.Math.floorMod;
import static java.lang.Math.multiplyExact;
import static java.lang.Math.subtractExact;
import static java.math.BigInteger.ONE;

public class Utils {
    // the jacobi function uses this lookup table
    static final int[] jacobiTable = {0, 1, 0, -1, 0, -1, 0, 1};

    private static final Cache<Pair<Long, Long>, Long> crtCache = Caffeine.newBuilder()
            .maximumSize((int) 1e8)
            .build();
    /**
     * From labmath method.
     * def crt(rems, mods): # moduli and remainders are lists; moduli must be pairwsise coprime.
     * """
     * Return the unique integer in range(iterprod(mods)) that reduces to x mod y
     * for (x,y) in zip(rems,mods).  All elements of mods must be pairwise coprime.
     * Input: rems, mods -- iterables of the same finite length containing integers
     * Output: an integer in range(iterprod(mods))
     * Examples:
     * >>> crt((1, 2), (3, 4))
     * 10
     * >>> crt((4, 5), (10, 3))
     * 14
     * >>> crt((-1, -1), (100, 101))
     * 10099
     * """
     * if len(mods) == 1: return rems[0]
     * N = iterprod(mods)
     * return sum(r * (N//m) * modinv(N//m, m) for (r, m) in zip(rems, mods) if m != 1) % N
     */
    static Long crt(long[] rems, long[] coprimes) {
        // START INDUCTIVE SOLUTION HERE
        return crt_inductive(coprimes, rems);
        // END INDUCTIVE SOLUTION HERE
        // START CONSTRUCTIVE SOLUTION HERE - looks to be a bit slower than using the inductive solution.
//        var N = 1L;
//        for (int i = 0; i < coprimes.size(); i++) N *= (long)Math.pow(coprimes.get(i), pows.get(i));
//
//        BigInteger acc = BigInteger.ZERO;
//        for (int i = 0; i < rems.size(); i++) {
//            long r = rems.get(i);
//            long p = coprimes.get(i);
//            long e = pows.get(i);
//            long m = (long) Math.pow(p, e);
//            if (m == 1) continue;
//            final var mBigInt = BigInteger.valueOf(m);
//            var N_over_m = N / m;
//            final var cacheKey = new Pair<>(N, m);
//            long invOpt = crtCache.get(cacheKey, k -> m % 2 == 0 ? BigInteger.valueOf(N_over_m).modInverse(mBigInt).longValueExact() : GenericUtils.inverse_mod_prime_power(N_over_m, p, e));
////            long invOpt = m % 2 == 0 ? BigInteger.valueOf(N_over_m).modInverse(mBigInt).longValueExact() : GenericUtils.inverse_mod_prime_power(N_over_m, p, e);
//            var toAdd = BigInteger.valueOf(invOpt).multiply(BigInteger.valueOf(r * N_over_m));
//            acc = acc.add(toAdd);
//        }
//        try {
//            return acc.mod(BigInteger.valueOf(N)).longValueExact();
//        } catch (Exception e){
//            System.out.println(String.format("overflow! acc: %s N: %s coprimes: %s, pows: %s", acc, N, coprimes, pows));
//            throw e;
//        }
        // END CONSTRUCTIVE SOLUTION HERE
    }

    /*
        From labmath method.
        def hensel(f, p, k): # Finds all solutions to f(x) == 0 mod p**k.

        Uses Hensel lifting to generate with some efficiency all zeros of a
        polynomial modulo a prime power.
        Input:
            f -- List.  These are the polynomial's coefficients in order of
                 increasing degree.
            p, k -- Integers.  We find zeros modulo p**k, where p is assumed prime.
        Output: Finite sequence of integers.
        Examples:
        >>> list(hensel([1,2,3], 3, 27))
        [7195739071075]
        >>> f = [3,3,3,3,3,6]
        >>> sorted(hensel(f, 3, 4))
        []
        >>> [x for x in range(3**4) if polyval(f, x, 3**4) == 0]
        []
        >>> f = [3,3,3,3,3,3]
        >>> sorted(hensel(f[:], 3, 4))
        [8, 17, 26, 35, 44, 53, 62, 71, 80]
        >>> [x for x in range(3**4) if polyval(f[:], x, 3**4) == 0]
        [8, 17, 26, 35, 44, 53, 62, 71, 80]
        """
        assert k > 0 and isprime(p)
        if k == 1: yield from polyroots_prime(f, p); return
        pk1, df = p**(k-1), [n*c for (n,c) in enumerate(f)][1:]    # df = derivative of f
        pk = pk1 * p
        for n in hensel(f, p, k-1):
            dfn, fn = polyval(df, n, p), polyval(f, n, pk)
            yield from [n + ((-modinv(dfn,p) * fn // pk1) % p) * pk1] if dfn else [] if fn else (n + t * pk1 for t in range(p))
         */
    static Cache<String, List<Long>> henselCuberootCache = Caffeine.newBuilder()
            .maximumSize((long)1e8)
            .build();

    /**
     * Computes the cuberoot of k mod p^e.
     * Does so in a cached fashion to avoid re-computing these values over and over.
     */
    public static List<Long> hensel_cuberoot(long prime, long primeExp, long cuberoot_k) {
        var cacheKey = prime + "~" + primeExp + "~" + cuberoot_k;
        return henselCuberootCache.get(cacheKey, key -> hensel(List.of(-cuberoot_k, 0L, 0L, 1L), prime, primeExp, cuberoot_k));
    }

    // @VisibleForTesting
    static List<Long> hensel(List<Long> f, long prime, long primeExp, long cuberoot_k) {
        if (primeExp == 1) return cuberoot_prime(cuberoot_k, prime);
        long pk1 = (long) Math.pow(prime, primeExp - 1);
        var df = computeDf(f);
        var pk = BigInteger.valueOf(pk1).multiply(BigInteger.valueOf(prime));
        var recurse = hensel(f, prime, primeExp - 1, cuberoot_k);
        final var acc = new ArrayList<Long>();
        for (long n : recurse) {
            BigInteger dfn = polyval(df, BigInteger.valueOf(n), BigInteger.valueOf(prime));
            BigInteger fn = polyval(f, BigInteger.valueOf(n), pk);
            if (!fn.equals(BigInteger.ZERO)) {
                if (!dfn.equals(BigInteger.ZERO)) {
                    var pB = BigInteger.valueOf(prime);
                    var modInvValue = dfn.modInverse(pB);
                    acc.add(BigInteger.valueOf(n).add(fn.multiply(modInvValue.negate()).divide(BigInteger.valueOf(pk1)).mod(BigInteger.valueOf(prime)).multiply(BigInteger.valueOf(pk1))).longValue());
                }
            } else {
                for (int i = 0; i < prime; i++) {
                    acc.add(n + i * pk1);
                }
            }
        }
        return acc;
    }

    // @VisibleForTesting
    static BigInteger polyval(List<Long> coefs, BigInteger x, BigInteger m) {
        BigInteger out = BigInteger.ZERO;
        for (int i = coefs.size() - 1; i >= 0; i--) {
            var coef = BigInteger.valueOf(coefs.get(i));
            var multiplied = out.multiply(x);
            out = coef.add(multiplied).mod(m);
        }
        return out;
    }

    // @VisibleForTesting
    static List<Long> computeDf(List<Long> coefs) {
        var result = new ArrayList<Long>();
        for (int i = 1; i < coefs.size(); i++) {
            result.add(i * coefs.get(i));
        }
        return result;
    }

    /*
        From labmath method.
        def cbrtmod_prime(a, p):
        """
        Returns in a sorted list all cube roots of a mod p.  There are a bunch of
        easily-computed special formulae for various cases with p != 1 (mod 9); we
        do those first, and then if p == 1 (mod 9) we use Algorithm 4.2 in "Taking
        Cube Roots in Zm" by Padro and Saez, Applied Mathematics Letters 15 (2002)
        703-708, https://doi.org/10.1016/S0893-9659(02)00031-9, which is essentially
        a variation on the Tonelli-Shanks algorithm for modular square roots.
        Input: a, p -- Integers.  We assume that p is prime.
        Output: List of integers.
        Examples:
        >>> [cbrtmod_prime(a,11) for a in range(11)]
        [[0], [1], [7], [9], [5], [3], [8], [6], [2], [4], [10]]
        >>> [cbrtmod_prime(a,19) for a in range(11)]
        [[0], [1, 7, 11], [], [], [], [], [], [4, 6, 9], [2, 3, 14], [], []]
        """
        a %= p
        if a == 0 or p == 2 or p == 3: return [a % p]
        if p % 3 == 2: return [pow(a, (2*p-1)//3, p)]
        assert a != 0 and p % 3 == 1
        crs = pow(a, (p-1)//3, p)   # Cubic residue symbol.  There will be roots iff it's 1.
        if crs != 1: return []
        # There will be three roots.  Find one, and then compute the others by using a nontrivial root of unity.
        if p%9 != 1:    # There are simple formulae for the p == 4 and p == 7 mod 9 cases and for a nontrivial cube roots of unity.
            x, c = pow(a, (2*p+1)//9, p) if p%9 == 4 else pow(a, (p+2)//9, p), ( (-1 + sqrtmod_prime(-3, p)) * modinv(2, p) ) % p
            return sorted((x, (x*c)%p, (x*c*c)%p))
        # TODO: Optimize.
        e, q = 2, (p-1)//9
        while q % 3 == 0: q //= 3; e += 1
        # 1: Find h in Zp at random such that [h/p] != 1 mod p.
        for h in count(2):
            if pow(h, (p-1)//3, p) != 1: break
        # 2: Initialize.
        y = g = pow(h, q, p)
        r, s, x = e, pow(g, 3**(e-1), p), pow(a, (((-q)%3)*q-2)//3, p)
        b, x = ( pow(a*x, 2, p) * x ) % p, (a*x) % p
        while b % p != 1:
            for m in count():
                if pow(b, 3**m, p) == 1: break
            #if m == r: return [] # Our special cases above prevent this from happening.
            # 4: Reduce exponent.
            if s == pow(b, 3**(m-1), p): t, s = pow(y, 2, p), pow(s, 2, p)
         */
    private static List<Long> cuberoot_prime(long a, long p) {
        // REMARK: This can potentially be cached, but since we are caching the Hensel lifted versions,
        // we don't cache here.
        return cuberoot_prime_computation(a, p);
    }

    private static List<Long> cuberoot_prime_computation(long a, long p) {
        a = Math.floorMod(a, p);
        if (a == 0 || p == 2 || p == 3) return List.of(Math.floorMod(a, p));
        if (Math.floorMod(p, 3) == 2) {
            return List.of(GenericUtils.pow(a, (2 * p - 1) / 3, p));
        }
        long crs = GenericUtils.pow(a, (p - 1) / 3, p);

        if (crs != 1) return List.of();

        if (Math.floorMod(p, 9) != 1) {
            /**
             x, c = pow(a, (2*p+1)//9, p) if p%9 == 4 else pow(a, (p+2)//9, p), ( (-1 + sqrtmod_prime(-3, p)) * modinv(2, p) ) % p
             return sorted((x, (x*c)%p, (x*c*c)%p))
             */
            long x;
            long c;
            if (Math.floorMod(p, 9) == 4) {
                x = GenericUtils.pow(a, (2 * p + 1) / 9, p);
            } else {
                x = GenericUtils.pow(a, (p + 2) / 9, p);
            }
            // TODO: Why hard-code of -3 here?
            var squareRoot = GenericUtils.squareRootModuloPrime(BigInteger.valueOf(-3), BigInteger.valueOf(p)).longValue();
            c = (-1 + squareRoot) * GenericUtils.montgomery_inverse(2, p);
            c = Math.floorMod(c, p);
            return List.of(x, Math.floorMod(x * c, p), BigInteger.valueOf(x).multiply(BigInteger.valueOf(c)).multiply(BigInteger.valueOf(c)).mod(BigInteger.valueOf(p)).longValue());
        }

        long e = 2;
        long q = (p - 1) / 9;
        while (q % 3 == 0) {
            q /= 3;
            e += 1;
        }

        long h = 2;
        while (GenericUtils.pow(h, (p - 1) / 3, p) == 1) {
            h += 1;
        }

        long y = GenericUtils.pow(h, q, p);
        long g = y;

        long r = e;
        long s = GenericUtils.pow(g, (long) Math.pow(3, (e - 1)), p);
        long x = GenericUtils.pow(a, (((-q) % 3) * q - 2) / 3, p);
        long b = Math.floorMod((GenericUtils.pow(a * x, 2, p) * x), p);
        x = Math.floorMod((a * x), p);

        while (Math.floorMod(b, p) != 1) {
            int m;
            for (m = 0; ; m++) {
                if (GenericUtils.pow(b, (long) Math.pow(3, m), p) == 1) break;
            }
            long t;
            if (s == GenericUtils.pow(b, (long) Math.pow(3, (m - 1)), p)) {
                t = GenericUtils.pow(y, 2, p);
                s = GenericUtils.pow(s, 2, p);
            } else {
                t = y;
            }
            t = GenericUtils.pow(t, (long) Math.pow(3, (r - m - 1)), p);
            y = GenericUtils.pow(t, 3, p);

            r = m;
            x = Math.floorMod((x * t), p);
            b = Math.floorMod((b * y), p);
        }
        return List.of(x, Math.floorMod((x * s), p), Math.floorMod((x * s * s), p));
    }

    static int[] primes(final int numPrimes) {
        final Primes primes = Primes.load(numPrimes);
        return primes.getUnderlyingArray();
    }

    private static final Cache<Long, List<Long>> ssubdCache = Caffeine.newBuilder()
            .maximumSize((int)1e7)
            .build();

    static long[][][][] SsubdCacheFor3Values = Constants.getSsubDCache(3, false);
    static long[][][][] SsubdCacheFor33Values = Constants.getSsubDCache(33, false);
    static long[][][][] SsubdCacheFor42Values = Constants.getSsubDCache(42, false);
    static long[][][][] SsubdCacheFor165Values = Constants.getSsubDCache(165, false);

    static long[][][][] SsubdCacheFor3Bitmask = Constants.getSsubDCache(3, true);
    static long[][][][] SsubdCacheFor33Bitmask = Constants.getSsubDCache(33, true);
    static long[][][][] SsubdCacheFor42Bitmask = Constants.getSsubDCache(42, true);
    static long[][][][] SsubdCacheFor165Bitmask = Constants.getSsubDCache(165, true);

    static long[] isInSSubDCache(long dModP, long dMod3, long prime, int k) {
        final var primeIndex = Constants.primeToIndexLookup(prime);
        long[][][][] cacheToUse;
        switch (k) {
            case 3:
                cacheToUse = SsubdCacheFor3Bitmask;
                break;
            case 33:
                cacheToUse = SsubdCacheFor33Bitmask;
                break;
            case 42:
                cacheToUse = SsubdCacheFor42Bitmask;
                break;
            case 165:
                cacheToUse = SsubdCacheFor165Bitmask;
                break;
            default:
                throw new RuntimeException("Unknown k: " + k);
        }

        return cacheToUse[primeIndex][(int) dModP][(int) (dMod3)];
    }

    static boolean isInSSubD (long d, long prime, int k, long candidate) {
        final var dModP = Math.floorMod(d, prime);
        final long dMod3 = Math.floorMod(d, 3);
        final var primeIndex = Constants.primeToIndexLookup(prime);
        long[][][][] cacheToUse;
        switch (k) {
            case 3:
                cacheToUse = SsubdCacheFor3Bitmask;
                break;
            case 33:
                cacheToUse = SsubdCacheFor33Bitmask;
                break;
            case 42:
                cacheToUse = SsubdCacheFor42Bitmask;
                break;
            case 165:
                cacheToUse = SsubdCacheFor165Bitmask;
                break;
            default:
                throw new RuntimeException("Unknown k: " + k);
        }

        return cacheToUse[primeIndex][(int)dModP][(int)(dMod3)][(int)candidate] == 1;
    }

    static List<Long> Ssubd(long d, long prime, int k) {
        final var dModP = Math.floorMod(d, prime);
        final long dMod3 = Math.floorMod(d ,3);
        final var primeIndex = Constants.primeToIndexLookup(prime);
        long[][][][] cacheToUse;
        switch (k) {
            case 3:
                cacheToUse = SsubdCacheFor3Values;
                break;
            case 33:
                cacheToUse = SsubdCacheFor33Values;
                break;
            case 42:
                cacheToUse = SsubdCacheFor42Values;
                break;
            case 165:
                cacheToUse = SsubdCacheFor165Values;
                break;
            default:
                throw new RuntimeException("Unknown k: " + k);
        }

        var bitmapResponse =  Arrays.stream(cacheToUse[primeIndex][(int)dModP][(int)dMod3]).boxed().collect(Collectors.toList());
//        final var longKey = (dModP << 32) + (dMod3 << 45) + (k << 50) + prime;
//        var cachedResponse = ssubdCache.get(longKey, key -> Ssubd_computation(dModP, dMod3, prime, k));
//        var rawResponse = Ssubd_computation(dModP, dMod3, prime, k);
//        if (!bitmapResponse.equals(cachedResponse)) {
//            throw new RuntimeException(String.format("Bitmap response failed for d=%s,prime=%s,k=%s, cached: %s, bitmap: %s, (dModP, primeIndex, dMod3): (%s, %s, %s)", d, prime, k, cachedResponse, bitmapResponse, dModP, primeIndex, dMod3));
//        }
        return bitmapResponse;
    }

    static List<Long> Ssubd_computation(long dMpdP, long dModThree, long prime, int k) {
        final var dBigInt = BigInteger.valueOf(dMpdP);
        if (prime == 2) {
            return List.of((long) Math.floorMod(k + dMpdP, 2));
        }
        else {
            // REMARK: dMod3 = d^{(p-1)/2} mod p for p=3, so it is the legendre symbol
            final var s = Constants.getEps(k) * (dModThree == 2 ? -1 : 1);
            final var zs = new ArrayList<Long>();
            final var squaresModP = new HashSet<Long>();
            for (long i = 0; i < prime; i++) {
                squaresModP.add(Math.floorMod(i * i, prime));
            }

            for (long i=0;i<prime;i++) {
                final var dCubed = dBigInt.pow(3);
                final var iCubed = BigInteger.valueOf(i).pow(3);
                final var threed = dBigInt.multiply(BigInteger.valueOf(3));
                final var fours = BigInteger.valueOf(4 * s);
                final var zCubedMinusK = iCubed.subtract(BigInteger.valueOf(k));
                final var potentialSquare = threed.multiply(
                        (fours.multiply(zCubedMinusK)).subtract(dCubed)
                );
                if (squaresModP.contains(potentialSquare.mod(BigInteger.valueOf(prime)).longValueExact())) {
                    zs.add(i);
                }
            }
            return zs;
        }
    }

    static List<Long> cubicReciprocityConstraint(Records.NumberAndFactors d, long k) {
        if (k == 3) {
            final var first = BigInteger.valueOf(4 * GenericUtils.legendreSymbol(d.number(), 3) * d.number());
            final var dBigInt = BigInteger.valueOf(d.number());
            final var second = (dBigInt.multiply(dBigInt).subtract(ONE)).multiply(BigInteger.valueOf(3));
            // REMARK: Mod 81 rather than 162 to avoid the case of p^k being a power of 2.
            final var toMod = d.primeFactors().containsKey(2) ? 81 : 162;
            return List.of((first.add(second)).mod(BigInteger.valueOf(toMod)).longValue());
        } else {
            throw new RuntimeException("Not supported yet!");
        }
    }

    static LongIterator crt_enumeration(Pair<Records.NumberAndPower, List<Long>>[] numberToResidues) {
        var remainderPossibilities = new long[numberToResidues.length][];
        final var coprimeNumbers = new long[numberToResidues.length];

        var possibilities = 1;
        for (int i=0;i<numberToResidues.length;i++) {
            remainderPossibilities[i] =numberToResidues[i].getSecond().stream().mapToLong(l->l).toArray();
            coprimeNumbers[i] = numberToResidues[i].getFirst().numberToPower();
            possibilities = possibilities * numberToResidues[i].getSecond().size();
        }
        final var cartesianProductRemainders = new CartesianProductOfLongsIterator.Product(remainderPossibilities).iterator();

        return new LongIterator() {
            @Override
            public long nextLong() {
                return Utils.crt(cartesianProductRemainders.nextLongs(), coprimeNumbers);
            }

            @Override
            public boolean hasNext() {
                return cartesianProductRemainders.hasNext();
            }
        };
    }

    public static long crt_inductive(final long[] primes,
                                         final long[] remainders) {

        long modulus = primes[0];
        for (int i = 1; i < primes.length; ++i) {
            modulus = primes[i] * modulus;
        }

        return ThreadLocalRandom.current().nextLong(0, modulus);
//        long result = 0;
//        for (int i = 0; i < primes.length; ++i) {
//            long iModulus = modulus / primes[i];
//            final var ii = i;
//            long bezout = bezout0Cache.get(new Pair<>(iModulus, primes[i]), k -> bezout0_computation(iModulus, primes[ii]));
////            long bezout = bezout0_computation(iModulus, primes[i]);
//            final var bezoutRemainders = bezout * remainders[i];
//            final var bezoutRemainderModPrime = floorMod(bezoutRemainders, primes[i]);
//            final var iModulusMultiplyBezoutRemainderModPrime = iModulus * bezoutRemainderModPrime;
//            final var iModulusModModulus = floorMod(iModulusMultiplyBezoutRemainderModPrime, modulus);
//            final var resultPlusModulus = result + iModulusModModulus;
//
//            result = floorMod(resultPlusModulus, modulus);
//        }
//        return result;
    }


    private static final Cache<Pair<Long, Long>, Long> bezout0Cache = Caffeine.newBuilder()
            .maximumSize((int) 1e8)
            .build();

    private static long bezout0_computation(long a, long b) {
        long s = 0, old_s = 1;
        long r = b, old_r = a;

        long q;
        long tmp;
        while (r != 0) {
            q = old_r / r;

            tmp = old_r;
            old_r = r;
            r = subtractExact(tmp, multiplyExact(q, r));

            tmp = old_s;
            old_s = s;
            s = subtractExact(tmp, multiplyExact(q, s));
        }
        return old_s;
    }
}
