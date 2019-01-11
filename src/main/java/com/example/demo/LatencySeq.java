package com.example.demo;

import java.util.Random;

class LatencySeq {

  private final long[] percentiles;

  LatencySeq(long... percentiles) {
    this.percentiles = percentiles;
  }

  long next(double val) {
    double threshold = 0.9D;
    double incr = 0.9D;
    for (int i = 0;i < percentiles.length;i++) {
      if (val < threshold) {
        return percentiles[i];
      }
      incr = incr * 0.1D;
      threshold += incr;
    }
    return percentiles[percentiles.length - 1];
  }

  public static void main(String[] args) {
    LatencySeq seq = new LatencySeq(1, 2, 3, 4);
/*
    System.out.println(seq.next(0));
    System.out.println(seq.next(0.9));
    System.out.println(seq.next(0.94));
    System.out.println(seq.next(0.98));
    System.out.println(seq.next(0.99));
    System.out.println(seq.next(0.999));
*/
    Random random = new Random();
    int[] test = new int[5];
    for (int i = 0;i < 100000;i++) {
      test[(int)seq.next(random.nextDouble())]++;
    }
    for (int i = 0;i < test.length;i++) {
      System.out.println(i + " -> " + test[i]);
    }
  }

}
