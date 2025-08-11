package org.choker.feedbackshiftregister;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/**
 * Analysis of P, LC and NLC for non-maximal Adjacent-AND NLFSR using precomputed files of feedback functions.
 * 
 * Copyright (c) 2024 Jalal Choker
 */
public class NonMaximalNLFSRAnalysisUsingPrecomputedListTests {

  @DisplayName("Analysis - Non-maximal Adjacent-AND NLFSR")
  @Test
  void testNonMaximalNLFSRAnalysis() {
    analyseNonMaximalNLFSRs(9, "Non-maximal Adjacent-AND NLFSR/9NLFSR.txt", "1000");
  }

  private static void analyseNonMaximalNLFSRs(int n, String fileName, String state) {
    System.out.println("\n"
            + "#".repeat(4) + " Analysis of P, LC and NLC of non-maximal Adjacent-AND NLFSR "
            + "#".repeat(4) + "\n");
    List<String> functions = Utils.readTextFile(fileName);
    var results = new HashMap<String, int[]>();

    Integer pMin = Integer.MAX_VALUE, pMax = 0, lcMin = Integer.MAX_VALUE, lcMax = 0;
    var maximalPeriod = Utils.maximumPeriod(n).intValue();
    var P_th = maximalPeriod;
    var NLFSRsSatisfyingPt = 0;

    for (var f : functions) {
      var result = analyseNonMaximalNLFSR(n, f, state);
      results.put(f, result);

      var p = result[0];
      if (pMin > p)
        pMin = p;
      if (pMax < p)
        pMax = p;

      var lc = result[1];
      if (lcMin > lc)
        lcMin = lc;
      if (lcMax < lc)
        lcMax = lc;

      if (lc >= (maximalPeriod + 1) / 2) {
        NLFSRsSatisfyingPt++;
        P_th = Math.min(P_th, p);
      }
    }

    results.forEach((key, value)
            -> System.out.println("f=" + key + ": P=" + value[0] + " LC=" + value[1]
            + " NLC=" + value[2]));

    System.out.println(
            "\nP(Min) " + pMin + " P(Max) " + pMax + " LC(Min) " + lcMin + " LC(Max) " + lcMax);
    System.out.println("P_th " + P_th + " - NLFSRs having P >= P_th " + NLFSRsSatisfyingPt);
  }

  // maximum FSR size that can be used in SoE before time-out (using onlinegdb online compiler)
  private static final int SOE_MAX_N = 8;

  // returns an array of 3 elements: P, LC and NLC
  private static int[] analyseNonMaximalNLFSR(int n, String representation, String state) {
    var result = new int[3];

    var taps = Utils.extractTaps(representation);
    var ands = Utils.extractAnds(representation);
    var nlfsr = new FibonacciNLFSR(n, taps, ands, state);

    result[0] = nlfsr.getPeriod().intValue();
    var twoPeriods = nlfsr.generate(2 * nlfsr.getPeriod().intValue()); // generate 2 periods
    var twoPeriodsArray = Utils.toIntArray(twoPeriods);

    var lc = BerlekampMassey.run(twoPeriodsArray).size();
    result[1] = lc;

    if (n <= SOE_MAX_N) {
      // generate 2 periods from all possible states until NLC is found
      int nlc = 0;
      for (var s = 1; s < (1 << n); s++) {
        var strS = Integer.toBinaryString(s);
        var fixedWidthStrState = "0".repeat(n - strS.length()) + strS;
        nlfsr = new FibonacciNLFSR(n, taps, ands, strS);
        twoPeriods = nlfsr.generate(2 * nlfsr.getPeriod().intValue()); // generate 2 periods
        twoPeriodsArray = Utils.toIntArray(twoPeriods);
        var recoveredNlfsr = NLFSRFinder.find(twoPeriodsArray);
        var cv_nlfsr = recoveredNlfsr.getOrDefault("cv", new ArrayList<Integer>());
        var av_nlfsr = recoveredNlfsr.getOrDefault("av", new ArrayList<Integer>());
        nlc = cv_nlfsr.size();
        if (nlc != 0) {
          result[2] = nlc;
          // System.out.println("S: " + s);System.out.println("NLC: " + nlc);
          break;
        }
      }
    }

    return result;
  }
}