# Sum of cubes exploration

Author: Chris Liu (chris.cl587@gmail.com/cl587@cornell.edu)

Goal: The goal of this repository is to document some investigations and learnings I've done on the sum of cubes problem.

Recently, Quanta Magazine has written about this problem in [Why the Sum of Three Cubes Is a Hard Math Problem](https://www.quantamagazine.org/why-the-sum-of-three-cubes-is-a-hard-math-problem-20191105/) and [Sum-of-Three-Cubes Problem Solved for ‘Stubborn’ Number 33](https://www.quantamagazine.org/sum-of-three-cubes-problem-solved-for-stubborn-number-33-20190326/). 

In reading the papers, I've discovered the following resources:
1. [Cracking the problem with 33](https://link.springer.com/content/pdf/10.1007/s40993-019-0162-1.pdf) - Research paper describing techniques used in 2019 to solve the 33 problem.
2. [Sums of three cubes](https://math.mit.edu/~drew/NTW2020.pdf) - Slides with a summary of the approach.
3. [On a question of Mordell](https://arxiv.org/pdf/2007.01209.pdf) - Arxiv preprint of techniques that the researchers developed to solve 42, 3, and improve efficiency from the 2019 solution.

## This repository

My background in mathematics does not allow me to understand the `On a question of Mordell` paper to its full extent, but I can gain intuition into the problem by coding Algorithm 3.5. This is what I did here.

The repository is in Java as the IDE, debugging, and library ecosystem allows me to iterate more quickly. I will lose efficiency compared to a C/C++ solution, but as I am not going for solving novel solutions, but rather for learning, I think this is a reasonable choice.

## Structure

Consider a fixed `k` (e.g `3`), and some search boundaries, `zMax`, and `dMax`, where `d = |x+y|`. Our goal is to find `x^3+y^3+z^3=k`.

Algorithm 3.5 has the following stages:
1. Recursively enumerate positive integers using prime powers. (This is also a source of easy parallelism, if we restrict the max prime)
2. Use cubic reciprociy constraints to restrict the set of solutions mod `q`, where `q` is defined by Lemma 3.3 - this helps reduce the number of candidates in Step 4.
3. Use auxiliary primes within the Chinese Remainder Theorem enumeration to restrict the set of solutions.
4. Use auxiliary primes to filter out candidate solutions before checking for square candidates.
5. Use Chinese Remainder Theorem, along with cube root solutions of `k` mod the prime power. This cube root solution's congruence class is then checked for squares (as due to equation (1.2), if a function of (d,z) is a square, then we get a solution triple (x,y,z))

## Code and optimizations

I've attempted to enumerate the steps in the code as `Step{1,...4}`, and the initial enumeration code in `Enumeration.java`. The code has the following optimizations:

1. Use primitive longs when possible, as it is much faster than BigInteger.
2. Use cached `S_d(p)` computations for congruence classes mod p of Algorithm 3.1, as per Remark 3.6. This uses the pre-computed bitmap approach: using a cache was much slower than direct array access.
3. Use Montgomery Inverse and [Newton-Raphson iteration](On Newton-Raphson iteration for multiplicative inverses modulo prime powers) for fast multiplicative inverses mod prime powers
4. Use Shanks-Tonelli algorithm to compute square roots mod prime.
5. Use lazy Cartesian product iterator for the Chinese Remainder Theorem candidates.
6. Use Algorithm 4.2 of [Taking Cube Roots in Zm](https://doi.org/10.1016/S0893-9659(02)00031-9) to compute cuberoots mod a prime (variant of Shanks-Tonelli)
7. Use Hensel Lifting to lift solutions of cuberoots mod a prime to mod a prime power.
