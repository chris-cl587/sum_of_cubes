package org.cliu;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigInteger;
import java.util.Map;

public class Records {
    public static class NumberAndFactors {
        // REMARK: BigInteger is immutable, so we do not have a way to reduce allocation here, unfortunately.
        public BigInteger number;

        // Use `Int2IntArrayMap` from fastutil for memory efficiency: reduces allocation
        // frequency significantly as compared to a regular HashMap.
        public Int2IntArrayMap primeFactors;

        public NumberAndFactors(BigInteger number, Int2IntArrayMap primeFactors){
            this.number = number;
            this.primeFactors = primeFactors;
        }

        public BigInteger number() {
            return this.number;
        }
        public Map<Integer, Integer> primeFactors() {
            return this.primeFactors;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }

        public NumberAndFactors multiply(long prime) {
            final var newPrimeFactors = new Int2IntArrayMap(this.primeFactors);
            final var primeAsInt = Long.valueOf(prime).intValue();
            newPrimeFactors.compute(primeAsInt, (key, val) -> (null == val ? 0:val) + 1);
            return new NumberAndFactors(this.number.multiply(BigInteger.valueOf(prime)), newPrimeFactors);
        }

        public void multiplyMutable(BigInteger prime) {
            this.primeFactors.compute(prime.intValue(), (key, val) -> (null == val ? 0:val) + 1);
            this.number = this.number.multiply(prime);
        }
    }

    public record NumberAndPower(long number, int power) {}
}
