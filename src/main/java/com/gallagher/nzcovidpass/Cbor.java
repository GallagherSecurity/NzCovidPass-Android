//
// Copyright Gallagher Group Ltd 2021
//

// The intention is that this file will split out into it's own library, like MiniRxSwift or LibAsn.
// but for now single-file inline is OK. This contains just enough CBOR to validate NZ Covid Passes; it is not a complete CBOR implementation.
package com.gallagher.nzcovidpass;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

// hacky use of a class to emulate a namespace, so we can keep this in a single file
public class Cbor {

    // convenience initializers
    public static Cbor.Value.Integer value(int integer) {
        return new Cbor.Value.Integer(integer);
    }
    public static Cbor.Value.ByteString value(@NonNull byte[] array) {
        return new Cbor.Value.ByteString(array);
    }
    public static Cbor.Value.TextString value(@NonNull String string) {
        return new Cbor.Value.TextString(string);
    }
    public static Cbor.Value.Array value(@NonNull List<Cbor.Value> list) {
        return new Cbor.Value.Array(list);
    }
    public static Cbor.Value.Map value(@NonNull java.util.Map<Cbor.Value, Cbor.Value> map) {
        return new Cbor.Value.Map(map);
    }
    public static Cbor.Value.Tagged value(int tag, @NonNull Cbor.Value value) {
        return new Cbor.Value.Tagged(tag, value);
    }

    // emulate a swift enum with a class hierarchy
    public static abstract class Value {
        /// CBOR encoded map (major type 5)
        public static class Map extends Value {
            @NonNull
            private final java.util.Map<Cbor.Value, Cbor.Value> _value;

            public Map(@NonNull java.util.Map<Cbor.Value, Cbor.Value> value) {
                _value = value;
            }
            public java.util.Map<Cbor.Value, Cbor.Value> getValue() {
                return _value;
            }

            @NonNull
            @Override
            public MajorType getType() {
                return MajorType.MAP;
            }

            @Override
            @Nullable
            public java.util.Map<Cbor.Value, Cbor.Value> asMap() {
                return _value;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Map map = (Map) o;
                return _value.equals(map._value);
            }

            @Override
            public int hashCode() {
                return Objects.hash(_value);
            }

            @NonNull
            @Override
            public String toString() {
                return "Cbor.Map{value=" + _value + '}';
            }
        }
        /// CBOR encoded array (major type 4)
        public static class Array extends Value {
            @NonNull
            private final List<Value> _value;

            public Array(@NonNull List<Value> value) {
                _value = value;
            }
            public List<Value> getValue() {
                return _value;
            }

            @NonNull
            @Override
            public MajorType getType() {
                return MajorType.ARRAY;
            }

            @Override
            @Nullable
            public List<Cbor.Value> asList() {
                return _value;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Array array = (Array) o;
                return _value.equals(array._value);
            }

            @Override
            public int hashCode() {
                return Objects.hash(_value);
            }

            @NonNull
            @Override
            public String toString() {
                return "Cbor.Array{value=" + _value + '}';
            }
        }
        /// CBOR encoded integer (major types 0 and 1)
        public static class Integer extends Value {
            private final int _value;

            public Integer(int value) {
                _value = value;
            }
            public int getValue() {
                return _value;
            }

            @NonNull
            @Override
            public MajorType getType() {
                return _value >= 0 ? MajorType.POSITIVE_INT : MajorType.NEGATIVE_INT;
            }

            @Override
            @Nullable
            public java.lang.Integer asInteger() {
                return _value;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Integer integer = (Integer) o;
                return _value == integer._value;
            }

            @Override
            public int hashCode() {
                return Objects.hash(_value);
            }

            @NonNull
            @Override
            public String toString() {
                return "Cbor.Integer{value=" + _value + '}';
            }
        }
        /// CBOR encoded text (major type 3)
        public static class TextString extends Value {
            @NonNull
            private final String _value;

            public TextString(@NonNull String value) {
                _value = value;
            }
            public String getValue() {
                return _value;
            }

            @NonNull
            @Override
            public MajorType getType() {
                return MajorType.TEXT_STRING;
            }

