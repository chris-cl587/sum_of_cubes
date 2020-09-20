import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

public class Runner {
    public static void run() {
//        IterationSpeedTest.test();
//        IterationSpeedTest.testWithFactorization();

        final var primesIntArray = Utils.primes((int)1e8);
        final var primesLongArray = Arrays.stream(primesIntArray).asLongStream().toArray();
        final var lastPrime = primesIntArray[primesLongArray.length - 1];

        final var n = Integer.MAX_VALUE;

        System.out.println(String.format("Generating at most %s numbers with lastPrime: %s", n, lastPrime));
        var instant = Instant.now();
        final var d0s = Enumeration.nSmoothEnumerationIteration((long) 1e17, (long) 2e17, primesLongArray, new Records.NumberAndFactors(BigInteger.valueOf(lastPrime), Map.of(lastPrime, 1)), n);
//        final var d0s = Enumeration.nSmoothEnumerationHybrid((long) 3e13, (long) 3.0001e13, primesLongArray, new Records.NumberAndFactors(lastPrime, Map.of(lastPrime, 1)), n, primesLongArray.length - 2);
        var timeTaken = 1.0 * (Instant.now().toEpochMilli() - instant.toEpochMilli())/1000;
        System.out.println(String.format("Generating numbers took %s, %s generated", timeTaken, d0s.size()));
//        final var d0For3 = new Records.NumberAndFactors(108398887211L, Map.of(167, 1, 649095133, 1));
//        final var d0s = List.of(d0For3);
        final var k = 3;

        instant = Instant.now();
        for (var d0 : d0s) {
//            System.out.println(String.format("Checking d0=%s", d0));
            final var step1Response = Step1.step1(d0, k);
            final var d = step1Response.d();
            final var Adq = step1Response.Adq();
            final var q = step1Response.q();
            final var a = Step2.step2(d, (int)q, k);
            final var b = Step3.step3(d, a);


            Step4.step4(q, Adq, k, d0, d, a, b);
        }

        timeTaken = 1.0 * (Instant.now().toEpochMilli() - instant.toEpochMilli()) / 1000;
        System.out.println(d0s.size() + String.format(" d0s checked in %s seconds!", timeTaken));
    }
}
