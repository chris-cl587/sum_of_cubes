package org.cliu;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import org.apache.commons.math3.util.Pair;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Step2 {
    /**
     * Computes step 2 of Algorithm 3.5 described in https://arxiv.org/pdf/2007.01209.pdf
     *
     * This bumps d0 up such that eventually in step 4, the mod "m" class is bigger so we have less
     * candidate values to check.
     */
    public static Records.NumberAndFactors step2(Records.NumberAndFactors d0, int q, int k) {
        // Set a := 1, and if c1qd0 < zmax then order the p |- d in A by log #Sd(p)/ log p, and while
        //c0qd0pa < zmax replace a by pa, where p is the next prime in the ordering.
        var a = new Records.NumberAndFactors(1L, new Int2IntArrayMap(4));
        var c1Prod = d0.multiply(Constants.c1 * q);
        if (c1Prod == null || c1Prod.number() > Constants.zMax) return a;

        final List<Pair<Integer, Double>> logSquareOverLogP = new ArrayList<>();
        for (var p: Constants.A) {
            if (d0.primeFactors().containsKey(p.intValue())) continue;
            final var Sdp = Math.log(Utils.Ssubd(d0.number(), p.intValue(), k).size()) / Math.log(p.intValue());
            logSquareOverLogP.add(new Pair<>(p.intValue(), Sdp));
        }
        logSquareOverLogP.sort(Comparator.comparing(Pair::getSecond));

        for (int i=0;i<logSquareOverLogP.size();i++) {
            final var prime = logSquareOverLogP.get(i);
            var c0Prod = d0.multiply(Constants.c0 * prime.getFirst() * a.number());
            if (null == c0Prod || c0Prod.number() > Constants.zMax || null == a.multiply(prime.getFirst())) break;
            a = a.multiply(prime.getFirst());
        }
        return a;
    }
}
