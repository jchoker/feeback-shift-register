package org.choker.feedbackshiftregister;

import org.junit.jupiter.api.Test;

/**
 * Tests of transforming a Fibonacci LFSR to Galois and vice versa
 *
 * Copyright (c) 2024 Jalal Choker
 */
public class LFSRConversionTests {

	@Test
	void demo() {
		System.out.println("\n"
				+ "#".repeat(28) + " Fibonacci --> Galois transformation "
				+ "#".repeat(27));
		// src: https://www.moria.us/articles/demystifying-the-lfsr/
		testConversion(Configuration.FIBONACCI, 8, new int[]{0, 2, 3, 4},
				"01011011"); // s: 0101 1011 taps: 0001 1101
		System.out.println("#".repeat(92));
	}

	@Test
	void testToGaloisConversion() {
		System.out.println("\n"
				+ "#".repeat(28) + " Fibonacci --> Galois transformation "
				+ "#".repeat(27));

		// source: COP514 Cryptography and Secure Systems - lfsr lect. 5 dec
		// notice that the adjusted seed is same as source
		testConversion(
				Configuration.FIBONACCI, 4, new int[]{0, 1}, "1000"); // seed 1000 - cp x^4 + x + 1
		System.out.println("#".repeat(88));
		// src: https://www.moria.us/articles/demystifying-the-lfsr/
		testConversion(Configuration.FIBONACCI, 8, new int[]{0, 2, 3, 4},
				"01011011"); // s: 0101 1011 taps: 0001 1101
	}

	@Test
	void testToFibonacciConversion() {
		System.out.println("\n"
				+ "#".repeat(28) + " Galois --> Fibonacci transformation "
				+ "#".repeat(27));

		// src: https://www.moria.us/articles/demystifying-the-lfsr/
		// convert back above galois lfsr to retrieve original Fibonacci LFSR
		testConversion(Configuration.GALOIS, 8, new int[]{0, 4, 5, 6},
				"01001011"); // seed 01001011 - p = x^8 + x^6 + x^5 + x^4 + 1
		System.out.println("#".repeat(88));
		// src: fig Galois (4th test)
		// notice that adjusted seed is same as source
		testConversion(
				Configuration.GALOIS, 5, new int[]{0, 1, 2, 3}, "10000"); // s: 10000 taps: 01111
	}

	private static void testConversion(
			Configuration config, int length, int[] tapsLocation, String seed) {
		AbstractLFSR lfsr = (config == Configuration.FIBONACCI)
				? new FibonacciLFSR(length, tapsLocation, seed)
				: new GaloisLFSR(length, tapsLocation, seed);

		System.out.println("Source LFSR:      " + lfsr);
		var converted = lfsr.convert();
		System.out.println("Transformed LFSR: " + converted);

		var period = (int) Math.pow(2, length) - 1; // period, assuming maximal lfsr
		System.out.println(
				"Generating " + period + "-bit keystream (1 period) from source LFSR...");
		var expected = lfsr.generate(period); // generate a period of keystream from source lfsr
		System.out.println("Generating same length keystream from transformed LFSR...");
		var actual = converted.generate(period);

		var take = Math.min(period, 70); // display at most 70 bits of output
		System.out.println("1st " + take + " bits/Source:     " + expected.substring(0, take));
		System.out.println("1st " + take + " bits/Transformed:" + actual.substring(0, take));
		System.out.println("#".repeat(92));
		System.out.println("Test OK         : " + expected.equals(actual));
	}
}