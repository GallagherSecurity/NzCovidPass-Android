package com.gallagher.nzcovidpass;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CwtSecurityTokenValidator {
    @NonNull PassVerifier.Options _options;

    public CwtSecurityTokenValidator(@NonNull PassVerifier.Options options) {
        _options = options;
    }

    // throws if the token isn't valid. Returns if it is
    public void validateToken(@NonNull CwtSecurityToken token, @Nullable Date referenceTime) throws CwtSecurityTokenValidationError {
        // validate the header
        String keyId = token.getHeader().getKeyId();
        if(keyId == null || keyId.length() == 0) {
            throw new CwtSecurityTokenValidationError.InvalidKeyId();
        }

        String alg = token.getHeader().getAlgorithm();
        if(alg == null || !_options.getValidAlgorithms().contains(alg)) {
            throw new CwtSecurityTokenValidationError.UnsupportedAlgorithm();
        }

        // validate the payload
        String jti = token.getPayload().getJti();
        if(jti == null || jti.length() == 0) {
            throw new CwtSecurityTokenValidationError.InvalidTokenId();
        }

        String issuer = token.getPayload().getIssuer();
        if(issuer == null || !_options.getValidIssuers().contains(issuer)) {
            throw new CwtSecurityTokenValidationError.InvalidIssuer();
        }

        Date nbf = token.getPayload().getNotBefore();
        if(nbf == null) {
            nbf = new Date(0); // 1st of jan 1970
        }
        Date exp = token.getPayload().getExpiry();
        if(exp == null) {
            exp = new Date(4133933999L); // 11:59pm on 31st Dec 2100
        }

        Date now = referenceTime == null ? new Date() : referenceTime;
        if(nbf.after(now)) {
            throw new CwtSecurityTokenValidationError.NotYetValid();
        }
        if(exp.before(now)) {
            throw new CwtSecurityTokenValidationError.Expired();
        }

        // validate the signature
        validateSignature(token, alg);

        // validate the credential
        VerifiableCredential cred = token.getPayload().getCredential();
        if(cred == null) {
            throw new CwtSecurityTokenValidationError.MissingCredential();
        }
        if(!cred.getContext().contains(VerifiableCredential.BASE_CONTEXT) ||
                !cred.getContext().contains(cred.getCredentialSubject().getContext())) {
            throw new CwtSecurityTokenValidationError.InvalidCredentialContext();
        }

        if(!cred.getType().contains(VerifiableCredential.BASE_CREDENTIAL_TYPE) ||
                !cred.getType().contains(cred.getCredentialSubject().getType())) {
            throw new CwtSecurityTokenValidationError.InvalidCredentialType();
        }
    }

    private void validateSignature(@NonNull CwtSecurityToken token, @NonNull String algorithm) throws CwtSecurityTokenValidationError {
        // future extension: fetch the DID from the internet and cache it rather than hardcoding
        // Note: before we get here we have already checked the token issuer against
        // options.validIssuers, so it isn't a security problem if "WellKnownIssuers" contains test keys
        String issuer = token.getPayload().getIssuer();
        if(issuer == null) { issuer = ""; }
        String keyId = token.getHeader().getKeyId();
        if(keyId == null) { keyId = ""; }
        if(!algorithm.equals(SecurityAlgorithms.ECDSA_SHA_256)) { // this only supports ES256. In future this is the extension point if we need to support more
            throw new CwtSecurityTokenValidationError.InvalidKeyParameters();
        }

        @Nullable DID.Document did;
        try {
            did = WellKnownIssuers.find(issuer, keyId);
        } catch (JSONException e) { // somehow an unparseable DID document
            did = null;
        }
        if(did == null) {
            throw new CwtSecurityTokenValidationError.InvalidIssuer();
        }

        String targetId = issuer + "#" + keyId;
        @Nullable DID.VerificationMethod verificationMethod = null;
        for(DID.VerificationMethod vf : did.getVerificationMethods()) {
            if(vf.getId().equals(targetId) && "P-256".equals(vf.getPublicKeyJwk().getCrv()) && "EC".equals(vf.getPublicKeyJwk().getKty())) {
                verificationMethod = vf;
                break;
            }
        }
        if(verificationMethod == null) {
            throw new CwtSecurityTokenValidationError.UnsupportedVerificationKeyType();
        }

        DID.JsonWebKey jwk = verificationMethod.getPublicKeyJwk();
        String xStr = jwk.getX();
        String yStr = jwk.getY();
        byte[] x;
        byte[] y;
        try {
            x = Base64.decode(xStr == null ? "" : xStr);
            y = Base64.decode(yStr == null ? "" : yStr);
        } catch (IllegalArgumentException e) {
            throw new CwtSecurityTokenValidationError.InvalidKeyParameters();
        }

        PublicKey publicKey;
        try {
            publicKey = loadP256PublicKey(x, y);
        } catch (InvalidKeySpecException e) {
            throw new CwtSecurityTokenValidationError.InvalidKeyParameters();
        }

        // the signature is generated not directly over the input, but over this derived structure
        // https://datatracker.ietf.org/doc/html/rfc8152#section-4.4
        // Note this process assumes a COSE_Sign1 structure, which NZ Covid passes should be.
        Cbor.Writer cborWriter = new Cbor.Writer();
        cborWriter.write(Cbor.value(Arrays.asList(
                // context
                Cbor.value("Signature1"),

                // body_protected
                Cbor.value(token.getHeader().getData()),

                // external_aad
                Cbor.value(new byte[0]),

                // payload
                Cbor.value(token.getPayload().getData())
        )));

        if (!verifyECDSASignature(token.getSignature(), cborWriter.getBuffer(), publicKey)) {
            throw new CwtSecurityTokenValidationError.InvalidSignature();
        }
    }

    private static boolean verifyECDSASignature(@NonNull byte[] signatureBuffer, @NonNull byte[] dataBuffer, @NonNull PublicKey publicKey) {
        // iOS wants EC signatures in ASN1 encoded format, not raw.
        byte[] asnSignatureBuffer = convertRawSignatureIntoAsn1(signatureBuffer);

        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initVerify(publicKey);
            signature.update(dataBuffer);
            return signature.verify(asnSignatureBuffer);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) { // in practice this should never happen, Android supports SHA256withECDSA back to API 11 which is Android 3.0
            return false;
        }
    }

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for(byte b: bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    @NonNull
    private static byte[] convertRawSignatureIntoAsn1(@NonNull byte[] data) {
        if(data.length != 64) {
            return new byte[0];
        }
        byte[] sigR = encodeIntegerToAsn1(data, 0, 32);
        byte[] sigS = encodeIntegerToAsn1(data, 32, 32);

        byte[] result = new byte[2 + sigR.length + sigS.length];
        result[0] = 0x30;
        result[1] = (byte)(sigR.length + sigS.length);
        System.arraycopy(sigR, 0, result, 2, sigR.length);
        System.arraycopy(sigS, 0, result, 2+sigR.length, sigS.length);
        return result;
    }

    @NonNull
    private static byte[] encodeIntegerToAsn1(@NonNull byte[] data, int offset, int count) {
        if(data.length < offset + count) {
            return new byte[0];
        }
        byte firstByte = data[offset];
        if (firstByte == 0x00) { // has an artificial leading zero, trim it
            return encodeIntegerToAsn1(data, offset+1, count-1);
        }
        byte[] result;
        if((firstByte & 0x80) == 0x80) { // high bit is set which asn1 interprets as negative, so we need a leading 0 pad
            result = new byte[3 + count];
            result[0] = 0x02;
            result[1] = (byte)(count + 1);
            result[2] = 0x00;
            System.arraycopy(data, offset, result, 3, count);
        } else { // ordinary data that requires no fiddling
            result = new byte[2 + count];
            result[0] = 0x02;
            result[1] = (byte)count;
            System.arraycopy(data, offset, result, 2, count);
        }
        return result;
    }

    private static final byte[] P256_HEAD = Base64.decode("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE");

    // thanks to Maarten Bodewes (https://stackoverflow.com/users/589259/maarten-bodewes) on https://stackoverflow.com/a/30471945
    public static ECPublicKey loadP256PublicKey(byte[] x, byte[] y) throws InvalidKeySpecException {
        if(x.length != 32 || y.length != 32) {
            throw new InvalidKeySpecException("x and y must both be 32 bytes");
        }
        byte[] encodedKey = new byte[P256_HEAD.length + 64];
        System.arraycopy(P256_HEAD, 0, encodedKey, 0, P256_HEAD.length);
        System.arraycopy(x, 0, encodedKey, P256_HEAD.length, x.length);
        System.arraycopy(y, 0, encodedKey, P256_HEAD.length + x.length, y.length);
        KeyFactory eckf;
        try {
            eckf = KeyFactory.getInstance("EC");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("EC key factory not present in runtime");
        }
        X509EncodedKeySpec ecpks = new X509EncodedKeySpec(encodedKey);
        return (ECPublicKey) eckf.generatePublic(ecpks);
    }
}

// ref https://github.com/AzureAD/azure-activedirectory-identitymodel-extensions-for-dotnet/blob/dev/src/Microsoft.IdentityModel.Tokens/SecurityAlgorithms.cs
class SecurityAlgorithms {
    @NonNull
    public static final String ECDSA_SHA_256 = "ES256";
    @NonNull
    public static final String SHA_256 = "SHA256";
    @NonNull
    public static final String SHA_512 = "SHA512";
}