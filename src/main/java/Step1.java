import java.util.List;

public class Step1 {
    record Step1Response(long q, Records.NumberAndFactors d, List<Long> Adq) {};
    public static Step1Response step1(Records.NumberAndFactors d0, int k) {
        // For each positive divisor d1 of k/3 with gcd(d1, k/d1) = 1, set d := d0d1 and let Ad(q)
        //be the set of z + qZ for which (d, z) is admissible.
        if (k == 3) {
            if (d0.number() * 162 < 0) {
                return new Step1Response(1, d0, List.of(0L));
            } else{
                return new Step1Response(162, d0, Utils.cubicReciprocityConstraint(d0, k));
            }
        } else {
            throw new RuntimeException("Not supported yet!");
        }
    }
}
