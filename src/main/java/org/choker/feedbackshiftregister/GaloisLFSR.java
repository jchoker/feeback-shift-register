package org.choker.feedbackshiftregister;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
*A class that represents a Galois LFSR
* 
* Copyright (c) 2024 Jalal Choker
*/
/* In Galois config. online sources are not consistent which is always a tap the lsb or the msb.
To be consistent with Fibonacci LFSR, the design assumes the right-most bit/output bit is
always a tap. The paper 'Lightweight Implementation of Hummingbird Cryptographic Algorithm on
4-Bit Microcontrollers' adopts this design, ECE department, University of Waterloo */
public class GaloisLFSR extends AbstractLFSR {
	
  public GaloisLFSR(int length, int[] tapsLocation, String seed) {
    this(length, Utils.calculateBigInteger(tapsLocation), seed);
  }

  // visibility within package only, not a public interface
  GaloisLFSR(int length, BigInteger taps, String seed) {
    super(length, taps, seed, Configuration.GALOIS);
  }

  /*In Galois configuration, when the state of LFSR needs to be updated, bits that are
  not taps are shifted one position to the right unchanged. The taps, on the other hand,
  are XOR’d with the output bit before they are fed into the next cell.
  ref.: see paper above*/
  
  // simulates one step of output and returns it as 0 or 1
  public int step() {
    int step = Utils.bitAt(state, 0); // extract step
    // update state
    {
      // If the output is 1 then state = state ^ taps
      if (step == 1)
        this.state = state.xor(taps); // Flip the register values at taps location

      this.state = state.shiftRight(1);
      // If the output is 1 then left-most bit must be explicitly set
      // Since 'by design' no tap at this location
      if (step == 1)
        this.state = state.setBit(this.length - 1);
    }

    return step;
  }

  // transforms this LFSR into an equivalent Fibonacci LFSR
  public AbstractLFSR convert() {
    // step 1: convert taps
    BigInteger fibTaps = convertTaps();
    // step 2: get equivalent seed that can generate same keystream with no time-offset

    /* should clone this LFSR
    Cloning is necessary because when generating output stream,
    the state of the LFSR changes however the LFSR should remain unchanged.
    Thus a copy should be used to generate n bits of keystream.
    The generated keystream is the equivalent seed for the converted Fibonacci LFSR */

    // pad with leading zeros if necessary
    String state =
        String.format("%" + this.length + "s", this.state.toString(RADIX)).replace(' ', '0');

    var galClone = new GaloisLFSR(this.length, this.taps, state);
    var targetSeed = galClone.generate(this.length); // in string format
    var reversedOutput = new StringBuilder(targetSeed).reverse().toString();

    return new FibonacciLFSR(this.length, fibTaps, reversedOutput);
  }
}