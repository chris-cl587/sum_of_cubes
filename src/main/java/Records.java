import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Records {
    public record NumberAndFactors(BigInteger number, Map<Integer, Integer>primeFactors) {
        public NumberAndFactors multiply(long prime) {
            final var newPrimeFactors = new HashMap<>(this.primeFactors);
            final var primeAsInt = Long.valueOf(prime).intValue();
            newPrimeFactors.compute(primeAsInt, (key, val) -> (val == null) ? 1 : val + 1);
            return new NumberAndFactors(this.number.multiply(BigInteger.valueOf(prime)), newPrimeFactors);
        }
    }

    public record NumberAndPower(long number, int power) {}
}
