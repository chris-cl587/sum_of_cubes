import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Enumeration {
    static ConcurrentLinkedDeque<Records.NumberAndFactors> nSmoothEnumerationRecursive(ConcurrentLinkedDeque<Records.NumberAndFactors> acc, long min, long max, long[] primes, Records.NumberAndFactors initialNumber, int startIndex) {
        if (startIndex < 0) return acc;
        nSmoothEnumerationRecursive(acc, min, max, primes, initialNumber, startIndex - 1);
        var iNumber = initialNumber.multiply(primes[startIndex]);
        for (int i=0;;i++) {
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
            if (primes[i] % 3 ==0) continue;
            for (var n : Collections.unmodifiableCollection(acc)) {
                var jNumber = n;
                for (int j = 0; ; j++) {
                    jNumber = jNumber.multiply(primes[i]);
                    if (jNumber.number() > max) break;
                    if (jNumber.number() > min) acc.add(jNumber);
                    if (acc.size() >= maxNum) return acc;
                }
            }
        }
        return acc;
    }
}
