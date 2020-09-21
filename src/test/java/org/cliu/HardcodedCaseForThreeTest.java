package org.cliu;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import org.junit.Test;

import java.util.Map;

public class HardcodedCaseForThreeTest {
    @Test(expected = RuntimeException.class)
    public void test() {
        final var k = 3;
        var d0For3 = new org.cliu.Records.NumberAndFactors(108398887211L, new Int2IntArrayMap(Map.of(167, 1, 649095133, 1)));
        Runner.runOne(d0For3, k);
    }
}
