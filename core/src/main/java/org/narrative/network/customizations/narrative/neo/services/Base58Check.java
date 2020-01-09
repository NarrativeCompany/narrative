package org.narrative.network.customizations.narrative.neo.services;

/*
 * From: https://github.com/MetacoSA/metaco-java-client/blob/master/src/main/java/com/metaco/api/encoders/Base58Check.java
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Metaco SA
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/* Inspired from https://github.com/adamcaudill/Base58Check/blob/master/src/Base58Check/Base58CheckEncoding.cs */

public final class Base58Check {
    private static String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static char[] ALPHABET_ARRAY = ALPHABET.toCharArray();
    private static BigInteger BASE_SIZE = BigInteger.valueOf(ALPHABET_ARRAY.length);
    private static int CHECKSUM_SIZE = 4;

    public static String encode(byte[] data) throws NoSuchAlgorithmException {
        return encodePlain(addChecksum(data));
    }

    public static String encodePlain(byte[] data) {
        BigInteger intData;

        try {
            intData = new BigInteger(1, data);
        } catch (NumberFormatException e) {
            return "";
        }

        String result = fromBigInteger(intData);

        // jw: because the BigInteger conversion will drop all leading zero's from the data, we need to add 1's to represent
        //     those zeros
        for (int i = 0; i < data.length && data[i] == 0; i++) {
            result = '1' + result;
        }

        return result;
    }

    public static String fromBigInteger(BigInteger intData) {
        String result = "";

        while (intData.compareTo(BigInteger.ZERO) == 1) {
            BigInteger[] quotientAndRemainder = intData.divideAndRemainder(BASE_SIZE);

            BigInteger quotient = quotientAndRemainder[0];
            BigInteger remainder = quotientAndRemainder[1];

            intData = quotient;

            result = ALPHABET_ARRAY[remainder.intValue()] + result;
        }

        return result;
    }

    public static byte[] decode(String encoded) throws NoSuchAlgorithmException {
        byte[] valueWithChecksum = decodePlain(encoded);

        if (valueWithChecksum == null) {
            return null;
        }

        byte[] value = verifyAndRemoveChecksum(valueWithChecksum);

        if (value == null) {
            return null;
        }

        return value;
    }

    public static byte[] decodePlain(String encoded) {
        BigInteger intData = toBigInteger(encoded);

        // jw: if we could not parse a big integer from the data, short out with null.
        if (intData == null) {
            return null;
        }

        int leadingZeros = 0;
        for (int i = 0; i < encoded.length(); i++) {
            char current = encoded.charAt(i);

            if (current == '1') {
                leadingZeros++;
            } else {
                break;
            }
        }

        byte[] bytesData;
        if (intData.equals(BigInteger.ZERO)) {
            bytesData = new byte[0];
        } else {
            bytesData = intData.toByteArray();
        }

        //Should we cut the sign byte ? - https://bitcoinj.googlecode.com/git-history/216deb2d35d1a128a7f617b91f2ca35438aae546/lib/src/com/google/bitcoin/core/Base58.java
        boolean stripSignByte = bytesData.length > 1 && bytesData[0] == 0 && bytesData[1] < 0;

        byte[] decoded = new byte[bytesData.length - (stripSignByte ? 1 : 0) + leadingZeros];

        System.arraycopy(bytesData, stripSignByte ? 1 : 0, decoded, leadingZeros, decoded.length - leadingZeros);

        return decoded;
    }

    public static BigInteger toBigInteger(String encoded) {
        if (encoded.length() == 0) {
            return null;
        }

        BigInteger intData = BigInteger.ZERO;

        for (int i = 0; i < encoded.length(); i++) {
            char current = encoded.charAt(i);

            int digit = ALPHABET.indexOf(current);

            if (digit == -1) {
                return null;
            }

            intData = (intData.multiply(BASE_SIZE)).add(BigInteger.valueOf(digit));
        }

        return intData;
    }

    private static byte[] verifyAndRemoveChecksum(byte[] data) throws NoSuchAlgorithmException {
        byte[] value = Arrays.copyOfRange(data, 0, data.length - CHECKSUM_SIZE);
        byte[] checksum = Arrays.copyOfRange(data, data.length - CHECKSUM_SIZE, data.length);
        byte[] expectedChecksum = getChecksum(value);

        return Arrays.equals(checksum, expectedChecksum) ? value : null;
    }

    private static byte[] addChecksum(byte[] data) throws NoSuchAlgorithmException {
        byte[] checksum = getChecksum(data);

        byte[] result = new byte[data.length + checksum.length];

        System.arraycopy(data, 0, result, 0, data.length);
        System.arraycopy(checksum, 0, result, data.length, checksum.length);

        return result;
    }

    private static byte[] getChecksum(byte[] data) throws NoSuchAlgorithmException {
        byte[] hash = hash256(data);
        hash = hash256(hash);

        return Arrays.copyOfRange(hash, 0, CHECKSUM_SIZE);
    }

    public static byte[] hash256(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        md.update(data);

        return md.digest();
    }
}