            @Override
            @Nullable
            public String asString() {
                return _value;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                TextString that = (TextString) o;
                return _value.equals(that._value);
            }

            @Override
            public int hashCode() {
                return Objects.hash(_value);
            }

            @NonNull
            @Override
            public String toString() {
                return "Cbor.TextString{value=" + _value + '}';
            }
        }
        /// CBOR encoded text (major type 2)
        public static class ByteString extends Value {
            @NonNull
            private final byte[] _value;

            public ByteString(@NonNull byte[] value) {
                _value = value;
            }
            public byte[] getValue() {
                return _value;
            }

            @NonNull
            @Override
            public MajorType getType() {
                return MajorType.BYTE_STRING;
            }

            @Override
            @Nullable
            public byte[] asBytes() {
                return _value;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                ByteString that = (ByteString) o;
                return Arrays.equals(_value, that._value);
            }

            @Override
            public int hashCode() {
                return Arrays.hashCode(_value);
            }

            @NonNull
            @Override
            public String toString() {
                return "Cbor.ByteString{size=" + _value.length + '}';
            }
        }
        /// CBOR value with additional semantic tag (such as 32 for a URL)
        public static class Tagged extends Value {
            private final int _tag;
            @NonNull
            private final Cbor.Value _value;

            public Tagged(int tag, @NonNull Cbor.Value value) {
                _tag = tag;
                _value = value;
            }
            public int getTag() {
                return _tag;
            }
            public Cbor.Value getValue() {
                return _value;
            }

            @NonNull
            @Override
            public MajorType getType() {
                return MajorType.SEMANTIC_TAG;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Tagged tagged = (Tagged) o;
                return _tag == tagged._tag && _value.equals(tagged._value);
            }

            @Override
            public int hashCode() {
                return Objects.hash(_tag, _value);
            }

            @NonNull
            @Override
            public String toString() {
                return "Cbor.Tagged{tag=" + _tag + ", value=" + _value + '}';
            }
        }

        @NonNull
        public abstract MajorType getType();

        // convenience for getting values out rather than using instanceof
        @Nullable
        public java.lang.Integer asInteger() {
            return null;
        }
        @Nullable
        public byte[] asBytes() {
            return null;
        }
        @Nullable
        public String asString() {
            return null;
        }
        @Nullable
        public List<Cbor.Value> asList() {
            return null;
        }
        @Nullable
        public java.util.Map<Cbor.Value, Cbor.Value> asMap() {
            return null;
        }
    }

    public enum MajorType {
        POSITIVE_INT(0),
        NEGATIVE_INT(1),
        BYTE_STRING(2),
        TEXT_STRING(3),
        ARRAY(4),
        MAP(5),
        SEMANTIC_TAG(6),
        SPECIAL(7);

        private final int _rawValue;
        MajorType(int rawValue) {
            _rawValue = rawValue;
        }
        public int getRawValue() {
            return _rawValue;
        }

        public static MajorType identify(byte b) {
            // first 3 bits in a CBOR tag are the major type
            switch((b & 0xE0) >>> 5) {
                case 0: return POSITIVE_INT;
                case 1: return NEGATIVE_INT;
                case 2: return BYTE_STRING;
                case 3: return TEXT_STRING;
                case 4: return ARRAY;
                case 5: return MAP;
                case 6: return SEMANTIC_TAG;
                case 7: return SPECIAL;
                default: throw new IllegalArgumentException("byte right-shifted by 5 cannot be greater than 7");
            }
        }
    }

    public static abstract class ReadError extends Exception {
        public static class MalformedInput extends ReadError {}
        public static class InputTooShort extends ReadError {}
        public static class InvalidUtf8String extends ReadError {}
    }

    public static class Reader {
        @NonNull private final byte[] _data;
        private int pos;

        public Reader(@NonNull byte[] data) {
            this(data, 0);
        }
        public Reader(@NonNull byte[] data, int offset) {
            _data = data;
            pos = offset;
        }

