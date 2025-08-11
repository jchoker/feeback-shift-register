package org.choker.feedbackshiftregister;

/**
 * Simulates a known plaintext attack using Berlekamp-Massey algorithm
 * Reproduced the example from https://github.com/thewhiteninja/lfsr-berlekamp-massey
 * 
 * Copyright (c) 2024 Jalal Choker
 */
class PlaintextAttack {

  public static void main(String[] args) {
    // used in demo
    simulateKnownPlaintextAttack();
  }

  public static void simulateKnownPlaintextAttack() {
    System.out.println("#".repeat(20) + " Known-plaintext attack using BM algorithm "
            + "#".repeat(19) + "\n");

    System.out.println("#".repeat(35) + " Encryption "
            + "#".repeat(35) + "\n");

    // the text that is encrypted and sent - 32 chars long
    String plaintext = "the secret is: there is no spoon";
    System.out.println("Plaintext       : " + plaintext);
    var binaryPlaintext = Utils.toBinaryString(plaintext); // convert it to binary string

    // x^6 + x^5 + x + 1 (101000) - not an m-lfsr
    var fiboLfsr = new FibonacciLFSR(6, new int[]{0, 1, 5}, "101000");
    System.out.println("LFSR init       : " + fiboLfsr);

    // generate (32*8) bits of keystream that is required to encrypt the 32-char plaintext
    var binaryKeystream = fiboLfsr.generate(256);
    var hexKeystream = Utils.toHexString(binaryKeystream); // convert it to hex for display
    System.out.println("LFSR stream     : " + hexKeystream);

    // xor plaintext and keystream to obtain ciphertext
    var binaryCipher = Utils.xor(binaryPlaintext, binaryKeystream);
    var ciphertext = Utils.toHexString(binaryCipher); // convert it to hex for display
    System.out.println("Ciphertext      : " + ciphertext);

    System.out.println("\n"
            + "#".repeat(35) + " Decryption "
            + "#".repeat(35) + "\n");

    String knownPlaintext = "the"; // assume 1st 3 chars of the plaintext is obtained
    System.out.println("Known plaintext : " + knownPlaintext);
    var binaryKnownPlaintext = Utils.toBinaryString(knownPlaintext); // convert to binary

    // extract cipher as large as the known plaintext
    var correspondingBinaryCipher = binaryCipher.substring(0, binaryKnownPlaintext.length());
    // keystream start = xor(known plaintext, corresponding ciphertext)
    var binaryStartKeystream = Utils.xor(binaryKnownPlaintext, correspondingBinaryCipher);
    var hexStartKeystream = Utils.toHexString(binaryStartKeystream);
    System.out.println("Stream start    : " + hexStartKeystream);

    var sequence = Utils.toIntArray(
            binaryStartKeystream); // convert to binary array
    // run BM algorithm against the keystream [1,0,0,0,1,1] or 23 hex
    var linearRecurrence = BerlekampMassey.run(sequence);
    var poly = Utils.toCharacteristicPolynomial(linearRecurrence);
    System.out.println("Recovered LFSR  : " + poly);

    // the seed is the reversed 1st 6 bits of the binary stream start
    // construct fibo lfsr from seed and linearRecurrence
    var recoveredLfsr =
            new FibonacciLFSR(linearRecurrence.size(), new int[]{0, 1, 5}, "101000");
    // generate stream as large as the binary cipher
    binaryKeystream = recoveredLfsr.generate(binaryCipher.length());
    // convert keystream to hex string for display
    hexKeystream = Utils.toHexString(binaryKeystream);
    System.out.println("LFSR stream     : " + hexKeystream);

    var binaryRecoveredPlaintext = Utils.xor(binaryKeystream, binaryCipher);
    // convert recovered binary text to alpha-numeric for display
    var recoveredPlaintext = Utils.toAlphaNumericText(binaryRecoveredPlaintext);
    System.out.println("\nDecrypted       : " + recoveredPlaintext);

    System.out.println("\n"
            + "#".repeat(38) + " Test "
            + "#".repeat(38));
    System.out.println("Test OK         : " + recoveredPlaintext.equals(plaintext));
  }
}