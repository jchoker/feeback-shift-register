package org.choker.feedbackshiftregister;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
  A demo where NLFSRFinder (extended system of equations algorithm) is required to
  run against multiple sequences from the same Adjacent-AND NLFSR to recover it.
  This shortcoming should possibly be resolved.
* 
* Copyright (c) 2024 Jalal Choker
*/
public class NLFSRFinderFailureTests {
  @Test
  void testNLFSRFinderFailure() {

    // N: 4, P: 15, f: x0 + x1 + x1.x2 + x2.x3
    testFindNLFSR(4, "0,1,(1,2),(2,3)", "0001"); // fails for "0001"
    System.out.println();
    testFindNLFSR(4, "0,1,(1,2),(2,3)", "0010"); // succeeds for "0010"
  }

  private static void testFindNLFSR(int n, String representation, String state) {
    var taps = Utils.extractTaps(representation);
    var ands = Utils.extractAnds(representation);
    var nlfsr = new FibonacciNLFSR(n, taps, ands, state);
    System.out.println("NLFSR --> " + nlfsr);

    var threshold = 3 * n - 1; // a longer sequence has no impact
    var sequence = nlfsr.generate(threshold); // generate the shortest sequence possible
    var sequenceArray = Utils.toIntArray(sequence);
    System.out.println(
            "Sequence:   " + Arrays.toString(sequenceArray) + " - K: " + sequenceArray.length);

    var recoveredNlfsr = NLFSRFinder.find(sequenceArray);
    var cv_nlfsr = recoveredNlfsr.get("cv");
    var av_nlfsr = recoveredNlfsr.get("av");
    var nlc = cv_nlfsr.size();

    System.out.println("NLC: " + nlc);
    System.out.println("CV:  " + cv_nlfsr.toString());
    System.out.println("AV:  " + av_nlfsr.toString());
  }
}