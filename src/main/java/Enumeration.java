import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class Enumeration {
    static ConcurrentLinkedDeque<Records.NumberAndFactors> nSmoothEnumerationRecursive(ConcurrentLinkedDeque<Records.NumberAndFactors> acc, long min, long max, long[] primes, Records.NumberAndFactors initialNumber, int startIndex) {
        if (startIndex < 0) return acc;
        nSmoothEnumerationRecursive(acc, min, max, primes, initialNumber, startIndex - 1);
        var iNumber = initialNumber.multiply(primes[startIndex]);
        while(true) {
            if (iNumber.number() > max) break;
            if (iNumber.number() > min) acc.add(iNumber);
            nSmoothEnumerationRecursive(acc, min, max, primes, iNumber, startIndex - 1);
            iNumber = iNumber.multiply(primes[startIndex]);
        }
        return acc;
    }

    static ConcurrentLinkedDeque<Records.NumberAndFactors> nSmoothEnumerationIteration(long min, long max, long[] primes, Records.NumberAndFactors initialNumber, int maxNum) {

        final var acc = new ConcurrentLinkedDeque<Records.NumberAndFactors>();
        acc.add(initialNumber);
        for (int i=primes.length-1;i>=0;i--) {
//        for (int i = 0; i < primes.length; i++) {
            if (primes[i] % 3 ==0) continue;
            if (primes[i] == 649095133L) {
                var moreThanMin = initialNumber.number() * primes[i] > min;
                var lessThanMax = initialNumber.number() * primes[i] < max;
                System.out.println("primes[i] == 649095133!! greater than min: " + moreThanMin + " less than max: " + lessThanMax + " acc: " + acc);
            }
            int finalI = i;
            final var candidates = acc.stream().filter(a -> a.multiply(primes[finalI]).number() < max).collect(Collectors.toUnmodifiableList());
            for (var n : candidates) {
                var jNumber = n;
                while(true) {
                    jNumber = jNumber.multiply(primes[i]);
                    if (jNumber.number() > max) break;
                    if (jNumber.number() > min) acc.add(jNumber);
                    if (acc.size() >= maxNum)  {
                        if (initialNumber.number() < min) acc.removeFirst();
                        return acc;
                    }
                }
            }
        }
        if (initialNumber.number() < min) acc.removeFirst();
        return acc;
    }
}
