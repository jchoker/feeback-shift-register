package org.choker.feedbackshiftregister;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.math.BigInteger;
/**
*An implementation of a Fibonacci NLFSR
Supports an NLFSR of any length and multi-input ANDs(> 2 pins)
* 
* Copyright (c) 2024 Jalal Choker
*/
// todo: move RADIX to utils file
public class FibonacciNLFSR {

    private final int length;
    private final BigInteger taps;
    private final List<int[]> ands;
    private final String seed;

    private BigInteger state;
    private BigInteger period;

    private static final int RADIX = 2;

    public FibonacciNLFSR(int length, int[] tapsLocation, List<int[]> ands, String seed) {
        this.length = length;
        this.taps = Utils.calculateBigInteger(tapsLocation);
        this.ands = ands;
        this.seed = seed;
        this.state = new BigInteger(seed, RADIX);
    }

    public int getLength() {
        return length;
    }

    public String getSeed() {
        return seed;
    }

    public String getState() {
        String strState = this.state.toString(RADIX);
        // pad state with zeros to have a fixed-width string representation
        strState = "0".repeat(length - strState.length()) + strState;
        return strState;
    }

    public void setState(String state) {
        this.state = new BigInteger(state, RADIX);
    }

    // returns bit i as 0 or 1.
    public int bitAt(int i) {
        return Utils.bitAt(this.state, i);
    }

    // returns the period of this nlfsr
    public BigInteger getPeriod() {
        // calculate the period when the method is called for the 1st time
        if (period == null) {

            var originalState = this.state; // take a copy of the state

            var map = new HashMap<BigInteger, BigInteger>(); // state to seen at step no. lookup
            var stepsCounter = BigInteger.ZERO;

            BigInteger seenAtStep = null;
            while (seenAtStep == null) {

                map.put(this.state, stepsCounter);
                stepsCounter = stepsCounter.add(BigInteger.ONE);
                step();

                seenAtStep = map.get(this.state);
                if (seenAtStep != null)
                    period = stepsCounter.subtract(seenAtStep);
            }
            ;
            /*restore original state (in case it doesn't appear again while calculating the period)
             eg.: consider the state transition flow a -> b -> c -> d -> b
             original state 'a' doesn't appear again in the cycle and thus after the period is calculated,
             the state is 'b', thus it's necessary to restore original state if such scenario happens.*/
            this.state = originalState;
        }

        return period;
    }

    // returns true if the NLFSR is maximal
    public boolean isMaximal() {

        return this.getPeriod().equals(Utils.maximumPeriod(this.length));
    }

    // returns a string representation of the NLFSR
    // eg. N: 5, f: x0 + x1 + x1.x2 + x3.x4, State: 00001
    public String toString() {
        var cv = Utils.tapsToCoefficientVector(taps, length); // convert taps to CV
        var ff = "f: " + Utils.toFeedbackFunction(cv, ands);

        var fixedWidthStrState = "State: " + getState();

        return "N: " + length + ", " + "P: " + getPeriod() + ", " + ff + ", " + fixedWidthStrState;
    }

    // simulates one step of output and returns it as 0 or 1
    public int step() {
        int step = Utils.bitAt(state, 0); // extract step

        // calculate value of feedback function before updating the state
        var feedback = feedbackValue();

        this.state = state.shiftRight(1); // shift state 1 location to the right

        if (feedback != 0) // If the feedback function yields 1
            this.state = state.setBit(length - 1); // Set the bit at Sn-1

        return step;
    }

    // calculates the value of feedback function bit
    private int feedbackValue() {
        var tapBits = state.and(taps); // keep tap bits only from state

        // step 1: calculate feedback value from xor taps only
        // if the count of 1's at tap bit locations is odd then feedback = 1
        int feedback = tapBits.bitCount() % 2;

        // step 2: include AND gates in feedback value calculation
        for (var and : ands) {
            var product = 1; // set product initially to 1
            for (var pin_location : and) {
                var bit = bitAt(pin_location);
                // reset product & exit inner loop if a 0 cell is seen at the pin location
                if (bit == 0) {
                    product = 0;
                    break;
                }
            }
            feedback += product;
        }
        feedback %= RADIX; // modulo 2 arithmetic

        return feedback;
    }

    // simulates k steps and returns a k-bit length string 
    public String generate(int k) {
        var sequence = new StringBuilder(k);
        for (var i = 0; i < k; i++)
            sequence.append(String.valueOf(step()));
        return sequence.toString();
    }
}