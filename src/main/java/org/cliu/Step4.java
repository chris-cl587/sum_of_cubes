package org.cliu;

import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import org.apache.commons.math3.util.Pair;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class Step4 {

    /**
     * Computes step 4 of Algorithm 3.5 described in https://arxiv.org/pdf/2007.01209.pdf
     * First, uses CRT to compute the set Z_m identified Z/mZ.
     *
     * Second, keep adding multiples of `m` for all the candidates with |z| <= z_max.
     */
    public static void step4(long q, List<Long> Adq, int k, Records.NumberAndFactors d0, Records.NumberAndFactors d, Records.NumberAndFactors a, Records.NumberAndFactors b, long zMax) {
        var crtResponse = step4_CRT(q, Adq, k, d0, d, a);
        var m = crtResponse.m();
        var Zm = crtResponse.Zm();
        step4_ZmCheck(d, Zm, b, m, k, zMax);
    }

    record Step4CrtResponse(long m, Stream<Long>Zm) {}

    public static Step4CrtResponse step4_CRT(long q, List<Long> Adq, int k, Records.NumberAndFactors d0, Records.NumberAndFactors d, Records.NumberAndFactors a) {
        var m = d0.number() * q * a.number();

        // Hard-coded aux primes
        final var numberToResidues = new Pair[a.primeFactors.size() + d0.primeFactors().size() + 1];
        var i = 0;
        for (var pEntry:a.fastIter()) {
            numberToResidues[i] = new Pair<>(new Records.NumberAndPower(pEntry.getIntKey(), 1, pEntry.getIntKey()), Utils.Ssubd(d.number(), pEntry.getIntKey(), k));
            i++;
        }

        // Adq
        numberToResidues[i] = (new Pair<>(new Records.NumberAndPower(q, 1, q), Adq));
        i++;

        // Candidate power cube roots.
        for (var primeFactorEntry:d0.fastIter()) {
            final var power = d0.primeFactors().get(primeFactorEntry.getIntKey());
            final var pair = new Pair<>(new Records.NumberAndPower(primeFactorEntry.getIntKey(), power, (long)Math.pow(primeFactorEntry.getIntKey(), power)), Utils.hensel_cuberoot(primeFactorEntry.getIntKey(), power, k));
            numberToResidues[i] = pair;
            i++;
        }
        return new Step4CrtResponse(m, Utils.crt_enumeration(numberToResidues));
    }

    public static void step4_ZmCheck(Records.NumberAndFactors d, Stream<Long> Zm, Records.NumberAndFactors b, long m, int k, long zMax) {
        final var start = Instant.now();
        final var primesInB = b.primeFactors();
        var multiplier = Constants.getEps(k) * GenericUtils.legendreSymbol(d.number(), 3);
        var count = 0;

        var max = Collections.max(primesInB.keySet());
        var dMod3 = Math.floorMod(d.number(), 3);
        long[][] ssubdCandidateLookupTable = new long[max][];
        for(var pb: Int2IntMaps.fastIterable(primesInB)) {
            var dModP = Math.floorMod(d.number(), pb.getIntKey());
            ssubdCandidateLookupTable[pb.getIntKey() - 1] = Utils.isInSSubDCache(dModP, dMod3, pb.getIntKey(), k);
        }
        long toCheckEstimate = zMax / Math.abs(m);

        var reported = false;
        for (Iterator<Long> it = Zm.iterator(); it.hasNext(); ) {
            long l = it.next();
            count += 1;
            if (count % 1000 == 0 ) System.out.println("Checked " + count + " Z_m solutions!");
            if (count == 25000000) {
                System.out.println("Checked 25m solutions in " + (Instant.now().getEpochSecond() - start.getEpochSecond()) + " seconds!");
//                throw new RuntimeException("ABORT!");
            }
            var z = l;
            var zChecked = 0;
            var squaresChecked = 0;
            if (!reported && Instant.now().getEpochSecond() - start.getEpochSecond() > 60) {
                System.out.println("Checked " + count + " Z_m solutions in about 60s!");
                reported = true;
                throw new RuntimeException("FOO!");
            }

            while (Math.abs(z) < zMax) {
                zChecked += 1;
                if (zChecked % 10000 == 0) System.out.println("For a specific residue class, zChecked: " +zChecked + " out of ~" + toCheckEstimate);
                var shouldCheckSquare = true;
                for (var pb : Int2IntMaps.fastIterable(primesInB)) {
                    var zModP = Math.floorMod(z, pb.getIntKey());
                    if (ssubdCandidateLookupTable[pb.getIntKey() - 1][zModP] == 0) {
                        shouldCheckSquare = false;
                        break;
                    }
                }
                // Check square
                if (shouldCheckSquare) {
                    squaresChecked += 1;
                    if (GenericUtils.isSquareCandidate(d.number(), z, k)) {
                        throw new SquareFoundException(String.format("FOUND SQUARE!! d:%s,z:%s", d, z));
                    }
                }
                z = z + (multiplier * m);
            }
//            System.out.println(String.format("Checked %s zs, %s squares, %s percent", zChecked, squaresChecked, 100.0 * squaresChecked/zChecked));
        }
    }
}
