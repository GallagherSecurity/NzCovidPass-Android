package com.gallagher.nzcovidpass;

public abstract class CwtSecurityTokenError extends Exception {
    // the root CBOR structure must be a taggged COSE Single Signer value
    public static class NotCoseSingleSignerObject extends CwtSecurityTokenError {}

    public static class CoseSingleSignerObjectInvalidPayload extends CwtSecurityTokenError {}

    // not a parseable CBOR structure
    public static class InvalidTokenFormat extends CwtSecurityTokenError {}
}
