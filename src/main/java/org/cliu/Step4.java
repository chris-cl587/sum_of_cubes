package org.cliu;

import it.unimi.dsi.fastutil.longs.LongIterator;
import org.apache.commons.math3.util.Pair;

import java.util.List;

public class Step4 {

    /**
     * Computes step 4 of Algorithm 3.5 described in https://arxiv.org/pdf/2007.01209.pdf
     * First, uses CRT to compute the set Z_m identified Z/mZ.
     *
     * Second, keep adding multiples of `m` for all the candidates with |z| <= z_max.
     */
    public static void step4(long q, List<Long> Adq, int k, Models.NumberAndFactors d0, Models.NumberAndFactors d, Models.NumberAndFactors a, Models.NumberAndFactors b, long zMax) {
        var crtResponse = step4_CRT(q, Adq, k, d0, d, a);
        var m = crtResponse.m();
        var Zm = crtResponse.Zm();
        step4_ZmCheck(d, Zm, b, m, k, zMax);
    }

    record Step4CrtResponse(long m, LongIterator Zm) {}

    public static Step4CrtResponse step4_CRT(long q, List<Long> Adq, int k, Models.NumberAndFactors d0, Models.NumberAndFactors d, Models.NumberAndFactors a) {
        var m = d0.number() * q * a.number();

        // Hard-coded aux primes
        final var numberToResidues = new Pair[a.primeFactors.size() + d0.primeFactors().size() + 1];
        var i = 0;
        for (var pEntry:a.fastIter()) {
            numberToResidues[i] = new Pair<>(new Models.NumberAndPower(pEntry.getIntKey(), 1, pEntry.getIntKey()), Utils.SsubdP(d.number(), pEntry.getIntKey(), k));
            i++;
        }

        // Adq
        numberToResidues[i] = (new Pair<>(new Models.NumberAndPower(q, 1, q), Adq));
        i++;

        // Candidate power cube roots.
        for (var primeFactorEntry:d0.fastIter()) {
            final var power = d0.primeFactors().get(primeFactorEntry.getIntKey());
            final var pair = new Pair<>(new Models.NumberAndPower(primeFactorEntry.getIntKey(), power, (long)Math.pow(primeFactorEntry.getIntKey(), power)), Utils.henselCuberoot(primeFactorEntry.getIntKey(), power, k));
            numberToResidues[i] = pair;
            i++;
        }
        return new Step4CrtResponse(m, Utils.crtEnumeration(numberToResidues));
    }

    public static void step4_ZmCheck(Models.NumberAndFactors d, LongIterator Zm, Models.NumberAndFactors b, long m, int k, long zMax) {
//        final var start = Instant.now();
        final var primesInB = b.primeFactors().keySet();
//        var count = 0;

        var dMod3 = Math.floorMod(d.number(), 3);
        var multiplier = dMod3 == 2 ? -1 : 1;
        var multiplierM = multiplier * m;

        long[][] ssubdCandidateLookupTable = new long[251][];
        long[] multiplerMModBCache = new long[251];
        for(var pb: primesInB) {
            var dModP = Math.floorMod(d.number(), pb);
            ssubdCandidateLookupTable[pb - 1] = Utils.isInSSubDCache(dModP, dMod3, pb, k);
            multiplerMModBCache[pb-1] = Math.floorMod(multiplierM, pb);
        }
//        long toCheckEstimate = zMax / Math.abs(m);

        for (; Zm.hasNext(); ) {
            //            count += 1;
//            if (count % 1000 == 0 ) System.out.println("Checked " + count + " Z_m solutions!");
            var z = Zm.nextLong();
//            var zChecked = 0;
//            var squaresChecked = 0;
//            if (Instant.now().getEpochSecond() - start.getEpochSecond() > 60) {
//                System.out.println("Checked " + count + " Z_m solutions in about 60s!");
//                throw new RuntimeException("FOO!");
//            }

            while (Math.abs(z) < zMax) {
//                zChecked += 1;
//                if (zChecked % 10000 == 0) System.out.println("For a specific residue class, zChecked: " +zChecked + " out of ~" + toCheckEstimate);
                var shouldCheckSquare = true;
                for (var pb : primesInB) {
                    var zModP = floorMod(z, pb);
                    if (ssubdCandidateLookupTable[pb - 1][zModP] == 0) {
                        shouldCheckSquare = false;
                        break;
                    }
                }
                // Check square
                if (shouldCheckSquare) {
//                    squaresChecked += 1;
                    if (GenericUtils.isSquareCandidate(d.number(), z, k)) {
                        throw new SquareFoundException(String.format("FOUND SQUARE!! d:%s,z:%s", d, z));
                    }
                }
                z = z + multiplierM;
            }
//            System.out.println(String.format("Checked %s zs, %s squares, %s percent", zChecked, squaresChecked, 100.0 * squaresChecked/zChecked));
        }
    }

    static int floorMod(long x, int y) {
        // TODO: Faster floorMod method may be possible using Barret Reduction here.
        //  Profile and optimize.
        return Math.floorMod(x,y);
    }
}
