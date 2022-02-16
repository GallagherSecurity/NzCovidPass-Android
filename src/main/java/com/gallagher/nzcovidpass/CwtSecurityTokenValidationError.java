package com.gallagher.nzcovidpass;

public abstract class CwtSecurityTokenValidationError extends Exception {
    /// The key Id field is missing from the header
    public static class InvalidKeyId extends CwtSecurityTokenValidationError { }

    /// The algorithm isn't in our approved list
    public static class UnsupportedAlgorithm extends CwtSecurityTokenValidationError { }

    /// The JTI field is missing from the payload
    public static class InvalidTokenId extends CwtSecurityTokenValidationError { }

    /// The issuer isn't in our approved list
    public static class InvalidIssuer extends CwtSecurityTokenValidationError { }

    /// The "notBefore" time is after the "expiry" which indicates a malformed pass
    public static class InvalidDateRange extends CwtSecurityTokenValidationError { }

    /// The "notBefore" time hasn't arrived yet.
    public static class NotYetValid extends CwtSecurityTokenValidationError { }

    /// The "expiry" time is in the past.
    public static class Expired extends CwtSecurityTokenValidationError { }

    /// The token does not contain a verifiable credential
    public static class MissingCredential extends CwtSecurityTokenValidationError { }

    /// The credential's context is not in the approved list
    public static class InvalidCredentialContext extends CwtSecurityTokenValidationError { }

    /// The credential's type is not in the approved list
    public static class InvalidCredentialType extends CwtSecurityTokenValidationError { }

    /// The issuer JWK was not of a type that we know how to verify
    public static class UnsupportedVerificationKeyType extends CwtSecurityTokenValidationError { }

    /// The issuer JWK key x and y values were missing or malformed
    public static class InvalidKeyParameters extends CwtSecurityTokenValidationError { }

    /// The signature was not verifiable; either the data has been tampered with or it was signed with a different key
    public static class InvalidSignature extends CwtSecurityTokenValidationError { }
}
