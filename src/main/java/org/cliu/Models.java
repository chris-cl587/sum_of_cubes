package org.cliu;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterable;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.math.BigInteger;
import java.util.Map;

public class Models {

    // Small wrapper class representing a number and its prime factors. This is useful for enumeration
    // purposes. We work with 64 bit longs, and when multiply overflows, we return `null` instead.
    public static class NumberAndFactors {
        public long number;

        // Use fastutil for memory efficiency: reduces allocation
        // frequency significantly as compared to a regular HashMap.
        public Int2IntMap primeFactors;

        public NumberAndFactors(long number, Int2IntMap primeFactors){
            this.number = number;
            this.primeFactors = primeFactors;
        }

        public long number() {
            return this.number;
        }

        public Int2IntMap primeFactors() {
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

        // Some code taken from `Math.multiplyExact`, but instead of throwing an exception, we return
        // -1, which serves as a sentient value as we assume `x,y > 0`.
        static long multiplyPositivesOrReturnNegativeOne(long x, long y) {
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

        // Multiple the model by `prime` and set this model's current state to that value.
        // This is useful to avoid the creation and subsequent of garbage collection of values.
        public void multiplyMutable(long prime) {
            this.primeFactors.compute((int)prime, (key, val) -> (null == val ? 0:val) + 1);
            this.number = NumberAndFactors.multiplyPositivesOrReturnNegativeOne(this.number, prime);
        }
        public NumberAndFactors copy() {
            return new NumberAndFactors(this.number, new Int2IntOpenHashMap(this.primeFactors));
        }
    }

    // A small model representing a number, the power it is raised by, and the value from raising to that power.
    // This is useful to represent prime powers in a concise and easy to lookup fashion.
    public record NumberAndPower(long number, int power, long numberToPower) {}
}
