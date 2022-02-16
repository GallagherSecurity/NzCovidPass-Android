package com.gallagher.nzcovidpass;

import androidx.annotation.NonNull;

// Android has java.util.base64, but not until API 26 (Android 8.0), so we can't use it if we want
// our code to work on Android 7.x or lower. There's also android.util.base64, but then we can't
// run our code under unit tests unless we run on the emulator, or import Robolectric.
// Both of these are massive dependencies so that's not great.
// The third option would be to pull in something like apache commons-codec (like we do for unit-test code)
// but now our library has a dependency on commons-codec which is quite large and might clash with host apps
// or the android OS.
//
// All these options are bad; Better to write our own stripped-down base64 impl using our existing Base32 as a reference
public class Base64 {

    @NonNull
    public static byte[] decode(@NonNull String str) {
        if(str.equals("")) {
            return new byte[0];
        }

        // remove paddingCharacters
        int trimToLength = str.length();
        if(str.charAt(trimToLength-2) == '=') {
            trimToLength -=2;
        } else if(str.charAt(trimToLength-1) == '=') {
            trimToLength -=1;
        }
        String input = str.substring(0, trimToLength);

        int byteCount = input.length() * 3 / 4; //this must be TRUNCATED
        byte[] result = new byte[byteCount]; // fills with zeroes

        byte curByte = 0;
        int bitsRemaining = 8;
        int mask = 0;
        int arrayIndex = 0;

        for(int ix = 0; ix < input.length(); ix++) {
            // throws IllegalArgumentException if the char isn't valid base64
            byte cValue = charToValue(input.charAt(ix));

            if (bitsRemaining > 6) {
                mask = cValue << (bitsRemaining - 6);
                curByte = (byte)(curByte | mask);
                bitsRemaining -= 6;
            }
            else {
                mask = cValue >> (6 - bitsRemaining);
                curByte = (byte)(curByte | mask);
                result[arrayIndex] = curByte;
                arrayIndex += 1;
                curByte = (byte)(cValue << (2 + bitsRemaining));
                bitsRemaining += 2;
            }
        }

        //if we didn't end with a full byte
        if (arrayIndex != byteCount) {
            result[arrayIndex] = curByte;
        }
        return result;
    }

    private static byte charToValue(char value) {
        //65-90 == uppercase letters map to 0-25 in base64
        if (value < 91 && value > 64) {
            return (byte)(value - 65);
        }
        //97-122 == lowercase letters map to 26-51 in base64
        if (value < 123 && value > 96) {
            return (byte)(value - 71);
        }
        //48-57 == numbers map to 52-61 in base64
        if (value < 58 && value > 47) {
            return (byte)(value + 4);
        }
        // transparently handle either standard or base64url
        if(value == '+' || value == '-') {
            return 62;
        }
        if(value == '/' || value == '_') {
            return 63;
        }
        // else Character is not a Base64 character
        throw new IllegalArgumentException("input string contains invalid Base64 character");
    }

}
