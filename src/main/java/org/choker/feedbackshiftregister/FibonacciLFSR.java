package org.choker.feedbackshiftregister;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**A class that represents a Fibonacci LFSR
* 
* Copyright (c) 2024 Jalal Choker
*/
public class FibonacciLFSR extends AbstractLFSR {
	
/*Convenience constructor which accepts an integer array denoting location of tapped cells
tapsLocation: array elements denote location of XOR gates
NB: in Fibonacci LFSR the right-most bit/output bit is always a tap */
  public FibonacciLFSR(int length, int[] tapsLocation, String seed) {
    this(length, Utils.calculateBigInteger(tapsLocation), seed);
  }

  // visibility within package only, not a public interface
  FibonacciLFSR(int length, BigInteger taps, String seed) {
    super(length, taps, seed, Configuration.FIBONACCI);
  }

  // simulates one step of output and returns it as 0 or 1
  public int step() {
    int step = Utils.bitAt(state, 0); // extract step

    // update state
    {
      // calculate feedback function value before shifting the state
      var tapBits = state.and(taps); // keep tap bits only from state
                                     // if the count of 1's at tapped cells is odd then feedback
                                     // = 1 as per the formula  Rx ^ Ry ^ ...
      int feedback = tapBits.bitCount() % 2;

      this.state = state.shiftRight(1); // shift state 1 location to the right
      if (feedback != 0) // If the feedback function yields 1
        this.state = state.setBit(length - 1); // Set the bit at Rn-1
    }
    return step;
  }

  // returns a Galois LFSR constructed from current LFSR
  public AbstractLFSR convert() {
    // step 1: convert taps
    BigInteger galTaps = convertTaps();
    // step 2: get equivalent seed that can generate same keystream with no time-offset
    String galSeed = convertSeed(this.state, galTaps, this.length);

    return new GaloisLFSR(this.length, galTaps, galSeed);
  }

  // converts Fibonacci seed to its Galois equivalent so that
  // both LFSR configurations generate the same keystream with no offset in time
  private String convertSeed(BigInteger fibSeed, BigInteger galTaps, int n) {
    int[] state = new int[n]; // galois current state
    Arrays.fill(state, -1); // n unknown register values
    int[] seed = new int[n]; // output

    for (var i = n - 1; i >= 0; i--) {
      var output = Utils.bitAt(fibSeed, n - (i + 1)); // extract next output from Fibonacci seed

      if (state[n - 1] == -1)
        seed[i] = output;
      else // if state[n-1] == -2 then toggle the output
        seed[i] = Utils.toggle(output);

      // rotate-right the state
      for (var j = n - 1; j > 0; j--) {
        var prv = state[j - 1];
        // if output is 0 then taps have no effect and rotate-right is just shift-right
        if (output == 0) {
          state[j] = prv; // current <- prv
        }
        // else consider taps
        else {
          // extract tap corresponding to state [j-1]
          var tap = Utils.bitAt(galTaps, (n - j));
          if (tap == 0)
            // if prv is not xor'ed with output then shift-right i.e current = prv
            state[j] = prv;
          else {
            // output & tap = 1 then current <- toggle(prv)
            if (prv >= 0) // if previous cell is a known value then toggle it
              state[j] = Utils.toggle(prv);
            // if previous cell value is an unknown variable
            // (denoted by -1 or -2) then toggle the variable
            else
              // -1 denotes 'x' and -2 denotes '-x'
              state[j] = (prv == -1) ? -2 : -1;
          }
        }
      }
      state[0] = output; // output directly feeds into input in galois mode
    }
    return Utils.toString(seed);
  }
}