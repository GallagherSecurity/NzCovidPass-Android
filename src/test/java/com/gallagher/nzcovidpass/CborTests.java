package com.gallagher.nzcovidpass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;

import org.apache.commons.codec.binary.Base16;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("ALL")
public class CborTests {
    // ref https://github.com/cbor/test-vectors/blob/master/appendix_a.json
    public static class CborReadTests  {

        @NonNull
        Cbor.Value readSingleBase64(@NonNull String str) throws Cbor.ReadError {
            Cbor.Reader reader = new Cbor.Reader(Base64.decodeBase64(str));
            return reader.read();
        }

        @NonNull
        Cbor.Value readSingleHex(@NonNull String str) throws Cbor.ReadError {
            Cbor.Reader reader = new Cbor.Reader(new Base16(true).decode(str));
            return reader.read();
        }

        @Test
        public void testReadZero() throws Cbor.ReadError {
            Cbor.Value result = readSingleBase64("AA==");
            assertTrue(result instanceof Cbor.Value.Integer);
            assertEquals(0, result.asInteger().intValue());
        }

        @Test
        public void testReadOne() throws Cbor.ReadError {
            Cbor.Value pos = readSingleBase64("AQ==");
            assertTrue(pos instanceof Cbor.Value.Integer);
            assertEquals(1, pos.asInteger().intValue());

            Cbor.Value neg = readSingleBase64("IA==");
            assertTrue(neg instanceof Cbor.Value.Integer);
            assertEquals(-1, neg.asInteger().intValue());
        }

        @Test
        public void testReadTen() throws Cbor.ReadError {
            Cbor.Value pos = readSingleBase64("Cg==");
            assertTrue(pos instanceof Cbor.Value.Integer);
            assertEquals(10, pos.asInteger().intValue());

            Cbor.Value neg = readSingleBase64("KQ==");
            assertTrue(neg instanceof Cbor.Value.Integer);
            assertEquals(-10, neg.asInteger().intValue());
        }

        @Test
        public void testRead100() throws Cbor.ReadError {
            Cbor.Value pos = readSingleBase64("GGQ=");
            assertTrue(pos instanceof Cbor.Value.Integer);
            assertEquals(100, pos.asInteger().intValue());

            Cbor.Value neg = readSingleBase64("OGM=");
            assertTrue(neg instanceof Cbor.Value.Integer);
            assertEquals(-100, neg.asInteger().intValue());
        }

        @Test
        public void testRead1000000() throws Cbor.ReadError {
            Cbor.Value pos = readSingleBase64("GgAPQkA=");
            assertTrue(pos instanceof Cbor.Value.Integer);
            assertEquals(1000000, pos.asInteger().intValue());
        }

        @Test
        public void testReadEmptyString() throws Cbor.ReadError {
            Cbor.Value pos = readSingleBase64("YA==");
            assertTrue(pos instanceof Cbor.Value.TextString);
            assertEquals("", pos.asString());
        }

        @Test
        public void testReadString_a() throws Cbor.ReadError {
            Cbor.Value pos = readSingleBase64("YWE=");
            assertTrue(pos instanceof Cbor.Value.TextString);
            assertEquals("a", pos.asString());
        }

        @Test
        public void testReadString_IETF() throws Cbor.ReadError {
            Cbor.Value pos = readSingleBase64("ZElFVEY=");
            assertTrue(pos instanceof Cbor.Value.TextString);
            assertEquals("IETF", pos.asString());
        }

        @Test
        public void testReadString_umlaut() throws Cbor.ReadError {
            Cbor.Value pos = readSingleBase64("YsO8");
            assertTrue(pos instanceof Cbor.Value.TextString);
            assertEquals("ü", pos.asString());
        }

        @Test
        public void testReadString_asian() throws Cbor.ReadError {
            Cbor.Value pos = readSingleBase64("Y+awtA==");
            assertTrue(pos instanceof Cbor.Value.TextString);
            assertEquals("水", pos.asString());
        }

        @Test
        public void testReadString() throws Cbor.ReadError {
            Cbor.Value pos = readSingleHex("76687474703a2f2f7777772e6578616d706c652e636f6d");
            assertTrue(pos instanceof Cbor.Value.TextString);
            assertEquals("http://www.example.com", pos.asString());
        }

        @Test
        public void testReadTaggedString32() throws Cbor.ReadError {
            Cbor.Value pos = readSingleHex("d820" + "76687474703a2f2f7777772e6578616d706c652e636f6d");
            assertTrue(pos instanceof Cbor.Value.Tagged);
            Cbor.Value.Tagged result = (Cbor.Value.Tagged)pos;

            assertEquals(32, result.getTag());

            assertTrue(result.getValue() instanceof Cbor.Value.TextString);
            assertEquals("http://www.example.com", result.getValue().asString());
        }

        @Test
        public void testReadEmptyArray() throws Cbor.ReadError {
            Cbor.Value pos = readSingleBase64("gA==");
            assertTrue(pos instanceof Cbor.Value.Array);
            assertEquals(new ArrayList<Cbor.Value>(), pos.asList());
        }

