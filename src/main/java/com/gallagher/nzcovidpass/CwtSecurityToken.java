package com.gallagher.nzcovidpass;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CwtSecurityToken {
    private @NonNull
    Header _header;
    private @NonNull
    Payload _payload;
    private @NonNull
    byte[] _signature;

    public CwtSecurityToken(@NonNull byte[] data) throws CwtSecurityTokenError {
        try {
            Cbor.Reader reader = new Cbor.Reader(data);
            Cbor.Value outer = reader.read();
            if (!(outer instanceof Cbor.Value.Tagged)) {
                throw new CwtSecurityTokenError.NotCoseSingleSignerObject();
            }
            Cbor.Value.Tagged tagged = (Cbor.Value.Tagged) outer;
            if (tagged.getTag() != 18) {
                throw new CwtSecurityTokenError.NotCoseSingleSignerObject();
            }

            List<Cbor.Value> coseStructure = tagged.getValue().asList();
            if (coseStructure == null || coseStructure.size() != 4) {
                throw new CwtSecurityTokenError.CoseSingleSignerObjectInvalidPayload();
            }

            // pick up actual contents
            byte[] headerBytes = coseStructure.get(0).asBytes();
            byte[] payloadBytes = coseStructure.get(2).asBytes();
            byte[] signatureBytes = coseStructure.get(3).asBytes();
            if (headerBytes == null || payloadBytes == null || signatureBytes == null) {
                throw new CwtSecurityTokenError.CoseSingleSignerObjectInvalidPayload();
            }

            // A CBOR Map is binary encoded, then written into a byte-string in a CBOR wrapper. COSE and CWT are ridiculous
            Cbor.Reader headerReader = new Cbor.Reader(headerBytes);
            Map<Cbor.Value, Cbor.Value> headerMap = headerReader.readMap();

            Cbor.Reader payloadReader = new Cbor.Reader(payloadBytes);
            Map<Cbor.Value, Cbor.Value> payloadMap = payloadReader.readMap();

            _header = new Header(headerMap, headerBytes);
            _payload = new Payload(payloadMap, payloadBytes);
            _signature = signatureBytes;
        } catch (Cbor.ReadError e) {
            throw new CwtSecurityTokenError.InvalidTokenFormat();
        }
    }

    @NonNull
    public Header getHeader() {
        return _header;
    }

    @NonNull
    public Payload getPayload() {
        return _payload;
    }

    @NonNull
    public byte[] getSignature() {
        return _signature;
    }

    // wrapper which helps us unpack the CBOR Cwt Header structure
    // we don't pre-parse anything though for some reason (copied from .NET impl)
    public static class Header {
        @NonNull
        private final Map<Cbor.Value, Cbor.Value> _claims;

        // preserve the original data so we can verify the signature exactly
        @NonNull
        private final byte[] _data;

        Header(@NonNull Map<Cbor.Value, Cbor.Value> claims, @NonNull byte[] data) {
            _claims = claims;
            _data = data;
        }

        @NonNull
        public byte[] getData() {
            return _data;
        }

        @Nullable
        String getKeyId() {
            @Nullable Cbor.Value claim = _claims.get(Cbor.value(ClaimIds.Header.KEY_ID));
            if(!(claim instanceof Cbor.Value.ByteString)) {
                return null;
            }
            return new String(claim.asBytes(), StandardCharsets.UTF_8);
        }

        @Nullable
        String getAlgorithm() {
            @Nullable Cbor.Value claim = _claims.get(Cbor.value(ClaimIds.Header.ALGORITHM));
            if(!(claim instanceof Cbor.Value.Integer)) {
                return null;
            }
            return ClaimIds.Header.algorithmMap.get(claim.asInteger());
        }
    }

    // wrapper which helps us unpack the CBOR Cwt Header structure
    public static class Payload {
        @NonNull
        private final Map<Cbor.Value, Cbor.Value> _claims;

        // preserve the original data so we can verify the signature exactly
        @NonNull
        private final byte[] _data;

        Payload(@NonNull Map<Cbor.Value, Cbor.Value> claims, @NonNull byte[] data) {
            _claims = claims;
            _data = data;
        }

        @NonNull
        public byte[] getData() {
            return _data;
        }

        @Nullable
        public String getJti() {
            UUID cti = getCti();
            if(cti == null) {
                return null;
            }
            return "urn:uuid:" + cti.toString();
        }

        @Nullable
        public UUID getCti() {
            @Nullable Cbor.Value claim = _claims.get(Cbor.value(ClaimIds.Payload.CTI));
            byte[] bytes = claim != null ? claim.asBytes() : null;
            if(bytes == null || bytes.length != 16) {
                return null;
            }

            long msb = (long)(bytes[0] & 0xff) << 56 | (long)(bytes[1] & 0xff) << 48 | (long)(bytes[2] & 0xff) << 40 | (long)(bytes[3] & 0xff) << 32 | (long)(bytes[4] & 0xff) << 24 | (long)(bytes[5] & 0xff) << 16 | (long)(bytes[6] & 0xff) << 8 | (long)(bytes[7] & 0xff);
            long lsb = (long)(bytes[8] & 0xff) << 56 | (long)(bytes[9] & 0xff) << 48 | (long)(bytes[10] & 0xff) << 40 | (long)(bytes[11] & 0xff) << 32 | (long)(bytes[12] & 0xff) << 24 | (long)(bytes[13] & 0xff) << 16 | (long)(bytes[14] & 0xff) << 8 | (long)(bytes[15] & 0xff);

            return new UUID(msb, lsb);
        }

        @Nullable
        public String getIssuer() {
            @Nullable Cbor.Value claim = _claims.get(Cbor.value(ClaimIds.Payload.ISS));
            return claim != null ? claim.asString() : null;
        }

        @Nullable
        public Date getExpiry() {
            @Nullable Cbor.Value claim = _claims.get(Cbor.value(ClaimIds.Payload.EXP));
            Integer i = claim != null ? claim.asInteger() : null;
            if(i == null) {
                return null;
            }
            return new Date((long)i * 1000);
        }

        @Nullable
        public Date getNotBefore() {
            @Nullable Cbor.Value claim = _claims.get(Cbor.value(ClaimIds.Payload.NBF));
            Integer i = claim != null ? claim.asInteger() : null;
            if(i == null) {
                return null;
            }
            return new Date((long)i * 1000);
        }

        @Nullable
        public VerifiableCredential getCredential() {
            @Nullable Cbor.Value claim = _claims.get(Cbor.value(ClaimIds.Payload.VC));
            Map<Cbor.Value, Cbor.Value> map = claim != null ? claim.asMap() : null;
            if(map == null) {
                return null;
            }
            // the C# one converts the CBOR into JSON and then uses Newtonsoft to parse the JSON back out again. I don't know why it does this
            Cbor.Value versionCbor = map.get(Cbor.value("version"));
            String version = versionCbor != null ? versionCbor.asString() : null;

            Cbor.Value contextsCbor = map.get(Cbor.value("@context"));
            List<Cbor.Value> contexts = contextsCbor != null ? contextsCbor.asList() : null;

            Cbor.Value typesCbor = map.get(Cbor.value("type"));
            List<Cbor.Value> types = typesCbor != null ? typesCbor.asList() : null;

            Cbor.Value subjectCbor = map.get(Cbor.value("credentialSubject"));
            Map<Cbor.Value, Cbor.Value> subject = subjectCbor != null ? subjectCbor.asMap() : null;

            if(version == null || contexts == null || types == null || subject == null) {
                return null;
            }

            // unpack the context and type arrays
            ArrayList<String> contextValues = new ArrayList<>();
            for(Cbor.Value v : contexts) {
                String s = v.asString();
                if(s != null) {
                    contextValues.add(s);
                }
            }
            ArrayList<String> typeValues = new ArrayList<>();
            for(Cbor.Value v : types) {
                String s = v.asString();
                if(s != null) {
                    typeValues.add(s);
                }
            }

            // now the nested PublicCovidPass
            Cbor.Value givenNameCbor = subject.get(Cbor.value("givenName"));
            String givenName = givenNameCbor != null ? givenNameCbor.asString() : null;

            Cbor.Value familyNameCbor = subject.get(Cbor.value("familyName"));
            String familyName = familyNameCbor != null ? familyNameCbor.asString() : null;

            Cbor.Value dobCbor = subject.get(Cbor.value("dob"));
            String dob = dobCbor != null ? dobCbor.asString() : null;

            if(givenName == null || dob == null) {
                return null;
            }

            PublicCovidPass pass = new PublicCovidPass(givenName, familyName, dob);
            return new VerifiableCredential(version, contextValues, typeValues, pass);
        }
    }

    static class ClaimIds {
        static class Header {
            public static final int ALGORITHM = 1;
            public static final int KEY_ID = 4;

            // https://github.com/AzureAD/azure-activedirectory-identitymodel-extensions-for-dotnet/blob/dev/src/Microsoft.IdentityModel.Tokens/SecurityAlgorithms.cs
            @NonNull
            public static final Map<Integer, String> algorithmMap;

            static {
                algorithmMap = new HashMap<>();
                algorithmMap.put(-7, SecurityAlgorithms.ECDSA_SHA_256);
                algorithmMap.put(-16, SecurityAlgorithms.SHA_256);
                algorithmMap.put(-44, SecurityAlgorithms.SHA_512);
            }
        }

        static class Payload {
            public static final int ISS = 1;
            public static final int EXP = 4;
            public static final int NBF = 5;
            public static final int CTI = 7;
            public static final String VC = "vc";
        }
    }
}
