package org.cliu;

import cc.redberry.rings.primes.BigPrimes;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

public class HardcodedCasesTest {
    @Test(expected = SquareFoundException.class)
    public void test3() {
        final var k = 3;
        var d0For3 = new org.cliu.Records.NumberAndFactors(108398887211L, new Int2IntArrayMap(Map.of(167, 1, 649095133, 1)));
        Runner.runOne(d0For3, k, Constants.zMax);
    }

    @Test(expected = SquareFoundException.class)
    public void test42() {
        final var k = 42;
        var d0For42 = new org.cliu.Records.NumberAndFactors(
                102980666258459L,
                new Int2IntArrayMap(Map.of(11, 1, 43, 1, 215921, 1, 1008323, 1)));
        Runner.runOne(d0For42, k, Constants.zMax);
    }

    @Test(expected = SquareFoundException.class)
    public void test165() {
        var dFor165Long = Math.abs(383344975542639445L - 385495523231271884L);
        var factors = BigPrimes.primeFactors(dFor165Long);
        final var k = 165;
        var d0For165 = new org.cliu.Records.NumberAndFactors(
                dFor165Long,
                new Int2IntArrayMap(Map.of(599, 1, 410783, 1, 8739967, 1)));
        Runner.runOne(d0For165, k, Constants.zMax);
    }

    // See https://math.mit.edu/~drew/NTW2020.pdf
    // d=5 checked ~5.5x10^9 values of `z`, with zMax = 1e16, according to the slides in about a minute.
    // CURRENT PERFORMANCE: 1e12 in ~120 seconds, so that's 2000x slower, but also we have no cubic reciprocity
    // so in reality, we are ~30x slower.
    @Test
    public void testd5PerfTest() {
        final var k = 33;
        var fiveD0 = new org.cliu.Records.NumberAndFactors(
                5,
                new Int2IntArrayMap(Map.of(5, 1)));
        Runner.runOne(fiveD0, k, (long)1e12);
    }
}
