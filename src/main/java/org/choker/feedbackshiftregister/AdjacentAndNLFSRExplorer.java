package org.choker.feedbackshiftregister;

import java.util.ArrayList;
import java.util.List;
/**
 * A function that finds all Adjacent-AND NLFSRs of length n
 * maximal: retrieve either maximal or non-maximal ones only
 * 
 * Copyright (c) 2024 Jalal Choker
*/
public class AdjacentAndNLFSRExplorer {

	public static List<FibonacciNLFSR> findAll(int n, boolean isMaximal) {

		var result = new ArrayList<FibonacciNLFSR>();

		// generate all possible taps combination for n; range between [1, 2^n - 1],
		// i.e. from 1 tap only at R0 to taps at all cells
		var allTapCombinations = new ArrayList<int[]>();
		var pow = Math.pow(2, n);
		for (var i = 1; i < pow; i++) {
			var taps = Integer.toBinaryString(i);

			// construct the taps location array from taps
			int tapsCount = Integer.bitCount(i);
			var tapsArray = new int[tapsCount];
			var tapsLn = taps.length();
			var idx = 0;
			// start from cell 0
			for (var k = tapsLn - 1; k >= 0; k--) {
				if (taps.charAt(k) == '1')
					tapsArray[idx++] = tapsLn - k - 1;
			}
			allTapCombinations.add(tapsArray);
		}
 
		/*Generate all possible adjacent AND combinations for n; range between
		[1, 2^(n - 1) - 1], i.e. from one AND to ANDs between each 2 adjacent cells.
		An NLFSR is required to have at least 1 AND
		*/
		var allAndCombinations = new ArrayList<List<int[]>>();
		pow = Math.pow(2, n - 1);
		for (var i = 1; i < pow; i++) {
			var ands = Integer.toBinaryString(i);
			var length = ands.length(); // get effective binary length
			var andList = new ArrayList<int[]>();
			for (var j = 0; j < length; j++)
				if (Utils.bitAt(i, j) == 1)
					andList.add(new int[]{j, j + 1});

			allAndCombinations.add(andList);
		}

		// create NLFSRs by combining taps with ands
		for (var taps : allTapCombinations)
			for (var ands : allAndCombinations) {
				var nlfsr = new FibonacciNLFSR(n, taps, ands, "1"); // default state '1'
				if (nlfsr.isMaximal() == isMaximal)// take maximal or non-maximal only
					result.add(nlfsr);
			}

		return result;
	}
}
