package org.choker.feedbackshiftregister;

import org.junit.jupiter.api.Test;

import java.util.*;
/**
 * Tests for Berlekamp-Massey algorithm
 * 
 * Copyright (c) 2024 Jalal Choker
 */
public class BerlekampMasseyTests {

  @Test
  void testBerlekampMasseyAlgorithm() {

    // source: http://koclab.cs.ucsb.edu/ simplified version slides 34-40-41
    // the first 16 bits of the states s0s1s2s3s4s5s6s7s8s9s10s11s12s13s14s15 =
    // 0101000010010110 Expected: an LFSR n = 5 and P*(x) x5 + x2 + 1  i.e  [0, 0, 1, 0, 1]
    // NB: for the LR  [0, 0, 1, 0, 1] the 1st bit in the array corresponds to location of
    // left-most cell in LFSR
    testBerlekampMasseyAlgorithm(new int[]{0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 1, 1, 0},
            List.of(0, 0, 1, 0, 1), "0101000010010110");

    // source:SANDJA REPORT ex. 1 pages 9 , 11
    // the first 9 bits of the states 001101110
    // Expected: an LFSR with n = 5 and P*(x) x5 + x2 + 1
    testBerlekampMasseyAlgorithm(
            new int[]{0, 0, 1, 1, 0, 1, 1, 1, 0}, List.of(0, 0, 1, 0, 1), "001101110");

    // source:SANDJA REPORT ex. 2 pages 11 , 13
    // the first 15 bits of the states 111101011001000
    // Expected: an LFSR with n = 4 and P*(x) x4 + x3 + 1  i.e  [1, 0, 0, 1]
    testBerlekampMasseyAlgorithm(new int[]{1, 1, 1, 1, 0, 1, 0, 1, 1, 0, 0, 1, 0, 0, 0},
            List.of(1, 0, 0, 1), "111101011001000111101011001000");

    // source:https://www.moria.us/articles/demystifying-the-lfsr/ fibo ver.
    // the first 18 bits of the states 0101 1011 0101 1001 01
    testBerlekampMasseyAlgorithm(
            new int[]{0, 1, 0, 1, 1, 0, 1, 1, 0, 1, 0, 1, 1, 0, 0, 1, 0, 1},
            List.of(0, 0, 0, 1, 1, 1, 0, 1), "010110110101100101");

    // source: COP514 Cryptography and Secure Systems - lfsr tutorial ques. 1
    // the first 12 bits of the states 0100 1101 1000
    // Expected: an LFSR with n = 6 and P*(x) x6 + x4 + x3 +x + 1  i.e  [1, 1, 0, 1, 1, 0]
    testBerlekampMasseyAlgorithm(new int[]{0, 1, 0, 0, 1, 1, 0, 1, 1, 0, 0, 0},
            List.of(0, 1, 1, 0, 1, 1), "010011011000");

    // source: COP514 Cryptography and Secure Systems - lfsr lect. 5 dec
    // the first 8 bits of the states 0001 0011
    // Expected: an LFSR with n = 4 and P*(x) x4 + x + 1  i.e  [0, 0, 1, 1]
    testBerlekampMasseyAlgorithm(new int[]{0, 0, 0, 1, 0, 0, 1, 1}, List.of(0, 0, 1, 1),
            "000100110101111000100110101111");

    // source: Massey paper - fig. 2
    // the first 5 bits of the states 10100
    // Expected: an LFSR with n = 3 and P*(x) = x3   i.e  [0, 0, 0]
    testBerlekampMasseyAlgorithm(new int[]{1, 0, 1, 0, 0}, List.of(0, 0, 0), "10100");
  }

  private static void testBerlekampMasseyAlgorithm(
          int[] sequence, List<Integer> expectedLR, String expectedKeystream) {
    System.out.println("Key bit sequence: " + Arrays.toString(sequence)
            + " - Length: " + sequence.length + " bits");

    var linearRecurrence = BerlekampMassey.run(sequence);
    System.out.println("Expected Linear Recurrence: " + expectedLR.toString());
    System.out.println("Actual Linear Recurrence:   " + linearRecurrence.toString());
    System.out.println(
            "Corresponding P*(x) = " + Utils.toCharacteristicPolynomial(linearRecurrence));

    // extract taps from Linear Recurrence
    var n = linearRecurrence.size();
    var tapsList = new ArrayList<Integer>();
    for (var i = 0; i < n; i++)
      if (linearRecurrence.get(i) == 1)
        tapsList.add(n - i - 1);
    int[] taps = new int[tapsList.size()];
    for (var j = 0; j < taps.length; j++) taps[j] = (int) tapsList.get(j);

    var state =
            new StringBuilder(Utils.toString(sequence).substring(0, n)).reverse().toString();
    var fiboLfsr = new FibonacciLFSR(linearRecurrence.size(), taps, state);

    System.out.println("Created the LFSR from the seed and synthesised P*(x)");
    System.out.println(fiboLfsr.toString());

    var k = expectedKeystream.length();
    System.out.println("Generating the 1st " + k + "-bit output sequence...");
    var actualKeyStr = fiboLfsr.generate(k);
    System.out.println("Expected Keystream: " + expectedKeystream);
    System.out.println("Actual Keystream:   " + actualKeyStr);
    System.out.println("Test OK         : " + expectedKeystream.equals(actualKeyStr));
    System.out.println("---------------------------------");
  }
}