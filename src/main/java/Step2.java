import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Step2 {
    public static Records.NumberAndFactors step2(Records.NumberAndFactors d0, int q, int k) {
        // Set a := 1, and if c1qd0 < zmax then order the p |- d in A by log #Sd(p)/ log p, and while
        //c0qd0pa < zmax replace a by pa, where p is the next prime in the ordering.
        var a = new Records.NumberAndFactors(1, Map.of());
//        return a;
        var c1Prod = Constants.c1 * q * d0.number();
        if (c1Prod > Constants.zMax || c1Prod < 0) return a;

        final List<Pair<Integer, Double>> logSquareOverLogP = new ArrayList<>();
        for (var p: Constants.A) {
            if (d0.primeFactors().containsKey(p)) continue;
            final var Sdp = Math.log(Utils.Ssubd(d0.number(), p, k).size()) / Math.log(p);
            logSquareOverLogP.add(new Pair<>(p, Sdp));
        }
        logSquareOverLogP.sort(Comparator.comparing(Pair::getSecond));

        for (int i=0;i<logSquareOverLogP.size();i++) {
            final var prime = logSquareOverLogP.get(i);
            var c0Prod = Constants.c0 * q * d0.number() * prime.getFirst() * a.number();
            if (c0Prod > Constants.zMax || c0Prod < 0) break;
            a = a.multiply(prime.getFirst());
        }
        return a;
    }
}
