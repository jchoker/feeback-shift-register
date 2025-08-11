package org.choker.feedbackshiftregister;

import java.util.*;
/** An implementation of Berlekamp-Massey algorithm in GF(2)
* 
* Copyright (c) 2024 Jalal Choker
*/
public class BerlekampMassey {

  static final int gf2 = 2; // GF(2)

  static List<Integer> run(int[] sequence) {
    List<Integer> lr = new ArrayList<>(); // the linear recurrence sequence
    List<Integer> previous = new ArrayList<>(); // the best previous linear recurrence
    int failIdx = -1; // the index of the previous lr failure

    for (int i = 0; i < sequence.length; i++) {
      // calculate discrepancy: XOR(Si, Si-1,...,Si-j)
      int discrepancy = sequence[i];
      for (int j = 0; j < lr.size(); j++) discrepancy += (sequence[i - j - 1] * lr.get(j));
      discrepancy %= gf2;

      if (discrepancy == 0)
        continue; // i.e lr generates Si correctly and no need to modify 'lr'

      // is lr being updated for the 1st time?
      // this is when a 'one' is encountered for the 1st time in the sequence
      if (failIdx == -1) {
        // initialize lr with a 0 or 1 sequence of length (i+1)
        // this is the minimal length required to include Si in the base case
        // since there are no previous versions of lr to rely for Si generation
        for (int j = 0; j <= i; j++) lr.add(0); // both 0 & 1 are fine

        failIdx = i; // set failure idx to current idx
      }
      // lr needs to be corrected as per the formula 'lr <-- lr + delta'
      else {
        // calc. delta as per the below 4 steps:

        // 1: Set delta equal to 'previous'
        var delta = new ArrayList<Integer>(previous);
        // 2: Multiply it by −1
        var l_delta = delta.size();
        for (int j = 0; j < l_delta; j++) delta.set(j, -delta.get(j));
        // 3: Pad-left with a 1
        delta.add(0, 1);
        // 4: Pad-left with (i−failIdx−1) zeros
        var zeros = i - failIdx - 1;
        for (int j = 0; j < zeros; j++) delta.add(0, 0);

        // Keep track of the best failed lr so far
        // The best is the one with the right-most left edge (more to the right)
        // So that next time 'delta' is calculated then added to LR, the minimal LR is obtained
        // if the failed LR 'lr' has a better left-endpoint
        // than the previous one then update previous & failIdx
        var leftIdxLR = i - lr.size(); // left index of current failed LR
        var leftIdxPrevious =
                failIdx - previous.size(); // left index of previous best failed LR
        if (leftIdxLR > leftIdxPrevious) {
          previous = new ArrayList<Integer>(lr);
          failIdx = i;
        }

        // now update 'lr' by adding the 2 sequences lr & delta together
        l_delta = delta.size();
        var l_lr = lr.size();
        var min = Math.min(l_delta, l_lr);
        for (int j = 0; j < min; j++) {
          var sum = (lr.get(j) + delta.get(j) + gf2)
                  % gf2; // add gf2 to accomodate for -ve values if any
          lr.set(j, sum);
        }

        if (l_delta > l_lr) // If lr & delta are of different lengths, pad with zeros
          for (int j = l_lr; j < l_delta; j++) lr.add((delta.get(j) + gf2) % gf2);
      }
    }

    return lr;
  }
}