        @Test
        public void testReadNumberArray() throws Cbor.ReadError {
            Cbor.Value pos = readSingleBase64("gwECAw==");
            assertTrue(pos instanceof Cbor.Value.Array);

            ArrayList<Cbor.Value> expected = new ArrayList<>();
            expected.add(Cbor.value(1));
            expected.add(Cbor.value(2));
            expected.add(Cbor.value(3));

            assertEquals(expected, pos.asList());
        }

        @Test
        public void testReadNestedNumberArray() throws Cbor.ReadError {
            Cbor.Value pos = readSingleBase64("gwGCAgOCBAU=");
            assertTrue(pos instanceof Cbor.Value.Array);

            ArrayList<Cbor.Value> expected = new ArrayList<>();
            expected.add(Cbor.value(1));
            expected.add(Cbor.value(Arrays.asList(Cbor.value(2), Cbor.value(3))));
            expected.add(Cbor.value(Arrays.asList(Cbor.value(4), Cbor.value(5))));

            assertEquals(expected, pos.asList());
        }

        @Test
        public void testReadNumberMap() throws Cbor.ReadError {
            Cbor.Value pos = readSingleBase64("ogECAwQ=");
            assertTrue(pos instanceof Cbor.Value.Map);

            HashMap<Cbor.Value, Cbor.Value> expected = new HashMap<>();
            expected.put(Cbor.value(1), Cbor.value(2));
            expected.put(Cbor.value(3), Cbor.value(4));

            assertEquals(expected, pos.asMap());
        }

        @Test
        public void testReadArrayStringMap() throws Cbor.ReadError {
            Cbor.Value pos = readSingleBase64("omFhAWFiggID");
            assertTrue(pos instanceof Cbor.Value.Map);

            HashMap<Cbor.Value, Cbor.Value> expected = new HashMap<>();
            expected.put(Cbor.value("a"), Cbor.value(1));
            expected.put(Cbor.value("b"), Cbor.value(Arrays.asList(Cbor.value(2), Cbor.value(3))));

            assertEquals(expected, pos.asMap());
        }

        @Test
        public void testReadCoseCovidPass() throws Cbor.ReadError {
            Cbor.Value result = readSingleHex("d2844aa204456b65792d310126a059011fa501781e6469643a7765623a6e7a63702e636f76696431392e6865616c74682e6e7a051a61819a0a041a7450400a627663a46840636f6e7465787482782668747470733a2f2f7777772e77332e6f72672f323031382f63726564656e7469616c732f7631782a68747470733a2f2f6e7a63702e636f76696431392e6865616c74682e6e7a2f636f6e74657874732f76316776657273696f6e65312e302e306474797065827456657269666961626c6543726564656e7469616c6f5075626c6963436f766964506173737163726564656e7469616c5375626a656374a369676976656e4e616d65644a61636b6a66616d696c794e616d656753706172726f7763646f626a313936302d30342d3136075060a4f54d4e304332be33ad78b1eafa4b5840d2e07b1dd7263d833166bdbb4f1a093837a905d7eca2ee836b6b2ada23c23154fba88a529f675d6686ee632b09ec581ab08f72b458904bb3396d10fa66d11477");
            assertTrue(result instanceof Cbor.Value.Tagged);
            Cbor.Value.Tagged tagged = (Cbor.Value.Tagged)result;

            assertEquals(18, tagged.getTag()); // COSE single sign data

            assertTrue(tagged.getValue() instanceof Cbor.Value.Array);
            List<Cbor.Value> ary = tagged.getValue().asList();
            assertEquals(4, ary.size());
            assertEquals(Cbor.MajorType.BYTE_STRING, ary.get(0).getType());
            assertEquals(Cbor.MajorType.MAP, ary.get(1).getType());
            assertEquals(Cbor.MajorType.BYTE_STRING, ary.get(2).getType());
            assertEquals(Cbor.MajorType.BYTE_STRING, ary.get(3).getType());
        }

        @Test
        public void testReadCoseCovidPassExpired() throws Cbor.ReadError {
            // this hits a boundary condition in the cbor structure that a non-expired pass doesn't
            Cbor.Value result = readSingleHex("d2844aa204456b65792d310126a059011fa501781e6469643a7765623a6e7a63702e636f76696431392e6865616c74682e6e7a051a5fa0668b041a61785f8b627663a46840636f6e7465787482782668747470733a2f2f7777772e77332e6f72672f323031382f63726564656e7469616c732f7631782a68747470733a2f2f6e7a63702e636f76696431392e6865616c74682e6e7a2f636f6e74657874732f76316776657273696f6e65312e302e306474797065827456657269666961626c6543726564656e7469616c6f5075626c6963436f766964506173737163726564656e7469616c5375626a656374a369676976656e4e616d65644a61636b6a66616d696c794e616d656753706172726f7763646f626a313936302d30342d3136075077d36a442a374daabad3030ecfaa8b00584059b85edf92c4c1ead34ecd2223a93fe37012913026e380f7169de0912a0caa8d75029863d917141caab0e8be927db5d93ececb669dd72f81a82d2da16306ce34");
            assertTrue(result instanceof Cbor.Value.Tagged);
            Cbor.Value.Tagged tagged = (Cbor.Value.Tagged)result;

            assertEquals(18, tagged.getTag()); // COSE single sign data

            assertTrue(tagged.getValue() instanceof Cbor.Value.Array);
            List<Cbor.Value> ary = tagged.getValue().asList();
            assertEquals(4, ary.size());
            assertEquals(Cbor.MajorType.BYTE_STRING, ary.get(0).getType());
            assertEquals(Cbor.MajorType.MAP, ary.get(1).getType());
            assertEquals(Cbor.MajorType.BYTE_STRING, ary.get(2).getType());
            assertEquals(Cbor.MajorType.BYTE_STRING, ary.get(3).getType());
        }
    }

