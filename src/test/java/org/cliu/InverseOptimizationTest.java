package org.cliu;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class InverseOptimizationTest {
    static int[] primes = Utils.primes(10000);
    @Test
    public void inverseTest() {
        final var primesLong = Arrays.stream(primes).asLongStream().boxed().collect(Collectors.toList());

        Collections.shuffle(primesLong);
        for (int i = 0; i < 1000; i++) {
            var prime = primesLong.get(i);
            var power = ThreadLocalRandom.current().nextInt(1, 4);
            while (BigInteger.valueOf(prime).pow(power).compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
                power -= 1;
            }
            var a = ThreadLocalRandom.current().nextLong(0, prime);

            var optimized = GenericUtils.inverse_mod_prime_power(a, prime, power);
            var nonOptimized = BigInteger.valueOf(a).modInverse(BigInteger.valueOf(prime).pow(power));
            Assert.assertEquals(String.format("Optimized mod inverse prime power didn't match! a: %s, prime: %s, power: %s", a, prime, power), nonOptimized.longValue(), optimized);
        }
    }
}
