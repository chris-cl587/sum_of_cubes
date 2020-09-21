package org.cliu;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Runner {
    public static void run() {
        final var primesIntArray = Utils.primes((int)1e8);
        final var primesLongArray = Arrays.stream(primesIntArray).asLongStream().toArray();

        // Recursive enumeration of positive integers using prime factors. We partition by the largest prime factor.
        // This appears to not be very efficient and dependent on the ratio between zMax and the largest prive value.
        final var n = Integer.MAX_VALUE;
        for(int i=primesLongArray.length-1;i>=0;i--) {
            var prime = primesIntArray[i];
            System.out.println(String.format("Generating at most %s numbers with prime: %s", n, prime));
            var instant = Instant.now();
            final var d0s = Enumeration.nSmoothEnumerationIteration((long) 1e17, (long) 3e17, primesLongArray, new Records.NumberAndFactors(BigInteger.valueOf(prime), new Int2IntArrayMap(Map.of(prime, 1))), n, i);
            var timeTaken = 1.0 * (Instant.now().toEpochMilli() - instant.toEpochMilli()) / 1000;
            System.out.println(String.format("Generating numbers took %s, %s generated", timeTaken, d0s.size()));

//        final var d0For3 = new org.cliu.Records.NumberAndFactors(108398887211L, Map.of(167, 1, 649095133, 1));
//        final var d0s = List.of(d0For3);

            final var k = 3;
            instant = Instant.now();
            // For each number, execute steps 1-4 of Algorithm 3.5 of https://arxiv.org/pdf/2007.01209.pdf
            for (var d0 : d0s) {
                final var step1Response = Step1.step1(d0, k);
                final var d = step1Response.d();
                final var Adq = step1Response.Adq();
                final var q = step1Response.q();
                final var a = Step2.step2(d, (int) q, k);
                final var b = Step3.step3(d, a);
                Step4.step4(q, Adq, k, d0, d, a, b);
            }

            timeTaken = 1.0 * (Instant.now().toEpochMilli() - instant.toEpochMilli()) / 1000;
            System.out.println(d0s.size() + String.format(" d0s checked in %s seconds for initial prime: %s!", timeTaken, prime));
        }

    }
}
