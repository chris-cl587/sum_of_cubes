import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Enumeration {
    // Recursively enumerate all positive integers using prime powers. Each prime must be congruent to 0 mod 3.
    static ConcurrentLinkedDeque<Records.NumberAndFactors> nSmoothEnumerationRecursive(ConcurrentLinkedDeque<Records.NumberAndFactors> acc, long min, long max, long[] primes, Records.NumberAndFactors initialNumber, int startIndex) {
        if (startIndex < 0) return acc;
        nSmoothEnumerationRecursive(acc, min, max, primes, initialNumber, startIndex - 1);
        var iNumber = initialNumber.multiply(primes[startIndex]);
        while(true) {
            if (iNumber.number().compareTo(BigInteger.valueOf(max)) > 0) break;
            if (iNumber.number().compareTo(BigInteger.valueOf(min)) > 0) acc.add(iNumber);
            nSmoothEnumerationRecursive(acc, min, max, primes, iNumber, startIndex - 1);
            iNumber = iNumber.multiply(primes[startIndex]);
        }
        return acc;
    }

    // Same as above recursion, but using iteration for memory reasons.
    static List<Records.NumberAndFactors> nSmoothEnumerationIteration(long min, long max, long[] primes, Records.NumberAndFactors initialNumber, int maxNum, int startIdx) {
        final var minB = BigInteger.valueOf(min);
        final var maxB = BigInteger.valueOf(max);
        final var acc = new ArrayList<Records.NumberAndFactors>();
        if (initialNumber.number().compareTo(minB) > 0 && initialNumber.number().compareTo(maxB) < 0) acc.add(initialNumber);

        List<Records.NumberAndFactors> candidates = new ArrayList<>();
        candidates.add(initialNumber);

        for (int i=startIdx;i>=0;i--) {
            if (primes[i] % 3 ==0
                    // HACK: This approach didn't seem to scale and OOM'ed without this filter.
                    || primes[i] < 30) continue;
            List<Records.NumberAndFactors> newCandidates = new ArrayList<>();
            for (var n : candidates) {
                var jNumber = n;
                while(true) {
                    jNumber = jNumber.multiply(primes[i]);
                    if (jNumber.number().compareTo(maxB) > 0) break;
                    if (i == 0 || jNumber.multiply(primes[i - 1]).number().compareTo(maxB) < 0) newCandidates.add(jNumber);
                    if (jNumber.number().compareTo(minB) > 0) {
                        acc.add(jNumber);
                    }
                    if (acc.size() >= maxNum)  {
                        return acc;
                    }
                }
            }
            candidates.addAll(newCandidates);
//            if (i % 10000 == 0 || i < 1000){
//                System.out.println("i: " + i + " acc.length: " + acc.size() + " Candidates length: " + candidates.size());
//            }
        }
        return acc;
    }
}


