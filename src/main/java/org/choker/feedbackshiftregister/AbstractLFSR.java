package org.choker.feedbackshiftregister;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.math.BigInteger;
/**
* Schematic reference 'A Graduate Course in Applied Cryptography' - Dan Boneh and Victor Shoup
* (Stanford University). Supports an LFSR of any length
* For cell labeling, numbering starts from the right. The output is taken from cell 0.
* 
* Copyright (c) 2024 Jalal Choker
*/
public abstract class AbstractLFSR {
    
    protected final int length;
    protected final BigInteger taps;
    protected final String seed;
    protected final Configuration conf;

    protected BigInteger state;
    
    protected static final int RADIX = 2;

    // length: length of lfsr; must be equal to length of 'seed'
    // taps: a BigInteger whose set bits denote tapped cells
    protected AbstractLFSR (int length,  BigInteger taps, String seed, Configuration conf) {
        
        this.length = length;
        this.taps = taps;
        this.seed = seed;
        this.conf = conf;
        this.state = new BigInteger(seed,RADIX);
    }
    
    public String getSeed(){
        return seed;
    }
    
    // simulates one step of the LFSR and return the new bit as 0 or 1
   abstract int step();
   
    /*converts current LFSR configuration into another and returns an instance
	of AbstractLFSR, subclasses provide the implementation*/
   abstract AbstractLFSR convert();
    
    // returns bit i as 0 or 1.
   public int bitAt(int i)
   {
       return Utils.bitAt(this.state,i);
   }

   // returns a string representation of this LFSR
   // eg. Config: Fibonacci, Length = 8, P*(x) = x^8 + x^4 + x^3 + x^2 + 1, State: 11011010
   public String toString()
   {
      var config = ((this.conf == Configuration.FIBONACCI) ? "Fibonacci" : "Galois") + " config." ;
      
      var lengthStr = "N = "  + this.length;
      var cv = Utils.tapsToCoefficientVector(taps, length); // convert taps to cv
      var cp = "P*(x) = " + Utils.toCharacteristicPolynomial(cv);  
         
      String strState = state.toString(RADIX);
	   // pad state with zeros to have a fixed-width string representation
      var fixedWidthStrState ="State = "  + "0".repeat(length - strState.length()) + strState;
       
      return config + ", " + lengthStr + ", " + cp + ", "+ fixedWidthStrState;
   }

   // simulates k steps and return the k bits as a k-bit length string 
   public String  generate(int k)
   {
      var sequence = new StringBuilder(k);
      for(var i = 0; i < k; i++)
          sequence.append(String.valueOf(step()));
      
      return sequence.toString();
   }
   
    /*
    This is used to convert one LFSR configuration to another
    returns the taps of the transformed lfsr
    this is achieved by reversing the 'taps' then performing a rotate-left
    e.g.:
    input taps:                   0001 1101
    after reversal:               1011 1000
    shift-left 1 location:      1 0111 0000
    toggle first and last bits: 0 0111 0001
    result:                       0111 0001	
	*/
    protected BigInteger convertTaps()
    {
        BigInteger otherTaps = BigInteger.ZERO;

            //reversal
            for(int i =0; i< length; i++) // check each bit in taps from right to left
            {
                if(taps.testBit(i)) // is the bit at location i set?
                {
                    var counterpartBit = length - (i+1);
                    otherTaps = otherTaps.setBit(counterpartBit); // set the counterpart bit in otherTaps
                }
            }
            
            // rotate-left
            otherTaps  = otherTaps.shiftLeft(1); // shift-left 1 location
            otherTaps = otherTaps.clearBit(this.length);  // clear last bit (at location length)
            otherTaps = otherTaps.setBit(0); // set first bit

        return otherTaps;
    }
}

enum Configuration {
  FIBONACCI,
  GALOIS
}