package org.cliu;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Step3 {
    /**
     * Computes step 3 of Algorithm 3.5 described in https://arxiv.org/pdf/2007.01209.pdf
     *
     * "Let b be the product of c2 primes p ∈ A not dividing da, chosen either using the
     * ordering computed in the previous step or a fixed order."
     */
    public static Records.NumberAndFactors step3(Records.NumberAndFactors d, Records.NumberAndFactors a) {
        var b = new Records.NumberAndFactors(BigInteger.ONE, new Int2IntArrayMap(4));
        for (int i = 0; i< Constants.A.size(); i++) {
            if (b.primeFactors().size() > Constants.c2) break;
            final var prime = Constants.A.get(i);
            if (d.primeFactors().containsKey(prime.intValue()) || a.primeFactors().containsKey(prime.intValue())) continue;
            b.multiplyMutable(Constants.A.get(i));
        }
        return b;
    }
}
