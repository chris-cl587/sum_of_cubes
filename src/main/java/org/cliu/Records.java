package org.cliu;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterable;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.math.BigInteger;
import java.util.Map;

public class Records {
    public static class NumberAndFactors {
        public long number;

        // Use `Int2IntArrayMap` from fastutil for memory efficiency: reduces allocation
        // frequency significantly as compared to a regular HashMap.
        public Int2IntArrayMap primeFactors;

        public NumberAndFactors(long number, Int2IntArrayMap primeFactors){
            this.number = number;
            this.primeFactors = primeFactors;
        }

        public long number() {
            return this.number;
        }
        public Int2IntArrayMap primeFactors() {
            return this.primeFactors;
        }
        public ObjectIterable<Int2IntMap.Entry> fastIter() {
            return Int2IntMaps.fastIterable(this.primeFactors);
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }

        @Nullable
        public NumberAndFactors multiply(long prime) {
            final var numberTimesPrime = NumberAndFactors.multiplyPositivesOrReturnNegativeOne(this.number, prime);
            if (numberTimesPrime == -1L) return null;
            final var newPrimeFactors = new Int2IntArrayMap(this.primeFactors);
            final var primeAsInt = Long.valueOf(prime).intValue();
            newPrimeFactors.compute(primeAsInt, (key, val) -> (null == val ? 0:val) + 1);
            return new NumberAndFactors(numberTimesPrime, newPrimeFactors);
        }

        private static long multiplyPositivesOrReturnNegativeOne(long x, long y) {
            long r = x * y;
            if (((x | y) >>> 31 != 0)) {
                // Some bits greater than 2^31 that might cause overflow
                // Check the result using the divide operator
                // and check for the special case of Long.MIN_VALUE * -1
                if (((y != 0) && (r / y != x)) ||
                        (x == Long.MIN_VALUE && y == -1)) {
                    return -1L;
                }
            }
            return r;
        }

        public void multiplyMutable(long prime) {
            this.primeFactors.compute((int)prime, (key, val) -> (null == val ? 0:val) + 1);
            this.number = Math.multiplyExact(this.number, prime);
        }
    }

    public record NumberAndPower(long number, int power) {}
}
