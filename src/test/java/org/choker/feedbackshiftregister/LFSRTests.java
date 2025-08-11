package org.choker.feedbackshiftregister;

import org.junit.jupiter.api.Test;
/**
 * Tests of Fibonacci and Galois LFSR
 *
 * Copyright (c) 2024 Jalal Choker
 */
public class LFSRTests {

    @Test
    void demo() {
        // D. Boneh and V. Shoup, "A Graduate Course in Applied Cryptography,"
        // Schema used in report and presentation
        // An 8-bit Fibonacci LFSR, cells 0, 2, 3, 4 are taps with seed = 10010110
        // Used in the presentation
        testLFSR(
                Configuration.FIBONACCI, 8, new int[]{0, 2, 3, 4}, "10010110", "0110100100010100");
    }

    @Test
    void testLFSR() {
        {
            // Create the 2 LFSRs from https://www.moria.us/articles/demystifying-the-lfsr/
            // An 8-bit Fibonacci LFSR, cells 0, 2, 3 and 4 are taps with initial seed = 11011010
            // Expected 1st 18 bits output: 010110110101100101
            testLFSR(
                    Configuration.FIBONACCI, 8, new int[]{0, 2, 3, 4}, "11011010", "010110110101100101");

            // An 8-bit Galois LFSR
            testLFSR(
                    Configuration.GALOIS, 8, new int[]{0, 4, 5, 6}, "01011011", "110100100010100101");
        }

        // A 14-bit Fibonacci LFSR, all cells are taps
        // It's equivalent to the 4-bit m-NLFSR with f = x0 + x1 + x2 + x1.x2
        // source: https://people.kth.se/~dubrova/nlfsr.html
        var two_p = "100010110100111100010110100111"; // test against 2 periods
        testLFSR(Configuration.FIBONACCI, 14,
                new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13}, "11001011010001", two_p);
    }

    static void testLFSR(Configuration config, int length, int[] tapsLocation, String seed,
                         String expectedOutput) {
        System.out.println("\n"
                + "#".repeat(40) + " LFSR Test "
                + "#".repeat(40) + "\n");

        AbstractLFSR lfsr = (config == Configuration.FIBONACCI)
                ? new FibonacciLFSR(length, tapsLocation, seed)
                : new GaloisLFSR(length, tapsLocation, seed);

        System.out.println("Created the LFSR --> " + lfsr);

        for (var i = 0; i < length; i++) {
            System.out.println("State bit at cell " + i + ": " + lfsr.bitAt(i));
        }

        var k = expectedOutput.length();
        var actual = lfsr.generate(k);

        System.out.println("Expected 1st " + k + " bits output sequence: " + expectedOutput);
        System.out.println("Actual 1st " + k + " bits output sequence:   " + actual);

        System.out.println("\n"
                + "#".repeat(33) + " Test of output keystream "
                + "#".repeat(33));
        System.out.println("Test OK         : " + expectedOutput.equals(actual));
        System.out.println("#".repeat(92) + "\n");
    }
}
