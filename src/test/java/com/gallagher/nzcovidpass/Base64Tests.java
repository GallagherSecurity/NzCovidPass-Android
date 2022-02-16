package com.gallagher.nzcovidpass;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Base64Tests {

    @Test
    public void DecodeEmptyString() {
        byte[] input = new byte[0];
        String encoded = org.apache.commons.codec.binary.Base64.encodeBase64String(input);
        byte[] output = Base64.decode(encoded);
        assertArrayEquals(input, output);
    }

    @Test
    public void DecodeSomeKindOfString() {
        byte[] input = "Nine boxing Wizards Jump Quickly; 0123456789".getBytes(StandardCharsets.UTF_8);
        String encoded = org.apache.commons.codec.binary.Base64.encodeBase64String(input);
        byte[] output = Base64.decode(encoded);
        assertArrayEquals(input, output);
    }

    @Test
    public void DecodeRandom() {
        byte[] input = new byte[64];
        Random random = new Random();
        for(int i = 0; i < 100; i++) {
            random.nextBytes(input);
            String encoded = org.apache.commons.codec.binary.Base64.encodeBase64String(input);
            byte[] output = Base64.decode(encoded);
            assertArrayEquals(input, output);
        }
    }

}
