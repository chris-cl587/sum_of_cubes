package org.cliu;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.scravy.primes.Primes;
import it.unimi.dsi.fastutil.longs.LongIterator;
import org.apache.commons.math3.util.Pair;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

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

    static Long crt(long[] rems, long[] coprimes) {
        // START INDUCTIVE SOLUTION HERE - This looks to be faster than the one-shot constructive
        // solution due to being able to avoid using arbitrary precision integers.
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

    static Cache<String, List<Long>> henselCuberootCache = Caffeine.newBuilder()
            .maximumSize((long)1e8)
            .build();

    /**
     * Computes the cuberoot of k mod p^e.
     * Does so in a cached fashion to avoid re-computing these values over and over.
     */
    public static List<Long> henselCuberoot(long prime, long primeExp, long cuberoot_k) {
        var cacheKey = prime + "~" + primeExp + "~" + cuberoot_k;
        return henselCuberootCache.get(cacheKey, key -> hensel(List.of(-cuberoot_k, 0L, 0L, 1L), prime, primeExp, cuberoot_k));
    }

    // @VisibleForTesting
    static List<Long> hensel(List<Long> f, long prime, long primeExp, long cuberoot_k) {
        if (primeExp == 1) return cuberootOfPrime(cuberoot_k, prime);
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

    // REMARK: This can potentially be cached, but since we are caching the Hensel lifted versions,
    // we don't cache here.
    // This algorithm is from ALgorithm 4.2 of https://doi.org/10.1016/S0893-9659(02)00031-9
    // Original reference impl from the MathLab library.
    private static List<Long> cuberootOfPrime(long a, long p) {
        a = Math.floorMod(a, p);
        if (a == 0 || p == 2 || p == 3) return List.of(Math.floorMod(a, p));
        if (Math.floorMod(p, 3) == 2) {
            return List.of(GenericUtils.pow(a, (2 * p - 1) / 3, p));
        }
        long crs = GenericUtils.pow(a, (p - 1) / 3, p);

        if (crs != 1) return List.of();

        if (Math.floorMod(p, 9) != 1) {
            long x;
            long c;
            if (Math.floorMod(p, 9) == 4) {
                x = GenericUtils.pow(a, (2 * p + 1) / 9, p);
            } else {
                x = GenericUtils.pow(a, (p + 2) / 9, p);
            }
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


    // TODO: These are static arrays that need to be constructed on load-time, instead of being produced lazily.
    static long[][][][] SsubdCacheFor3Values = Constants.getSsubDCache(3, false);
    static long[][][][] SsubdCacheFor33Values = Constants.getSsubDCache(33, false);
    static long[][][][] SsubdCacheFor42Values = Constants.getSsubDCache(42, false);
    static long[][][][] SsubdCacheFor165Values = Constants.getSsubDCache(165, false);

    static long[][][][] SsubdCacheFor3Bitmask = Constants.getSsubDCache(3, true);
    static long[][][][] SsubdCacheFor33Bitmask = Constants.getSsubDCache(33, true);
    static long[][][][] SsubdCacheFor42Bitmask = Constants.getSsubDCache(42, true);
    static long[][][][] SsubdCacheFor165Bitmask = Constants.getSsubDCache(165, true);

    // Produces an array that caches SsubDP lookups, where the candidate mod P just needs to be indexed into the array.
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

    static List<Long> SsubdP(long d, long prime, int k) {
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
        return bitmapResponse;
    }

    static List<Long> SsubdPComputation(long dMpdP, long dModThree, long prime, int k) {
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

    // This is an attempt to implement 3.1-3.3 of the `On a question of Mordell` paper.
    // We currently only have the mod 162 constraints for the `k=3` case.
    static List<Long> cubicReciprocityConstraint(Models.NumberAndFactors d, long k) {
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

    // Produces an enumeration of the candidate `Zm` values using CRT and the possible residues.
    // This enumeration is lazy due to the cartesian product possibly expanding out to many rows.
    static LongIterator crtEnumeration(Pair<Models.NumberAndPower, List<Long>>[] numberToResidues) {

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

    // Inductive solution as per description in https://en.wikipedia.org/wiki/Chinese_remainder_theoremhttps://en.wikipedia.org/wiki/Chinese_remainder_theorem
    // Original implementation from the `Rings` library, revised to cache te lookups to take advantage
    // of the structure of the problem.
    public static long crt_inductive(final long[] primes,
                                         final long[] remainders) {
        long modulus = primes[0];
        for (int i = 1; i < primes.length; ++i) {
            modulus = primes[i] * modulus;
        }
//        return 1;
//        return ThreadLocalRandom.current().nextLong(0, modulus);
        long result = 0;
        for (int i = 0; i < primes.length; ++i) {
            long iModulus = modulus / primes[i];
            final var ii = i;
            long bezout = bezout0Cache.get(new Pair<>(iModulus, primes[i]), k -> bezoutComputation(iModulus, primes[ii]));
//            long bezout = bezout0_computation(iModulus, primes[i]);
            final var bezoutRemainders = bezout * remainders[i];
            final var bezoutRemainderModPrime = floorMod(bezoutRemainders, primes[i]);
            final var iModulusMultiplyBezoutRemainderModPrime = iModulus * bezoutRemainderModPrime;
            final var iModulusModModulus = floorMod(iModulusMultiplyBezoutRemainderModPrime, modulus);
            final var resultPlusModulus = result + iModulusModModulus;

            result = floorMod(resultPlusModulus, modulus);
        }
        return result;
    }


    private static final Cache<Pair<Long, Long>, Long> bezout0Cache = Caffeine.newBuilder()
            .maximumSize((int) 1e8)
            .build();

    // TODO: Maybe optimize?
    private static long bezoutComputation(long a, long b) {
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
