package org.cliu;

import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class Constants {
    // Small set of aux primes. (See section 3.1 of https://arxiv.org/pdf/2007.01209.pdf)
    final static List<Integer> A = List.of(5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251);

    final static int primeToIndexLookup(long prime) {
        return A.indexOf((int)prime);
    }

    // Epsilon, c0, c1, c2, zMax are constants in the algorithm.
    static long getEps(long k) {
        // k congruent 3*epsilon mod 9
        return Math.floorMod(k, 9) == 3 ? 1 : -1;
    }
    static long c0 = 4;
    static long c1 = 50;
    static long c2 = 6;
    static long zMax = (long)5e18;
}
