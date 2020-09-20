import java.math.BigInteger;
import java.util.Map;

public class Step3 {
    public static Records.NumberAndFactors step3(Records.NumberAndFactors d, Records.NumberAndFactors a) {

        var b = new Records.NumberAndFactors(BigInteger.ONE, Map.of());
        for (int i = 0; i< Constants.A.size(); i++) {
            if (b.primeFactors().size() > Constants.c2) break;
            final var prime = Constants.A.get(i);
            if (d.primeFactors().containsKey(prime) || a.primeFactors().containsKey(prime)) continue;
            b = b.multiply(Constants.A.get(i));
        }
        return b;
    }
}
