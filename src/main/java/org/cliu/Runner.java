package org.cliu;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Runner {
    public static void run() {
        final var primesIntArray = Utils.primes((int)1e8);
        final var primesLongArray = Arrays.stream(primesIntArray).asLongStream().toArray();

        // Recursive enumeration of positive integers using prime factors. We partition by the largest prime factor.
        // This appears to not be very efficient and dependent on the ratio between zMax and the largest prime value.
        final var n = Integer.MAX_VALUE;
        for(int i=primesLongArray.length-1;i>=0;i--) {
            var prime = primesIntArray[i];
            System.out.println(String.format("Generating at most %s numbers with prime: %s", n, prime));
            var instant = Instant.now();
            final var d0s = Enumeration.nSmoothEnumerationIteration((long) 1e17, (long) 3e17, primesLongArray, new Models.NumberAndFactors(prime, new Int2IntArrayMap(Map.of(prime, 1))), n, i);
            var timeTaken = 1.0 * (Instant.now().toEpochMilli() - instant.toEpochMilli()) / 1000;
            System.out.println(String.format("Generating numbers took %s, %s generated", timeTaken, d0s.size()));

            final var k = 3;
            instant = Instant.now();
            // For each number, execute steps 1-4 of Algorithm 3.5 of https://arxiv.org/pdf/2007.01209.pdf
            for (var d0 : d0s) {
                runOne(d0, k, Constants.zMax, Constants.c0, Constants.c1, Constants.c2);
            }

            timeTaken = 1.0 * (Instant.now().toEpochMilli() - instant.toEpochMilli()) / 1000;
            System.out.println(d0s.size() + String.format(" d0s checked in %s seconds for initial prime: %s!", timeTaken, prime));
        }
    }

    public static void runOneDefaults(Models.NumberAndFactors d0, int k) {
        runOne(d0, k, Constants.zMax, Constants.c0, Constants.c1, Constants.c2);
    }

    public static void runOne(Models.NumberAndFactors d0, int k, long zMax, long c0, long c1, long c2) {
        final var step1Response = Step1.step1(d0, k);

        final var d = step1Response.d();
        var Adq = step1Response.Adq();
        var q = step1Response.q();

        final var a = Step2.step2(d, (int) q, k, zMax, c0, c1);

        final var b = Step3.step3(d, a, c2);

        // REMARK: the response from step 1 may overflow as zMax is close to the max Long value.
        // To check, we multiply d0 * a * q and if it overflows, we set q = 1. This still makes
        // `m` large enough such that checking for square candidates is fairly fast.
        if (null == d0.multiply(a.number() * q)) {
            q = 1;
            Adq = List.of(0L);
        }

        Step4.step4(q, Adq, k, d0, d, a, b, zMax);
    }
}
