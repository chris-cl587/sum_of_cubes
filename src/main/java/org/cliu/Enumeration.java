package org.cliu;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Enumeration {
    // Recursively enumerate all positive integers using prime powers. Each prime must be congruent to 0 mod 3.
    // NOT USED - THIS WILL BLOW THE STACK IMMEDIATELY.
//    static ConcurrentLinkedDeque<Records.NumberAndFactors> nSmoothEnumerationRecursive(ConcurrentLinkedDeque<Records.NumberAndFactors> acc, long min, long max, long[] primes, Records.NumberAndFactors initialNumber, int startIndex) {
//        if (startIndex < 0) return acc;
//        nSmoothEnumerationRecursive(acc, min, max, primes, initialNumber, startIndex - 1);
//        var iNumber = initialNumber.multiply(primes[startIndex]);
//        while(true) {
//            if (iNumber.number().compareTo(BigInteger.valueOf(max)) > 0) break;
//            if (iNumber.number().compareTo(BigInteger.valueOf(min)) > 0) acc.add(iNumber);
//            nSmoothEnumerationRecursive(acc, min, max, primes, iNumber, startIndex - 1);
//            iNumber = iNumber.multiply(primes[startIndex]);
//        }
//        return acc;
//    }

    // Same as above recursion, but using iteration.
    static List<Models.NumberAndFactors> nSmoothEnumerationIteration(long min, long max, long[] primes, Models.NumberAndFactors initialNumber, int maxNum, int startIdx) {
        final var minB = BigInteger.valueOf(min);
        final var maxB = BigInteger.valueOf(max);
        final var acc = new ArrayList<Models.NumberAndFactors>();
        if (initialNumber.number() > min && initialNumber.number() < max) acc.add(initialNumber);

        List<Models.NumberAndFactors> candidates = new ArrayList<>();
        candidates.add(initialNumber);

        for (int i=startIdx;i>=0;i--) {
            if (primes[i] % 3 ==0
                    // HACK: This approach didn't seem to scale and OOM'ed without this filter.
                    || primes[i] < 30) continue;
            List<Models.NumberAndFactors> newCandidates = new ArrayList<>();
            for (var n : candidates) {
                var jNumber = n.copy();
                while(true) {
                    final var nextCandidate = Models.NumberAndFactors.multiplyPositivesOrReturnNegativeOne(jNumber.number(),primes[i]);

                    jNumber.multiplyMutable(primes[i]);
                    if (-1 == nextCandidate || nextCandidate > max) break;

                    if (i == 0 || (-1 != Models.NumberAndFactors.multiplyPositivesOrReturnNegativeOne(nextCandidate, primes[i-1]) && Models.NumberAndFactors.multiplyPositivesOrReturnNegativeOne(nextCandidate, primes[i-1]) < max))
                        newCandidates.add(jNumber.copy());
                    if (nextCandidate > min) {
                        acc.add(jNumber.copy());
                    }
                    if (acc.size() >= maxNum)  {
                        return acc;
                    }
                }
            }
            candidates.addAll(newCandidates);
        }
        return acc;
    }
}


