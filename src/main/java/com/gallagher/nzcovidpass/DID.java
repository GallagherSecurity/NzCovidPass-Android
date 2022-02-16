package com.gallagher.nzcovidpass;

import static com.gallagher.nzcovidpass.Util.optStringNullable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// container-as-namespace
public class DID {
    public static class JsonWebKey {
        @Nullable
        private final String _kty;
        @Nullable
        private final String _crv;
        @Nullable
        private final String _x; // base64Url encoded data
        @Nullable
        private final String _y; // base64Url encoded data

        // JsonWebKey can have many more fields, but these are the only ones the covid pass uses

        public JsonWebKey(@Nullable String kty, @Nullable String crv, @Nullable String x, @Nullable String y) {
            _kty = kty;
            _crv = crv;
            _x = x;
            _y = y;
        }

        public JsonWebKey(@NonNull JSONObject jsonObject) {
            _kty = optStringNullable(jsonObject, "kty");
            _crv = optStringNullable(jsonObject, "crv");
            _x = optStringNullable(jsonObject, "x");
            _y = optStringNullable(jsonObject, "y");
        }

        @Nullable
        public String getKty() {
            return _kty;
        }
        @Nullable
        public String getCrv() {
            return _crv;
        }
        @Nullable
        public String getX() {
            return _x;
        }
        @Nullable
        public String getY() {
            return _y;
        }
    }

    public static class VerificationMethod {
        @NonNull
        private final String _id;

        @NonNull
        private final String _controller;

        @NonNull
        private final String _type;

        @NonNull
        private final JsonWebKey _publicKeyJwk;

        public VerificationMethod(@NonNull String id, @NonNull String controller, @NonNull String type, @NonNull JsonWebKey publicKeyJwk) {
            _id = id;
            _controller = controller;
            _type = type;
            _publicKeyJwk = publicKeyJwk;
        }

        public VerificationMethod(@NonNull JSONObject jsonObject) throws JSONException {
            _id = jsonObject.getString("id");
            _controller = jsonObject.getString("controller");
            _type = jsonObject.getString("type");

            JSONObject jwk = jsonObject.getJSONObject("publicKeyJwk");
            _publicKeyJwk = new JsonWebKey(jwk);
        }

        @NonNull
        public String getId() {
            return _id;
        }

        @NonNull
        public String getController() {
            return _controller;
        }

        @NonNull
        public String getType() {
            return _type;
        }

        @NonNull
        public JsonWebKey getPublicKeyJwk() {
            return _publicKeyJwk;
        }
    }

    public static class Document {
        @NonNull
        private final String _id;

        @NonNull
        private final List<String> _contexts;

        @NonNull
        private final List<VerificationMethod> _verificationMethods;

        @NonNull
        private final List<String> _assertionMethods;

        public Document(@NonNull String id, @NonNull List<String> contexts, @NonNull List<VerificationMethod> verificationMethods, @NonNull List<String> assertionMethods) {
            _id = id;
            _contexts = contexts;
            _verificationMethods = verificationMethods;
            _assertionMethods = assertionMethods;
        }

        public Document(@NonNull JSONObject jsonObject) throws JSONException {
            _id = jsonObject.getString("id");

            JSONArray contextJsonArray = jsonObject.optJSONArray("@context");
            if(contextJsonArray == null) { // not an array, must be a single string
                String singleContext = jsonObject.getString("@context");
                _contexts = Collections.singletonList(singleContext);
            } else {
                List<String> result = new ArrayList<>();
                for(int i = 0; i < contextJsonArray.length(); i++) {
                     result.add(contextJsonArray.getString(i)); // this will throw a JSON exception if the context array contains a non-string
                }
                _contexts = result;
            }

            JSONArray vfJsonArray = jsonObject.getJSONArray("verificationMethod");
            List<VerificationMethod> vfList = new ArrayList<>();
            for(int i = 0; i < vfJsonArray.length(); i++) {
                vfList.add(new VerificationMethod(vfJsonArray.getJSONObject(i))); // throws if the structure is invalid
            }
            _verificationMethods = vfList;

            JSONArray amJsonArray = jsonObject.getJSONArray("assertionMethod");
            List<String> amList = new ArrayList<>();
            for(int i = 0; i < amJsonArray.length(); i++) {
                amList.add(amJsonArray.getString(i));
            }
            _assertionMethods = amList;
        }