    public static class CborWriteTests {
        @NonNull
        String writeSingleBase64(@NonNull Cbor.Value value) {
            Cbor.Writer writer = new Cbor.Writer();
            writer.write(value);
            return Base64.encodeBase64String(writer.getBuffer());
        }

        @NonNull
        String writeSingleHex(@NonNull Cbor.Value value) {
            Cbor.Writer writer = new Cbor.Writer();
            writer.write(value);
            return new Base16(true).encodeAsString(writer.getBuffer());
        }

        @Test
        public void testWriteZero() {
            String result = writeSingleBase64(Cbor.value(0));
            assertEquals("AA==", result);
        }

        @Test
        public void testWriteOne() {
            String pos = writeSingleBase64(Cbor.value(1));
            assertEquals("AQ==", pos);

            String neg = writeSingleBase64(Cbor.value(-1));
            assertEquals("IA==", neg);
        }

        @Test
        public void testWriteTen() {
            String pos = writeSingleBase64(Cbor.value(10));
            assertEquals("Cg==", pos);

            String neg = writeSingleBase64(Cbor.value(-10));
            assertEquals("KQ==", neg);
        }

        @Test
        public void testWrite100() {
            String pos = writeSingleBase64(Cbor.value(100));
            assertEquals("GGQ=", pos);

            String neg = writeSingleBase64(Cbor.value(-100));
            assertEquals("OGM=", neg);
        }

        @Test
        public void testWrite1000000() {
            String pos = writeSingleBase64(Cbor.value(1000000));
            assertEquals("GgAPQkA=", pos);
        }

        @Test
        public void testWriteEmptyString() {
            String pos = writeSingleBase64(Cbor.value(""));
            assertEquals("YA==", pos);
        }

        @Test
        public void testWriteString_a() {
            String pos = writeSingleBase64(Cbor.value("a"));
            assertEquals("YWE=", pos);
        }

        @Test
        public void testWriteString_IETF() {
            String pos = writeSingleBase64(Cbor.value("IETF"));
            assertEquals("ZElFVEY=", pos);
        }

        @Test
        public void testWriteString_umlaut() {
            String pos = writeSingleBase64(Cbor.value("ü"));
            assertEquals("YsO8", pos);
        }

        @Test
        public void testWriteString_asian() {
            String pos = writeSingleBase64(Cbor.value("水"));
            assertEquals("Y+awtA==", pos);
        }

        @Test
        public void testWriteString() {
            String pos = writeSingleHex(Cbor.value("http://www.example.com"));
            assertEquals("76687474703a2f2f7777772e6578616d706c652e636f6d", pos);
        }

        @Test
        public void testWriteBytes() {
            String pos = writeSingleHex(Cbor.value(new byte[] { 0x1, 0x2, 0x3 }));
            assertEquals("43010203", pos);
        }

        // writer can't do tagged values yet

        @Test
        public void testWriteEmptyArray() {
            String pos = writeSingleBase64(Cbor.value(Collections.emptyList()));
            assertEquals("gA==", pos);
        }

        @Test
        public void testWriteNumberArray() {
            String pos = writeSingleBase64(Cbor.value(Arrays.asList(
                    Cbor.value(1), Cbor.value(2),Cbor.value(3)
            )));
            assertEquals("gwECAw==", pos);
        }

        @Test
        public void testWriteNestedNumberArray() {
            String pos = writeSingleBase64(Cbor.value(Arrays.asList(
                    Cbor.value(1),
                    Cbor.value(Arrays.asList(Cbor.value(2), Cbor.value(3))),
                    Cbor.value(Arrays.asList(Cbor.value(4), Cbor.value(5)))
            )));
            assertEquals("gwGCAgOCBAU=", pos);
        }

        @Test
        public void testWriteArrayOfStringsAndBytes() {
            String pos = writeSingleBase64(Cbor.value(Arrays.asList(
                    Cbor.value("Signature1"),
                    Cbor.value(new byte[] { 0x1, 0x2, 0x3 }),
                    Cbor.value(new byte[] { 0x5, 0x6, 0x7 }),
                    Cbor.value("Trailer")
            )));
            assertEquals("hGpTaWduYXR1cmUxQwECA0MFBgdnVHJhaWxlcg==", pos);
        }

        // writer can't do maps yet
    }
}
