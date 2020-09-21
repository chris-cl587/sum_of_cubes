package org.cliu;

import java.util.List;

public class Step1 {
    record Step1Response(long q, Records.NumberAndFactors d, List<Long> Adq) {};

    /**
     * Computes step 1 of Algorithm 3.5 described in https://arxiv.org/pdf/2007.01209.pdf
     *
     * Find admissible values mod `q` which helps reduce computation as in the CRT step
     * this reduces the possible candidate values.
     */
    public static Step1Response step1(Records.NumberAndFactors d0, int k) {
        // For each positive divisor d1 of k/3 with gcd(d1, k/d1) = 1, set d := d0d1 and let Ad(q)
        //be the set of z + qZ for which (d, z) is admissible.
        if (k == 3) {
            return new Step1Response(d0.primeFactors().containsKey(2) ? 81 : 162, d0, Utils.cubicReciprocityConstraint(d0, k));
        } else {
            // We don't support using cubic reciprocity constraints for k != 3 yet, need more research.
            return new Step1Response(1, d0, List.of(0L));
        }
    }
}
