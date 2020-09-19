import cc.redberry.rings.ChineseRemainders;
import de.scravy.primes.Primes;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Pair;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.math.BigInteger.ONE;
import static java.util.stream.Collectors.toList;

public class Utils {

    // the jacobi function uses this lookup table
    static final int[] jacobiTable = {0, 1, 0, -1, 0, -1, 0, 1};


    /**
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
    static Long crt(List<Long> rems, List<Long> primes, List<Integer> pows) {
        BigInteger N = BigInteger.valueOf(1L);
        // TODO: Can overflow here.
        for (int i = 0; i < primes.size(); i++) N = N.multiply(BigInteger.valueOf(primes.get(i)).pow(pows.get(i)));

        BigInteger acc = BigInteger.ZERO;
        for (int i = 0; i < rems.size(); i++) {
            long r = rems.get(i);
            long p = primes.get(i);
            long e = pows.get(i);
            long m = (long) Math.pow(p, e);
            final var mBigInt = BigInteger.valueOf(m);
            BigInteger N_over_m = N.divide(mBigInt);
            long inv = N_over_m.modInverse(mBigInt).longValue();
            long invOpt = GenericUtils.inverse_mod_prime_power(N_over_m.longValue(), p, e);
            if (inv < 0) {
                inv += m;
            }
            if (inv != invOpt) {
//                System.err.println("Optimized inverse didn't get the same result!");
            }
            var toAdd = BigInteger.valueOf(r).multiply(N_over_m).multiply(BigInteger.valueOf(inv));
//            System.out.println(String.format("r=%s,p=%s,e=%s,m=%s,N/m=%s,inv=%s", r, p, e, m, N_over_m, inv));
            acc = acc.add(toAdd);
        }
//        System.out.println(String.format("acc:%s,N:%s", acc, N));
        return acc.mod(N).longValue();
    }

    /*
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
    static List<Long> hensel_cuberoot(long p, long k, long cuberoot_k) {
        return hensel(List.of(-cuberoot_k, 0L, 0L, 1L), p, k, cuberoot_k);
    }

    static List<Long> hensel(List<Long> f, long p, long k, long cuberoot_k) {
        if (k == 1) return cuberoot_prime(cuberoot_k, p);
        long pk1 = (long) Math.pow(p, k - 1);
        var df = computeDf(f);
        var pk = BigInteger.valueOf(pk1).multiply(BigInteger.valueOf(p));
        var recurse = hensel(f, p, k - 1, cuberoot_k);
        final var acc = new ArrayList<Long>();
        for (long n : recurse) {
            BigInteger dfn = polyval(df, BigInteger.valueOf(n), BigInteger.valueOf(p));
            BigInteger fn = polyval(f, BigInteger.valueOf(n), pk);
//            System.out.println("n=" + n + ",dfn=" + dfn + ",fn=" + fn + ",pk=" + pk + ",pk1=" + pk1);
            if (!fn.equals(BigInteger.ZERO)) {
                if (!dfn.equals(BigInteger.ZERO)) {
                    var pB = BigInteger.valueOf(p);
                    var modInvValue = dfn.modInverse(pB);
                    acc.add(BigInteger.valueOf(n).add(fn.multiply(modInvValue.negate()).divide(BigInteger.valueOf(pk1)).mod(BigInteger.valueOf(p)).multiply(BigInteger.valueOf(pk1))).longValue());
                }
            } else {
                for (int i = 0; i < p; i++) {
                    acc.add(n + i * pk1);
                }
            }
        }
        return acc;
    }

    static BigInteger polyval(List<Long> coefs, BigInteger x, BigInteger m) {
        BigInteger out = BigInteger.ZERO;
        for (int i = coefs.size() - 1; i >= 0; i--) {
            var coef = BigInteger.valueOf(coefs.get(i));
            var multiplied = out.multiply(x);
            out = coef.add(multiplied).mod(m);
        }
        return out;
    }

    static List<Long> computeDf(List<Long> coefs) {
        var result = new ArrayList<Long>();
        for (int i = 1; i < coefs.size(); i++) {
            result.add(i * coefs.get(i));
        }
        return result;
    }

    /*
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
    static List<Long> cuberoot_prime(long a, long p) {
        a = Math.floorMod(a, p);
        if (a == 0 || p == 2 || p == 3) return List.of(Math.floorMod(a, p));
        if (Math.floorMod(p, 3) == 2) {
//            System.out.println("mod 3 = 2 case, a=" + a + ",p=" + p);
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
            var squareRoot = GenericUtils.squreRootModuloPrime(BigInteger.valueOf(-3), BigInteger.valueOf(p)).longValue();
            c = (-1 + squareRoot) * GenericUtils.montgomery_inverse(2, p);
            c = Math.floorMod(c, p);
//            System.out.println("x=" + x + ",c=" + c + ",squareRoot=" + squareRoot);
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

    static List<Long> cube_solutions(List<Long> primes, List<Integer> powers) {
        long cuberoot_k = 3;
        var primeToRemainders = new HashMap<Long, List<Long>>();
        for (int i = 0; i < primes.size(); i++) {
            var modP_power_remainders = new ArrayList<Long>();
            var prime = primes.get(i);
            var power = powers.get(i);
            modP_power_remainders.addAll(hensel_cuberoot(prime, power, cuberoot_k));
//            System.out.println(String.format("p: %s, modP_power_remainders: %s", prime, modP_power_remainders));
            primeToRemainders.put(prime, modP_power_remainders);
        }

//        System.out.println("primeToRemainders: " + primeToRemainders);

        final List<List<Long>> possibleRemainders = GenericUtils.cartesianProduct(new ArrayList<>(primeToRemainders.values()));

        return possibleRemainders.stream().map(r -> crt(r, primes, powers)).collect(toList());
    }

    static int[] primes(final int numPrimes) {
        final Primes primes = Primes.load(numPrimes);
        return primes.getUnderlyingArray();
    }

    static List<Long> Ssubd(long d, long prime, int k) {
        final var dBigInt = BigInteger.valueOf(d);
        if (prime == 2) {
            return List.of((long) Math.floorMod(k + d, 2));
        }
        else {
            final var s = Constants.eps * GenericUtils.legendreSymbol(d, 3);
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
                if (squaresModP.contains(potentialSquare.mod(BigInteger.valueOf(prime)).longValue())) {
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
            return List.of((first.add(second)).mod(BigInteger.valueOf(162)).longValue());
        } else {
            throw new RuntimeException("Not supported yet!");
        }
    }

    static Stream<Long> crt_enumeration(List<Pair<Records.NumberAndPower, List<Long>>> numberToResidues) {
        final var remainderPossibilities = numberToResidues.stream().map(Pair::getSecond).collect(Collectors.toList());
        final var coprimeNumbers = numberToResidues.stream().map(i -> i.getFirst().number()).collect(Collectors.toList());
        final var coprimePowers = numberToResidues.stream().map(i -> i.getFirst().power()).collect(Collectors.toList());

//        final List<List<Long>> possibleRemainders = GenericUtils.cartesianProduct(remainderPossibilities);
        final Iterable<Long[]> cartesianProductRemainders = CartesianProductIterator.product(Long.class, remainderPossibilities);

        final cc.redberry.rings.bigint.BigInteger[] coprimeRaisedNumbers = new cc.redberry.rings.bigint.BigInteger[coprimeNumbers.size()];
        for (int i=0;i<coprimeRaisedNumbers.length;i++) {
            coprimeRaisedNumbers[i] = cc.redberry.rings.bigint.BigInteger.valueOf(coprimeNumbers.get(i)).pow(coprimePowers.get(i));
        }

        final var cartesianProductStream = StreamSupport.stream(cartesianProductRemainders.spliterator(), false);
        return cartesianProductStream.map(r -> {
            final var rList = Arrays.asList(r);
            final cc.redberry.rings.bigint.BigInteger[] rBigInts = rList.stream().map(cc.redberry.rings.bigint.BigInteger::valueOf).toArray(cc.redberry.rings.bigint.BigInteger[]::new);
//            final var manualCRT = crt(rList, coprimeNumbers, coprimePowers);
            try {
                final var ringsCRT = ChineseRemainders.ChineseRemainders(coprimeRaisedNumbers, rBigInts).longValue();
                return ringsCRT;
            } catch (Exception e){
                throw e;
            }
//            if (manualCRT != ringsCRT) {
//                System.err.println("Manual CRT got wrong results");
//            }
        });
    }
}
