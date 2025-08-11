package org.choker.feedbackshiftregister;

import java.io.*;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.math.BigDecimal;
/**
 * A utility class that contains helper methods used through out the project
 * 
 * Copyright (c) 2024 Jalal Choker
 */
public class Utils {
	
	// converts an integer array into a BigInteger
	// the array values indicate the location of set bits
	public static BigInteger calculateBigInteger(int[] setBitLocations) {
		var number = BigInteger.ZERO;
		for (var location : setBitLocations) number = number.setBit(location);
		return number;
	}
 
	// extracts the location of set bits in a BigInteger number
	public static int[] setBitsLocation(BigInteger number) {
		var locations = new int[number.bitCount()]; // the number of set bits in number
 
		var i = 0;
		for (var location = 0; location < number.bitLength(); location++)
			if (number.testBit(location))
				locations[i++] = location;
 
		return locations;
	}
 
	// converts a digits text into an int array
	public static int[] toIntArray(String digitsStr) {
		var ln = digitsStr.length();
		var res = new int[ln];
		for (int i = 0; i < ln; i++) res[i] = Character.getNumericValue(digitsStr.charAt(i));
		return res;
	}
 
	// converts an array of integers into a string text
	public static String toString(int[] arr) {
		var binaryText = new StringBuilder(arr.length);
		for (var bit : arr) binaryText.append(String.valueOf(bit));
 
		return binaryText.toString();
	}
 
	// toggles a bit
	public static int toggle(int bit) {
		return (bit + 1) % 2;
	}
 
	// convert binary text into hexadecimal text
	public static String toHexString(String binaryText) {
		var sb = new StringBuilder();
		for (int i = 0; i < binaryText.length(); i += 8) {
			var _byte = binaryText.substring(i, i + 8);
			var number = Integer.parseInt(_byte, 2);
			String hexa = Integer.toHexString(number);
			// pad with 1 zero (2 chars per byte)
			String padded = String.format("%2s", hexa).replace(' ', '0');
			sb.append(padded);
		}
 
		return sb.toString();
	}
 
	// XOR's 2 binary strings of equal length
	public static String xor(String a, String b) {
		var sb = new StringBuilder();
		for (int i = 0; i < a.length(); i++) {
			var bit1 = Character.getNumericValue(a.charAt(i));
			var bit2 = Character.getNumericValue(b.charAt(i));
			sb.append(bit1 ^ bit2);
		}
 
		return sb.toString();
	}
 
	// converts a text into its underlying binary ASCII representation
	public static String toBinaryString(String text) {
		var sb = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			// char to ascii binary text
			String binary = Integer.toBinaryString(text.charAt(i));
			// pad with zeros (8 bits per char)
			var zeroPadded = String.format("%8s", binary).replaceAll(" ", "0");
			sb.append(zeroPadded);
		}
		return sb.toString();
	}
 
	// converts an ASCII binary string whose length is a multiple of 8 into alphanumeric text
	public static String toAlphaNumericText(String binaryString) {
		var res = new StringBuilder();
		var binaryStringAr = binaryString.split("(?<=\\G.{" + 8 + "})");
		for (var byteStr : binaryStringAr) {
			int charCode = Integer.parseInt(byteStr, 2);
			String alphanumeric = new Character((char) charCode).toString();
			res.append(alphanumeric);
		}
		return res.toString();
	}
 
	// returns the bit of an integer at a specific location
	public static int bitAt(int number, int i) {
		return (number >> i) & 1;
	}
 
	// returns the bit of a BigInteger at a specific location
	public static int bitAt(BigInteger number, int i) {
		return number.testBit(i) ? 1 : 0;
	}
 
	// converts the taps of an FSR represented in a BigInteger into a binary vector
	// length: the length of the taps
	public static List<Integer> tapsToCoefficientVector(BigInteger taps, int length) {
		var cv = new ArrayList<Integer>(length);
		for (var i = length - 1; i >= 0; i--) cv.add(bitAt(taps, i));
		return cv;
	}
 
	// converts the taps of an FSR represented in a BigInteger into a binary vector
	public static List<Integer> tapsToCoefficientVector(int taps, int length) {
		return tapsToCoefficientVector(BigInteger.valueOf(taps), length);
	}
 
	// constructs the characteristic polynomial of an lfsr from a coefficient vector
	// eg. cv: [1,1,0,0,0,1] --> P*(x): x^6 + x^5 + x^4 + 1
	public static String toCharacteristicPolynomial(List<Integer> coefficientVector) {
		var sb = new StringBuilder();
		var n = coefficientVector.size();
		sb.append("x^" + n);
		for (int i = 0; i < n; i++) {
			if (coefficientVector.get(i) != 0) {
				var exponent = n - (i + 1);
				if (exponent == 0)
					sb.append(" + 1");
				else {
					sb.append(" + x");
					if (exponent > 1)
						sb.append("^" + exponent);
				}
			}
		}
		return sb.toString();
	}
 
	// constructs the feedback function f of an nlfsr from CV and AV
	public static String toFeedbackFunction(List<Integer> coefficientVector, List<int[]> ands) {
		var ff = new StringBuilder();
 
		// step 1: process the coefficient vector
		var n = coefficientVector.size();
		for (int i = n - 1; i >= 0; i--) {
			if (coefficientVector.get(i) != 0) {
				var index = n - (i + 1);
				if (ff.length() != 0)
					ff.append(" + ");
 
				ff.append("x" + index);
			}
		}
		// step 2: update ff from AND gates
		for (var and : ands) {
			var product = new StringBuilder();
			for (var pin_location : and) {
				if (product.length() != 0)
					product.append(".");
 
				product.append("x" + pin_location);
			}
			ff.append(" + ");
			ff.append(product.toString());
		}
 
		return ff.toString();
	}
 
	// reads the contents of a text file from the file system and returns the lines in a list
