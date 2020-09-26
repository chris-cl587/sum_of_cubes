package org.cliu;

import java.util.List;

public class Constants {
    // Small set of aux primes. (See section 3.1 of https://arxiv.org/pdf/2007.01209.pdf)
    final static List<Integer> A = List.of(5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251);

    static int primeToIndexLookup(long prime) {
        return switch ((int) prime) {
            case 5 -> 0;
            case 7 -> 1;
            case 11 -> 2;
            case 13 -> 3;
            case 17 -> 4;
            case 19 -> 5;
            case 23 -> 6;
            case 29 -> 7;
            case 31 -> 8;
            case 37 -> 9;
            case 41 -> 10;
            case 43 -> 11;
            case 47 -> 12;
            case 53 -> 13;
            case 59 -> 14;
            case 61 -> 15;
            case 67 -> 16;
            case 71 -> 17;
            case 73 -> 18;
            case 79 -> 19;
            case 83 -> 20;
            case 89 -> 21;
            case 97 -> 22;
            case 101 -> 23;
            case 103 -> 24;
            case 107 -> 25;
            case 109 -> 26;
            case 113 -> 27;
            case 127 -> 28;
            case 131 -> 29;
            case 137 -> 30;
            case 139 -> 31;
            case 149 -> 32;
            case 151 -> 33;
            case 157 -> 34;
            case 163 -> 35;
            case 167 -> 36;
            case 173 -> 37;
            case 179 -> 38;
            case 181 -> 39;
            case 191 -> 40;
            case 193 -> 41;
            case 197 -> 42;
            case 199 -> 43;
            case 211 -> 44;
            case 223 -> 45;
            case 227 -> 46;
            case 229 -> 47;
            case 233 -> 48;
            case 239 -> 49;
            case 241 -> 50;
            case 251 -> 51;
            default -> -1;
        };
    }

    public static long[][][][] getSsubDCache(int k, boolean isBitMaskCache) {
        final var retval = new long[A.size()][251][3][];
        for (int i=0;i<A.size();i++) {
            for (int j=0;j<A.get(i);j++) {
                var jModP = Math.floorMod(j, A.get(i));
                long[] dMod3EqualsOneArray = new long[A.get(i)];
                for (var isPositiveIdx: Utils.SsubdPComputation(jModP, 1, A.get(i), k)) {
                    dMod3EqualsOneArray[isPositiveIdx.intValue()] = 1;
                }

                long[] dMod3EqualsTwoArray = new long[A.get(i)];
                for (var isPositiveIdx : Utils.SsubdPComputation(jModP, 2, A.get(i), k)) {
                    dMod3EqualsTwoArray[isPositiveIdx.intValue()] = 1;
                }
                if (isBitMaskCache) {
                    retval[i][jModP][1] = dMod3EqualsOneArray;
                    retval[i][jModP][2] = dMod3EqualsTwoArray;
                } else {
                    retval[i][jModP][1] = Utils.SsubdPComputation(jModP, 1, A.get(i), k).stream().mapToLong(l -> l).toArray();;
                    retval[i][jModP][2] = Utils.SsubdPComputation(jModP, 2, A.get(i), k).stream().mapToLong(l -> l).toArray();
                }
            }
        }
        return retval;
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
