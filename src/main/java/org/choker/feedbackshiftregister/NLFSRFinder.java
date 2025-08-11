package org.choker.feedbackshiftregister;

import io.nayuki.gaussjordanelimination.Matrix;
import io.nayuki.gaussjordanelimination.PrimeField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map; 
/**
*Finds the coefficient and 'AND' vectors of the minimal length Adjacent-AND NLFSR that generate a given sequence.
*It uses an extended version of the system of linear equations algorithm.
* 
* Copyright (c) 2024 Jalal Choker
*/
public class NLFSRFinder {

  /*returns a map containing 2 key-value pairs (kvps), the keys are 'cv' & 'av'
   value of 'cv' is a list of coefficients, value of 'av' is a list of ANDs; T = O(n^4)*/
  public static Map<String, List<Integer>> find(int[] seq) {
    var ln = seq.length;
    // observes the 1st 1 in the sequence: a loop optimisation is to skip a run of leading
    // 0s and 1st 1: no NLFSR of this kind can generate 1 from a run of 0s
    var oneSeen = false;
 
    /* n is the size of the current potential NLFSR
       n-bit NLFSR will have n & n - 1 unknowns for the CV & AV respectively (total: 2n-1)
       A system of 2n - 1 equations is required to find 2n-1 unknowns
       2n - 1 bits of keystream after index n is needed to build the system
       Thus the sub-sequence of length K satisfies the condition: K = 3n - 1
       To verify the obtained CV & AV against remaining sequence the requirement is K > 3n - 1
    */
    for (var n = 0; (3 * n - 1) <= ln; n++) // or n <= (ln + 1) / 3
    {
      if (!oneSeen) {
        if (seq[n] == 1)
          oneSeen = true;
        continue;
      }
 
      /* build a system of (2*n - 1) equations with (2*n - 1) unknowns represented by AX = B
		 A: (2*n - 1)x(2*n - 1) matrix of input state bits each row is formed of n input
         state bits and (n-1) multiplications of each 2 adjacent input state bits
		 X: the variables vector of size (2*n - 1)
			denoting a CV of size n followed by an AV of size n - 1
		 B: a vector of size (2*n - 1) of output bits*/
      var equations = 2 * n - 1; // no. of equations to generate
      var a = new int[equations]
              [equations]; // 2d array to store values of left-hand side of equations
      var b = new int[equations]; // a vectore to store values of right-hand side of equations

      buildSystemOfEquations(a, b, seq, n);

      // solves a system of linear equations in GF(2) using Gauss–Jordan elimination algo.
      Matrix<Integer> matrix = buildAugmentedMatrix(a, b);

      matrix.reducedRowEchelonForm(); // apply the algorithm, O(n3)

      // fill CV & AV from 'matrix' in the order (Cn-1,Cn-2,...,C1,C0) & (An-2,An-3,...,A1,A0)
      // only when a solution has been found
      if (isIdentity(matrix)) // if the matrix is in identity then a solution has been found
      {
        var cv = new ArrayList<Integer>();
        var av = new ArrayList<Integer>();

        for (int j = n - 1; j >= 0; j--) cv.add((int) matrix.get(j, equations));

        for (int j = equations - 1; j >= n; j--) av.add((int) matrix.get(j, equations));
        // validate remaining sequence bits from index 3*n - 1
        if (verifyVectors(seq, 3 * n - 1, cv, av))
          // exit loop if the solution found is valid for the rest of the sequence
          return Map.of("cv", cv, "av", av);
      }
    }

    // return empty vectors if no solution is found
    return Map.of("cv", new ArrayList<Integer>(), "av", new ArrayList<Integer>());
  }

  // builds a system of equations represented by the matrix form AX = B
  // n: the size of a potential FSR
  private static void buildSystemOfEquations(int[][] a, int[] b, int[] seq, int n) {
    var ln = a.length;
    for (var i = 0; i < ln; i++) {
      for (var j = 0; j < n;
           j++) // 1st n locations in each row of 'a'  are filled with bits Si, Si+1,..., Sn-1
      {
        var s_in = seq[i + j];
        a[i][j] = s_in;
      }
      for (var j = 0; j < n - 1;
           j++) // remaining n-1 locations of 'a' filled with bits Si*Si+1,..., Sn-2*Sn-1
      {
        var s_in = seq[i + j];
        var s_inPlus1 = seq[i + j + 1];
        a[i][n + j] = s_in * s_inPlus1; // add offset n to insert Si*Si+1 in the array row
      }

      var s_out = seq[n + i];
      b[i] = s_out;
    }
  }

  // returns an augmented matrix that holds the values A & B in the form A|B
  static Matrix<Integer> buildAugmentedMatrix(int[][] a, int[] b) {
    var n = a.length;
    Matrix<Integer> matrix = new Matrix<Integer>(n, n + 1, new PrimeField(2));

    // fill the matrix
    for (int r = 0; r < n; r++) {
      int c = 0;

      for (; c < n; c++) matrix.set(r, c, a[r][c]);

      matrix.set(r, c, b[r]);
    }
    return matrix;
  }

  // verifies cv & av against remaining sequence if any
  // if no bits remain in the sequence, the code assumes that vectors are valid
  // start: index of sequence to start verification
  private static boolean verifyVectors(
          int[] sequence, int start, List<Integer> cv, List<Integer> av) {
    var n = cv.size(); // the length of the fsr being evaluated
    for (var i = start; i < sequence.length; i++) {
      var s_expected = sequence[i]; // output bit being considered

      var s_actual = 0;
      // apply CV & AV on the state bits that are included in the generation of s_actual
      // start from the bit to the left of s_expected and proceed left, iterating n times
      for (int j = 0; j < n; j++) {
        var s_in_idx = i - (j + 1);
        var s_in = sequence[s_in_idx];
        if (cv.get(j) == 1) // is this a tap location
        {
          // add corresponding state bit to feedback
          s_actual += s_in;
        }
        if (j < n - 1) // iterate n-1 times only for AV
        {
          if (av.get(j) == 1) // is there AND gate
          {
            s_actual += s_in * sequence[s_in_idx - 1]; // multiply corresponding adjacent state
            // bits and add value to feedback
          }
        }
      }

      s_actual %= 2; // perform xor

      // if feedback not equal to actual output bit then the vectors are invalid
      if (s_actual != s_expected)
        return false;
    }

    return true;
  }

  // checks if the square sub-matrix of the augmented matrix is identity
  // if it's in identity form then a solution for the system is found
  static boolean isIdentity(Matrix<Integer> augmentedMatrix) {
    var n = augmentedMatrix.rowCount();

    for (int i = 0; i < n; i++)
      for (int j = 0; j < n; j++)
        if (i == j && augmentedMatrix.get(i, j) == 0) // consider the diagonal
          return false;

    return true;
  }
}
