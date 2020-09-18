import cc.redberry.rings.ChineseRemainders;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class Step4 {

    public static void step4(long q, List<Long> Adq, int k, Records.NumberAndFactors d0, Records.NumberAndFactors d, Records.NumberAndFactors a, Records.NumberAndFactors b) {
        var crtResponse = step4_CRT(q, Adq, k, d0, d, a);
        var m = crtResponse.m();
        var Zm = crtResponse.Zm();
        step4_ZmCheck(d, Zm, b, m, k);
    }

    record Step4CrtResponse(long m, List<Long> Zm) {}
    public static Step4CrtResponse step4_CRT(long q, List<Long> Adq, int k, Records.NumberAndFactors d0, Records.NumberAndFactors d, Records.NumberAndFactors a) {
        var m = d0.number() * q * a.number();

        // Hard-coded aux primes
        final var numberToResidues = new ArrayList<Pair<Records.NumberAndPower, List<Long>>>();
        for (var p:a.primeFactors().keySet()) {
            numberToResidues.add(new Pair<>(new Records.NumberAndPower((long) p, 1), Utils.Ssubd(d.number(), p, k)));
        }

        // Adq
        numberToResidues.add(new Pair<>(new Records.NumberAndPower(q, 1), Adq));

        // Candidate power cube roots.
        for (var primeFactor:d0.primeFactors().keySet()) {
            final var power = d0.primeFactors().get(primeFactor);
            final var pair = new Pair<>(new Records.NumberAndPower(primeFactor, power), Utils.hensel_cuberoot(primeFactor, power, k));
            numberToResidues.add(pair);
        }
        return new Step4CrtResponse(m, Utils.crt_enumeration(numberToResidues));
    }

    public static void step4_ZmCheck(Records.NumberAndFactors d, List<Long> Zm, Records.NumberAndFactors b, long m, int k) {
        final var primesInB = b.primeFactors().keySet();
        var multiplier = Constants.eps * GenericUtils.legendreSymbol(d.number(), 3);
        final var expectedZm = 543208221177868L;
        for(long l: Zm) {
            if (Math.floorMod(-472715493453327032L, m) == l) {
                throw new RuntimeException("Zm candidate " + l + " in same residue class as answer!");
            }
            var z = l;
            while (Math.abs(z) < Constants.zMax) {
                var shouldCheckSquare = true;
                for (var pb:primesInB) {
                    var zModP = Long.valueOf(Math.floorMod(z, pb));
                    if (!Utils.Ssubd(d.number(), pb, k).contains(zModP)) {
                        shouldCheckSquare = false;
                        break;
                    }
                }
                // Check square
                if (shouldCheckSquare) {
                    if (GenericUtils.isSquareCandidate(d.number(), z, k)) {
                        throw new RuntimeException(String.format("FOUND SQUARE!! d:%s,z:%s", d, z));
                    }
                }
                z = z + (multiplier * m);
            }
        }
    }
}
