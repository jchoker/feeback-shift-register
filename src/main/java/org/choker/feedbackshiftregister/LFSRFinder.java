package org.choker.feedbackshiftregister;

import io.nayuki.gaussjordanelimination.Matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**Finds the linear recurrence of a minimal length LFSR that can generate the given sequence
 * using the system of linear equations; T ~ O(n4)
* 
* Copyright (c) 2024 Jalal Choker
*/
public class LFSRFinder {

  public static List<Integer> find(int[] seq) {

    var result = new ArrayList<Integer>();
    var ln = seq.length;
    // observes the 1st 1 in the sequence, a loop optimisation technique to skip a run
    // of leading zeros and 1st 1 since no CV can generate a 1 from a run of all 0s
    var oneSeen = false;

    /* n is the length of the subset of seq being evaluated.
       We must have at least n bits of keystream after location n to build a system of
               n equations (satisfy the equation ln = 2n).
       To verify the obtained CV/LR against remaining sequence the condition is ln > 2n*/
    for (var n = 0; n <= (ln / 2); n++) {
      if (!oneSeen) {
        if (seq[n] == 1)
          oneSeen = true;
        continue;
      }

      // build a linear system of n equations with n unknowns represented by AX = B
      // A: nxn matrix of input state bits
      // X: an unknown CV of size n
      // B: a vector of size n of output bits
      var a = new int[n][n];
      var b = new int[n];

      buildSystemOfEquations(a, b, seq);

      // using the library from
      // https://www.nayuki.io/page/gauss-jordan-elimination-over-any-field to solve the system
      // in modulo 2 arithmetic using Gauss–Jordan elimination algo.

      // augmented matrix which holds A&B in the form A|B
      Matrix<Integer> matrix = NLFSRFinder.buildAugmentedMatrix(a, b);
      matrix.reducedRowEchelonForm(); // apply the algorithm, O(n3)

      // fill CV from 'matrix' in the order (Cn-1,Cn-2,...,C1,C0)
      // when a solution is found
      if (NLFSRFinder.isIdentity(matrix)) // if matrix is identity then a solution is found
      {
        // fill result from matrix in the order (Cn-1,Cn-2,...,C1,C0)
        for (int j = n - 1; j >= 0; j--) result.add((int) matrix.get(j, n));

        if (verifyVector(seq, 2 * n, result)) // validate remaining sequence bits from index 2*n
          return result; // return the solution if it's valid for the rest of the sequence(if
        // any)
        result.clear(); // else clear solution
      }
    }

    return result; // empty CV if no solution found
  }

  // builds a system of equations of the matrix form AX = B
  private static void buildSystemOfEquations(int[][] a, int[] b, int[] seq) {
    var ln = a.length;
    for (var i = 0; i < ln; i++) {
      for (var j = 0; j < ln; j++) // each row of 'a' is filled with bits Si, Si+1,..., Sn-1
      {
        var s_in = seq[i + j];
        a[i][j] = s_in;
      }

      var s_out = seq[ln + i];
      b[i] = s_out;
    }
  }

  // verifies 'cv' against remaining sequence if any
  // if no bits remain in the sequence, the code assumes 'cv' is valid
  // start: index of sequence to start verification
  private static boolean verifyVector(int[] sequence, int start, List<Integer> cv) {
    var n = cv.size(); // the length of the LFSR being evaluated
    for (var i = start; i < sequence.length; i++) {
      var s_expected = sequence[i]; // output bit being considered

      var s_actual = 0;
      // apply CV on the state bits that are included in the generation of s_actual
      // start from the bit to the left of s_expected and proceed left, iterating n times
      for (int j = 0; j < n; j++) {
        var s_in_idx = i - (j + 1);
        var s_in = sequence[s_in_idx];
        if (cv.get(j) == 1) // is this a tap location
        {
          // add corresponding state bit to feedback
          s_actual += s_in;
        }
      }

      s_actual %= 2; // perform xor

      // if feedback not equal to actual output bit then CV is not valid
      if (s_actual != s_expected)
        return false;
    }

    return true;
  }
}