/*	static List<String> readTextFile(String fileName) {
		List<String> functions = new ArrayList<>();
		try {
			var file = new File(fileName);
			var fr = new FileReader(file); // read the file
			var br = new BufferedReader(fr); // creates a buffering character input stream
 
			String line;
			while ((line = br.readLine()) != null) functions.add(line);
 
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return functions;
	}*/

	static List<String> readTextFile(String fileName) {
		List<String> functions = new ArrayList<>();

		// https://mkyong.com/java/java-read-a-file-from-resources-folder/
		ClassLoader classLoader = Utils.class.getClassLoader();
		URL resource = classLoader.getResource(fileName);
		if (resource == null) {
			throw new IllegalArgumentException("file not found! " + fileName);
		}

		try {
			//File file = new File(resource.getFile());
			File file = new File(resource.toURI());
			var fr = new FileReader(file); // read the file
			var br = new BufferedReader(fr); // creates a buffering character input stream

			String line;
			while ((line = br.readLine()) != null) functions.add(line);

			fr.close();
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
		return functions;
	}
 
	// returns the taps extracted from the NLFSR feedback function representation
	// eg. the function f = x0 + x1 + x2 + x1B7 x2 is represented as 1,2,(1,2)
	static int[] extractTaps(String representation) {
		var tapsLocationList = new ArrayList<Integer>();
		var sb = new StringBuilder();
		for (var i = 0; i < representation.length(); i++) {
			var ch = representation.charAt(i);
			if (ch == '(')
				break;
			if (ch == ' ')
				continue;
			if (ch == ',') {
				var location = Integer.parseInt(sb.toString());
				tapsLocationList.add(location);
				sb.setLength(0);
			} else {
				sb.append(ch);
			}
		}
 
		int[] tapsLocation = new int[tapsLocationList.size()];
		for (var i = 0; i < tapsLocation.length; i++) tapsLocation[i] = tapsLocationList.get(i);
		return tapsLocation;
	}
 
	// returns the ANDs extracted from the NLFSR feedback function representation
	static List<int[]> extractAnds(String representation) {
		List<int[]> ands = new ArrayList<int[]>();
		var andSeen = false;
 
		List<Integer> andList = new ArrayList<Integer>();
		var sb = new StringBuilder();
		for (var i = 0; i < representation.length(); i++) {
			var ch = representation.charAt(i);
			if (ch == ' ')
				continue;
			if (ch == '(')
				andSeen = true;
			else if (ch == ')') {
				andList.add(Integer.parseInt(sb.toString()));
				sb.setLength(0);
 
				int[] andArray = new int[andList.size()];
				for (var j = 0; j < andArray.length; j++) andArray[j] = (int) andList.get(j);
				andList.clear();
				ands.add(andArray);
				andSeen = false;
			} else if (andSeen) {
				if (ch == ',') {
					andList.add(Integer.parseInt(sb.toString()));
					sb.setLength(0);
				} else
					sb.append(ch);
			}
		}
 
		return ands;
	}
 
	// returns true if all ANDs conform to the Adjacent-AND NLFSR structure
	static boolean isAdjacentAndNLFSR(List<int[]> ands) {
		for (var i = 0; i < ands.size(); i++) {
			var and = ands.get(i);
			if ((and.length != 2) || (Math.abs(and[0] - and[1]) != 1))
				return false;
		}
		return true;
	}
	
	// caculates the period of a maximal FSR
	static BigInteger maximumPeriod(int n)
	{
		var power = Math.pow(2, n) - 1;
		return BigDecimal.valueOf(power).toBigIntegerExact();
	}
}