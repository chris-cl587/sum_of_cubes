import cc.redberry.rings.primes.BigPrimes;

import java.time.Instant;
import java.util.Arrays;

public class IterationSpeedTest {
    public static void test() {
        var started = Instant.now();
        var iterStart = (long) 0;
        var iterEnd = (long) 1e7;
        for (long i = iterStart; i < iterEnd; i++) {
            if (i % 3 == 0) continue;
            if (i % ((iterEnd - iterStart) / 100) == 0) {
                System.out.println(String.format("i: %s", i));
            }
        }
        var timeTakenIter = 1.0 * (Instant.now().toEpochMilli() - started.toEpochMilli()) / 1000;

        System.out.println(String.format("Iterated over %s in %s", iterEnd - iterStart, timeTakenIter));
    }

    public static void testWithFactorization() {
        var started = Instant.now();
        var iterStart = (long) 0;
        var iterEnd = (long) 1e6;
        for (long i = iterStart; i < iterEnd; i++) {
            if (i % 3 == 0) continue;
            long[] factors = BigPrimes.primeFactors(i);
            if (i % ((iterEnd - iterStart) / 100) == 0) {
                System.out.println(String.format("i: %s, factors: %s", i, Arrays.toString(factors)));
            }
        }
        var timeTakenIter = 1.0 * (Instant.now().toEpochMilli() - started.toEpochMilli()) / 1000;

        System.out.println(String.format("Iterated over %s in %s", iterEnd - iterStart, timeTakenIter));
    }
}
