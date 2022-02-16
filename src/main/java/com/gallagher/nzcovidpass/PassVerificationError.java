package com.gallagher.nzcovidpass;

public abstract class PassVerificationError extends Exception {
    public static class InvalidPassComponents extends PassVerificationError {} // pass payload must be in the form <prefix>:/<version>/<base32-encoded-CWT>'
    public static class InvalidPrefix extends PassVerificationError {}
    public static class InvalidVersion extends PassVerificationError {}
    public static class MissingPayload extends PassVerificationError {}
    public static class InvalidPayloadEncoding extends PassVerificationError {}
    public static class MissingCredentialSubject extends PassVerificationError {}
}