        public Cbor.Value read() throws ReadError {
            switch (MajorType.identify(_data[pos])) {
                case POSITIVE_INT: return value(readPositiveInt());
                case NEGATIVE_INT: return value(readNegativeInt());
                case BYTE_STRING: return value(readByteString());
                case TEXT_STRING: return value(readTextString());
                case ARRAY: return value(readArray());
                case MAP: return value(readMap());
                case SEMANTIC_TAG:
                    TagAndValue r = readTagged();
                    return value(r.Tag, r.Value);
                case SPECIAL: throw new java.lang.IllegalArgumentException("CBOR float/special type is not implemented yet");
                default: throw new RuntimeException("wat");
            }
        }

        // https://en.wikipedia.org/wiki/CBOR#Specification_of_the_CBOR_encoding
        int readPositiveInt() throws ReadError {
            // strip off the major type bits
            int shortCount = _data[pos] & 0x1F;

            if(shortCount < 23) { // directly encoded in the single byte
                pos += 1;
                return shortCount;
            } else if(shortCount == 24) { // the count is in a following 8-bit extended count field
                if(pos + 1 > _data.length) {
                    throw new ReadError.InputTooShort();
                }
                int value = _data[pos + 1] & 0xff;
                pos += 2;
                return value;
            } else if(shortCount == 25) { // the count is in a following 16-bit extended count field
                if(pos + 2 > _data.length) {
                    throw new ReadError.InputTooShort();
                }
                int value = (_data[pos + 1] & 0xff) << 8 | (_data[pos + 2] & 0xff);
                pos += 3;
                return value;
            } else if(shortCount == 26) { // the count is in a following 32-bit extended count field
                if(pos + 4 > _data.length) {
                    throw new ReadError.InputTooShort();
                }
                int value = (_data[pos + 1] & 0xff) << 24 | (_data[pos + 2] & 0xff) << 16 | (_data[pos + 3] & 0xff) << 8 | (_data[pos + 4] & 0xff);
                pos += 5;
                return value;

            } else if(shortCount == 27) { // the count is in a following 64-bit extended count field
                throw new RuntimeException("64 bit integers aren't supported yet");
            } else {
                throw new ReadError.MalformedInput(); // "Wikipedia: Values 28–30 are not assigned and must not be used."
            }
        }

        int readNegativeInt() throws ReadError {
            return (readPositiveInt() + 1) * -1;
        }

        @NonNull
        byte[] readByteString() throws ReadError {
            int len = readPositiveInt();
            if(pos+len > _data.length) {
                throw new ReadError.InputTooShort();
            }
            byte[] result = Arrays.copyOfRange(_data, pos, pos+len);
            pos += len;
            return result;
        }

        @NonNull
        String readTextString() throws ReadError {
            int len = readPositiveInt();
            if(pos+len > _data.length) {
                throw new ReadError.InputTooShort();
            }
            String result = new String(_data, pos, len, StandardCharsets.UTF_8);
            pos += len;
            return result;
        }

        @NonNull
        ArrayList<Value> readArray() throws ReadError {
            int len = readPositiveInt();
            if(pos+len > _data.length) {
                throw new ReadError.InputTooShort();
            }
            ArrayList<Value> result = new ArrayList<>(/*capacity:*/ len);
            for(int i = 0; i < len; i++) {
                result.add(read());
            }
            return result;
        }

        @NonNull
        HashMap<Value, Value> readMap() throws ReadError {
            int len = readPositiveInt();
            if(pos+len > _data.length) {
                throw new ReadError.InputTooShort();
            }
            HashMap<Value, Value> result = new HashMap<>(/*capacity:*/ len);
            for(int i = 0; i < len; i++) {
                Value key = read();
                Value value = read();
                result.put(key, value);
            }
            return result;
        }

        private static class TagAndValue {
            public final int Tag;
            @NonNull public final Cbor.Value Value;
            public TagAndValue(int tag, @NonNull Cbor.Value value) {
                Tag = tag;
                Value = value;
            }
        }

        TagAndValue readTagged() throws ReadError {
            int tag = readPositiveInt();
            Cbor.Value following = read();
            return new TagAndValue(tag, following);
        }
    }

    public static class Writer {
        // this gets appended to as we write more and more data.
        // call getBuffer to get the current stuff written so far
        private byte[] _buffer = new byte[0];
        private int _bufferPos;

