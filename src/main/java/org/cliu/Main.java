package org.cliu;


import jvm_alloc_rate_meter.MeterThread;

import java.math.BigInteger;

class Main {
    public static void main(String[] args) {
        MeterThread t = new MeterThread((r) -> System.out.println("Rate is: " + (r / 1e6) + " MB/sec"));
        t.start();
        Runner.run();
    }
}
