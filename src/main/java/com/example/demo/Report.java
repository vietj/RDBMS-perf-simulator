package com.example.demo;

import org.HdrHistogram.Histogram;

import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

class Report {

  static void prettyPrint(PrintStream out, Histogram histogram) {
    out.println("min    = " + getMinResponseTimeMillis(histogram));
    out.println("max    = " + getMaxResponseTimeMillis(histogram));
    out.println("50%    = " + getResponseTimeMillisPercentile(histogram, 50));
    out.println("90%    = " + getResponseTimeMillisPercentile(histogram, 90));
    out.println("99%    = " + getResponseTimeMillisPercentile(histogram, 99));
    out.println("99.9%  = " + getResponseTimeMillisPercentile(histogram, 99.9));
    out.println("99.99% = " + getResponseTimeMillisPercentile(histogram, 99.99));
  }

  private static long getMinResponseTimeMillis(Histogram histogram) {
    return TimeUnit.NANOSECONDS.toMillis(histogram.getMinValue());
  }

  private static long getMaxResponseTimeMillis(Histogram histogram) {
    return TimeUnit.NANOSECONDS.toMillis(histogram.getMaxValue());
  }

  private static long getResponseTimeMillisPercentile(Histogram histogram, double x) {
    return TimeUnit.NANOSECONDS.toMillis(histogram.getValueAtPercentile(x));
  }
}