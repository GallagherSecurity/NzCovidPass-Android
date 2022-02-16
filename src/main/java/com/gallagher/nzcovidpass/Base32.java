package com.gallagher.nzcovidpass;

import androidx.annotation.NonNull;

public class Base32 {
    @NonNull
    public static byte[] decode(@NonNull String str) {
        // https://stackoverflow.com/a/7135008
        // stackoverflow thinks this is the best algorithm, so let's port it to java.
        if(str.equals("")) {
            return new byte[0];
        }

        // remove paddingCharacters
        int trimToLength = str.length();
        if(str.charAt(trimToLength-3) == '=') {
            trimToLength -= 3;
        } else if(str.charAt(trimToLength-2) == '=') {
            trimToLength -=2;
        } else if(str.charAt(trimToLength-1) == '=') {
            trimToLength -=1;
        }
        String input = str.substring(0, trimToLength);

        int byteCount = input.length() * 5 / 8; //this must be TRUNCATED
        byte[] result = new byte[byteCount]; // fills with zeroes

        byte curByte = 0;
        int bitsRemaining = 8;
        int mask = 0;
        int arrayIndex = 0;

        for(int ix = 0; ix < input.length(); ix++) {
            // throws IllegalArgumentException if the char isn't valid base32
            byte cValue = charToValue(input.charAt(ix));

            if (bitsRemaining > 5) {
                mask = cValue << (bitsRemaining - 5);
                curByte = (byte)(curByte | mask);
                bitsRemaining -= 5;
            }
            else {
                mask = cValue >> (5 - bitsRemaining);
                curByte = (byte)(curByte | mask);
                result[arrayIndex] = curByte;
                arrayIndex += 1;
                curByte = (byte)(cValue << (3 + bitsRemaining));
                bitsRemaining += 3;
            }
        }

        //if we didn't end with a full byte
        if (arrayIndex != byteCount) {
            result[arrayIndex] = curByte;
        }
        return result;
    }

    private static byte charToValue(char value) {
        //65-90 == uppercase letters
        if (value < 91 && value > 64) {
            return (byte)(value - 65);
        }
        //50-55 == numbers 2-7
        if (value < 56 && value > 49) {
            return (byte)(value - 24);
        }
        //97-122 == lowercase letters
        if (value < 123 && value > 96) {
            return (byte)(value - 97);
        }
        // else Character is not a Base32 character
        throw new IllegalArgumentException("input string contains invalid Base32 character");
    }
}
