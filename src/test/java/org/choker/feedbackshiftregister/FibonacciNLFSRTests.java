package org.choker.feedbackshiftregister;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
/**
 * Tests for Fibonacci NLFSR implementation
 * 
 * Copyright (c) 2024 Jalal Choker
 */
public class FibonacciNLFSRTests {

  @Test
  void demo() {
    System.out.println("\n"
            + "#".repeat(35) + " Fibonacci NLFSR Test "
            + "#".repeat(35) + "\n");

    // https://people.kth.se/~dubrova/NLFSRwebpage/4NLFSR 0,1,2,(1,2) f = "x0 + x1 + x2 + x1.x2"
    testFibonacciNLFSR(
            "0001", 4, new int[]{0, 1, 2}, Arrays.asList(new int[]{1, 2}), "100010110100111");
  }

  @Test
  void testFibonacciNLFSR() {
    System.out.println("\n"
            + "#".repeat(35) + " Fibonacci NLFSR Test "
            + "#".repeat(35) + "\n");

    // https://people.kth.se/~dubrova/NLFSRwebpage/4NLFSR 0,1,(1,2),(2,3) f = "x0 + x1 + x1.x2 +
    // x2.x3"
    testFibonacciNLFSR("1001", 4, new int[]{0, 1},
            Arrays.asList(new int[]{1, 2}, new int[]{2, 3}), "100111101011000");

    // https://people.kth.se/~dubrova/NLFSRwebpage/5NLFSR 0,1,(1,2),(3,4) x0 + x1 + x1.x2 +
    // x3.x4 1 more register P: 0100 1100 0010 0011 1110 1110 0101 011
    testFibonacciNLFSR("10010", 5, new int[]{0, 1},
            Arrays.asList(new int[]{1, 2}, new int[]{3, 4}), "0100110000100011111011100101011");

    // https://people.kth.se/~dubrova/NLFSRwebpage/6NLFSR n= 6; f = 0,1,2,(1,2)
    // 1 more register
    testFibonacciNLFSR("1", 6, new int[]{0, 1, 2}, Arrays.asList(new int[]{1, 2}),
            "100000100011101100010101101010010111101000011001101110010011111");
  }

  static void testFibonacciNLFSR(
          String seed, int n, int[] taps, List<int[]> ands, String period) {
    var nlfsr = new FibonacciNLFSR(n, taps, ands, seed);
    System.out.print("Created the NLFSR: ");
    System.out.println(nlfsr);

    var actual = nlfsr.generate(period.length());

    System.out.println("Expected 1 period of keystream: " + period);
    System.out.println("Actual 1 period of keystream:   " + actual);

    System.out.println("\n"
            + "#".repeat(33) + " Test of output keystream "
            + "#".repeat(33));
    System.out.println("Test OK         : " + period.equals(actual));
    System.out.println("#".repeat(92) + "\n");
  }
}