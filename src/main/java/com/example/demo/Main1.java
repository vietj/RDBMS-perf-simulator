package com.example.demo;

public class Main1 {

  public static void main(String[] args) throws Exception {
    // 10 connections
    // 1000 requests / sec
    // latency distribution
    //  0ms 0.0 -> 1.0
    Simulation simulation = new Simulation(10, 1000, new long[]{0});
    simulation.run();
  }
}
