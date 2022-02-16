package com.gallagher.nzcovidpass;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import androidx.annotation.Nullable;

import org.apache.commons.codec.binary.Base16;
import org.junit.Assert;
import org.junit.Test;

// ref https://github.com/cbor/test-vectors/blob/master/appendix_a.json
public class Base32Tests {
    @Test
    public void testDecodeEmptyString() {
        byte[] d = Base32.decode("");
        assertEquals(0, d.length);
    }

    @Test
    public void testDecodeString() {
        byte[] d = Base32.decode("IRXWO===");
        assertArrayEquals(new byte[]{ (byte)'D', (byte)'o', (byte)'g' }, d);
    }

    @Test
    public void testDecodeBinary() {
        // 1px transparent PNG file image from https://png-pixel.com/
        byte[] referenceData = new Base16(true).decode("89504e470d0a1a0a0000000d49484452000000010000000108060000001f15c4890000000d4944415478da63fcff9fa11e000782027f3dc848ef0000000049454e44ae426082");

        // converted to base32 by https://cryptii.com/pipes/ (text->decodeBase64->encodeBase32->text)
        byte[] d = Base32.decode("RFIE4RYNBINAUAAAAAGUSSCEKIAAAAABAAAAAAIIAYAAAAA7CXCISAAAAAGUSRCBKR4NUY7476P2CHQAA6BAE7Z5ZBEO6AAAAAAESRKOISXEEYEC");
        assertArrayEquals(referenceData, d);
    }
}
