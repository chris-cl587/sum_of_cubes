package org.cliu;

import cc.redberry.rings.ChineseRemainders;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class CRTOptimizationTest {
    static int[] primes = Utils.primes(100000);
    @Test
    public void crtTest() {
        final var primesLong = Arrays.stream(primes).asLongStream().boxed().collect(Collectors.toList());

        Collections.shuffle(primesLong);
        // Test 100 different combinations.
        for (int i=0;i<1000;i++) {
            final var primesToTest = new ArrayList<Long>();
            for (int j=0;j<3;j++) {
                primesToTest.add(primesLong.get(i+j));
            }
            crtTestOne(primesToTest);
        }
    }

    private void crtTestOne(List<Long> primes) {
        final var remainderList = new ArrayList<Long>();
        for(var p: primes) {
            final var remainder = ThreadLocalRandom.current().nextLong(0, p);
            remainderList.add(remainder);
        }

        final var coprimePowers = new ArrayList<Integer>();
        for (var p: primes) {
            var power = 1;
            if (p < 100 ) {
                power = ThreadLocalRandom.current().nextInt(1, 2);
            }
            coprimePowers.add(power);
        }

        final cc.redberry.rings.bigint.BigInteger[] rBigInts = remainderList.stream().map(cc.redberry.rings.bigint.BigInteger::valueOf).toArray(cc.redberry.rings.bigint.BigInteger[]::new);
        var coprimeRaisedNumbers = new cc.redberry.rings.bigint.BigInteger[primes.size()];
        var N = BigInteger.ONE;
        for (int i = 0; i < coprimeRaisedNumbers.length; i++) {
            coprimeRaisedNumbers[i] = cc.redberry.rings.bigint.BigInteger.valueOf(primes.get(i)).pow(coprimePowers.get(i));
            N = N.multiply(
                    BigInteger.valueOf(primes.get(i)).pow(coprimePowers.get(i)));
        }

        var crtResponse = Utils.crt(remainderList, primes, coprimePowers);
        var ringsResponse = ChineseRemainders.ChineseRemainders(coprimeRaisedNumbers, rBigInts).longValue();
        if (ringsResponse < 0) {
            ringsResponse += N.longValue();
        }
        Assert.assertEquals(String.format("Manual CRT wrong! remainders: %s, primes: %s, primePowers: %s, coprimeRaisedNumbers: %s. Manual: %s, rings: %s", remainderList, primes, coprimePowers, Arrays.toString(coprimeRaisedNumbers), crtResponse, ringsResponse), Long.valueOf(ringsResponse), crtResponse);
    }
}