        @NonNull
        public byte[] getBuffer() {
            // trim the underlying buffer which may have excess capacity. If not we still want a defensive copy
            return Arrays.copyOf(_buffer, _bufferPos);
        }

        @SuppressWarnings("ConstantConditions")
        public void write(Cbor.Value value) {
            switch(value.getType()) {
                case POSITIVE_INT:
                    writePositiveInteger(value.asInteger());
                    break;
                case NEGATIVE_INT:
                    writeNegativeInteger(value.asInteger());
                    break;
                case TEXT_STRING:
                    writeTextString(value.asString());
                    break;
                case BYTE_STRING:
                    writeByteString(value.asBytes());
                    break;
                case ARRAY:
                    writeArray(value.asList());
                    break;
                default:
                    throw new RuntimeException("unhandled Cbor.MajorType. (tagged and special are not supported yet)");
            }
        }

        private void writePositiveInteger(int value) {
            writeHeader(MajorType.POSITIVE_INT, value);
        }

        private void writeNegativeInteger(int value) {
            int countValue = (value * -1) -1;
            writeHeader(MajorType.NEGATIVE_INT, countValue);
        }

        private void writeTextString(@NonNull String value) {
            // java getBytes just returns garbage if it encounters something that isn't utf-8 encodable,
            // there's no reliable way to detect it :-(
            byte[] utf8Bytes = value.getBytes(StandardCharsets.UTF_8);

            writeHeader(MajorType.TEXT_STRING, utf8Bytes.length);
            ensureBufferCapacity(utf8Bytes.length);
            System.arraycopy(utf8Bytes, 0, _buffer, _bufferPos, utf8Bytes.length);
            _bufferPos += utf8Bytes.length;
        }

        private void writeByteString(@NonNull byte[] value) {
            writeHeader(MajorType.BYTE_STRING, value.length);
            ensureBufferCapacity(value.length);
            System.arraycopy(value, 0, _buffer, _bufferPos, value.length);
            _bufferPos += value.length;
        }

        private void writeArray(@NonNull List<Cbor.Value> value) {
            writeHeader(MajorType.ARRAY, value.size());
            for(Cbor.Value item : value) {
                write(item);
            }
        }

        private void writeHeader(MajorType majorType, long countValue) {
            byte mtBits = (byte) (majorType.getRawValue() << 5);
            if (countValue < 24) {
                // tiny encoding, the count goes inline and that's all
                ensureBufferCapacity(1);
                _buffer[_bufferPos++] = (byte)(mtBits | countValue);
            } else if (countValue < 256) {
                // 8-bit length follows in a single trailing byte
                ensureBufferCapacity(2);
                _buffer[_bufferPos++] = (byte)(mtBits | 24);
                _buffer[_bufferPos++] = (byte)countValue;
            } else if (countValue < 65536) {
                // 16-bit length follows in two trailing bytes
                ensureBufferCapacity(3);
                _buffer[_bufferPos++] = (byte)(mtBits | 25);
                _buffer[_bufferPos++] = (byte)((countValue >>> 8) & 0xff);
                _buffer[_bufferPos++] = (byte)(countValue & 0xff);
            } else if (countValue < 4294967296L) {
                // 32-bit length follows in four trailing bytes
                ensureBufferCapacity(5);
                _buffer[_bufferPos++] = (byte)(mtBits | 26);
                _buffer[_bufferPos++] = (byte)((countValue >>> 24) & 0xff);
                _buffer[_bufferPos++] = (byte)((countValue >>> 16) & 0xff);
                _buffer[_bufferPos++] = (byte)((countValue >>> 8) & 0xff);
                _buffer[_bufferPos++] = (byte)(countValue & 0xff);
            } else {
                // 64-bit length not supported in java version of Cbor yet
                throw new IllegalArgumentException("java Cbor cannot encode 64 bit integers yet");
            }
        }

        private void ensureBufferCapacity(int additionalCapacity) {
            if(_bufferPos + additionalCapacity <= _buffer.length) {
                return; // already capacity
            }
            // need to grow the buffer
            int growBy = _buffer.length * 2;
            if(growBy < additionalCapacity) {
                growBy = additionalCapacity;
            }
            _buffer = Arrays.copyOf(_buffer, _buffer.length + growBy);
        }
    }
}