        @NonNull
        public String getId() {
            return _id;
        }

        @NonNull
        public List<String> getContexts() {
            return _contexts;
        }

        @NonNull
        public List<VerificationMethod> getVerificationMethods() {
            return _verificationMethods;
        }

        @NonNull
        public List<String> getAssertionMethods() {
            return _assertionMethods;
        }
    }
}

// normally we would resolve DID web keys by going to https://<base>/.well-known/did.json
// however we don't need to do that until the ministry of health rotates their keys, so we
// can build the defaults in for now
class WellKnownIssuers {
    // test keys
    @NonNull
    private static final String nzcpCovid19HealthNzKey1_raw =
"{" +
"  \"@context\": \"https://w3.org/ns/did/v1\"," +
"  \"id\": \"did:web:nzcp.covid19.health.nz\"," +
"  \"verificationMethod\": [" +
"    {" +
"      \"id\": \"did:web:nzcp.covid19.health.nz#key-1\"," +
"      \"controller\": \"did:web:nzcp.covid19.health.nz\"," +
"      \"type\": \"JsonWebKey2020\"," +
"      \"publicKeyJwk\": {" +
"        \"kty\": \"EC\"," +
"        \"crv\": \"P-256\"," +
"        \"x\": \"zRR-XGsCp12Vvbgui4DD6O6cqmhfPuXMhi1OxPl8760\"," +
"        \"y\": \"Iv5SU6FuW-TRYh5_GOrJlcV_gpF_GpFQhCOD8LSk3T0\"" +
"      }" +
"    }" +
"  ]," +
"  \"assertionMethod\": [" +
"    \"did:web:nzcp.covid19.health.nz#key-1\"" +
"  ]" +
"}";

    // This is a real key used to validate real covid passes
    @NonNull
    private static final String nzcpIdentityHealthNzKey_z12Kf_raw =
"{" +
"  \"@context\": [" +
"    \"https://w3.org/ns/did/v1\"," +
"    \"https://w3id.org/security/suites/jws-2020/v1\"" +
"  ]," +
"  \"id\": \"did:web:nzcp.identity.health.nz\"," +
"  \"verificationMethod\": [" +
"    {" +
"      \"id\": \"did:web:nzcp.identity.health.nz#z12Kf7UQ\"," +
"      \"controller\": \"did:web:nzcp.identity.health.nz\"," +
"      \"type\": \"JsonWebKey2020\"," +
"      \"publicKeyJwk\": {" +
"        \"kty\": \"EC\"," +
"        \"crv\": \"P-256\"," +
"        \"x\": \"DQCKJusqMsT0u7CjpmhjVGkHln3A3fS-ayeH4Nu52tc\"," +
"        \"y\": \"lxgWzsLtVI8fqZmTPPo9nZ-kzGs7w7XO8-rUU68OxmI\"" +
"      }" +
"    }" +
"  ]," +
"  \"assertionMethod\": [" +
"    \"did:web:nzcp.identity.health.nz#z12Kf7UQ\"" +
"  ]" +
"}";

    @Nullable private static List<DID.Document> _issuers;

    @Nullable
    public static DID.Document find(@NonNull String issuer, @NonNull String keyId) throws JSONException {
        // load issuers if need be.
        // TODO this is where we would go off to the internet and fetch the issuer, should we need to
        if(_issuers == null) {
            _issuers = new ArrayList<>();
            _issuers.add(new DID.Document(new JSONObject(nzcpCovid19HealthNzKey1_raw)));
            _issuers.add(new DID.Document(new JSONObject(nzcpIdentityHealthNzKey_z12Kf_raw)));
        }

        String assertionMethod = issuer + "#" + keyId;
        for(DID.Document did : _issuers) {
            if(did.getId().equals(issuer) && did.getAssertionMethods().contains(assertionMethod)) {
                return did;
            }
        }
        return null;
    }

}

class Util {
    public static @Nullable String optStringNullable(@NonNull JSONObject object, @NonNull String name) {
        Object value = object.opt(name);

        return value == null ? null : value.toString();
    }
}


