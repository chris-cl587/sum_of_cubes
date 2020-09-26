package org.cliu;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;

public class Step3 {
    /**
     * Computes step 3 of Algorithm 3.5 described in https://arxiv.org/pdf/2007.01209.pdf
     *
     * "Let b be the product of c2 primes p ∈ A not dividing da, chosen either using the
     * ordering computed in the previous step or a fixed order."
     */
    public static Models.NumberAndFactors step3(Models.NumberAndFactors d, Models.NumberAndFactors a, long c2) {
        var b = new Models.NumberAndFactors(1L, new Int2IntArrayMap(4));
        for (int i = 0; i< Constants.A.size(); i++) {
            if (b.primeFactors().size() > c2) break;
            final var prime = Constants.A.get(i);
            if (d.primeFactors().containsKey(prime) || a.primeFactors().containsKey(prime)) continue;
            if (null == b.multiply(prime)) continue;
            b.multiplyMutable(prime);
        }
        return b;
    }
}
