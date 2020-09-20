package org.cliu;

import org.apache.commons.math3.util.Pair;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Step2 {
    public static Records.NumberAndFactors step2(Records.NumberAndFactors d0, int q, int k) {
        // Set a := 1, and if c1qd0 < zmax then order the p |- d in A by log #Sd(p)/ log p, and while
        //c0qd0pa < zmax replace a by pa, where p is the next prime in the ordering.
        var a = new Records.NumberAndFactors(BigInteger.ONE, Map.of());
        var c1Prod = d0.number().multiply(BigInteger.valueOf(Constants.c1 * q));
        if (c1Prod.compareTo(BigInteger.valueOf(Constants.zMax)) > 0) return a;

        final List<Pair<Integer, Double>> logSquareOverLogP = new ArrayList<>();
        for (var p: Constants.A) {
            if (d0.primeFactors().containsKey(p)) continue;
            final var Sdp = Math.log(Utils.Ssubd(d0.number().longValue(), p, k).size()) / Math.log(p);
            logSquareOverLogP.add(new Pair<>(p, Sdp));
        }
        logSquareOverLogP.sort(Comparator.comparing(Pair::getSecond));

        for (int i=0;i<logSquareOverLogP.size();i++) {
            final var prime = logSquareOverLogP.get(i);
            var c0Prod = d0.number().multiply(BigInteger.valueOf(Constants.c0 * prime.getFirst() * a.number().longValueExact()));
            if (c0Prod.compareTo(BigInteger.valueOf(Constants.zMax)) > 0) break;
            a = a.multiply(prime.getFirst());
        }
        return a;
    }
}
