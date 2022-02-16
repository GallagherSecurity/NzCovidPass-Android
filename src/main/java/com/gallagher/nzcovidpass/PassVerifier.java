package com.gallagher.nzcovidpass;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class PassVerifier {
    public static class Options {
        @NonNull
        private final String _prefix;
        private final int _version;
        @NonNull
        private final List<String> _validIssuers;
        @NonNull
        private final List<String> _validAlgorithms;
        // todo securityKeyCacheTime?

        public Options() {
            this(DEFAULT_PREFIX, DEFAULT_VERSION, DEFAULT_VALID_ISSUERS, DEFAULT_VALID_ALGORITHMS);
        }

        // this is really the thing we want to vary most commonly
        public Options(@NonNull List<String> validIssuers) {
            this(DEFAULT_PREFIX, DEFAULT_VERSION, validIssuers, DEFAULT_VALID_ALGORITHMS);
        }

        public Options(@NonNull String prefix, int version, @NonNull List<String> validIssuers, @NonNull List<String> validAlgorithms) {
            _prefix = prefix;
            _version = version;
            _validIssuers = validIssuers;
            _validAlgorithms = validAlgorithms;
        }

        @NonNull
        public String getPrefix() {
            return _prefix;
        }
        public int getVersion() {
            return _version;
        }
        @NonNull
        public List<String> getValidIssuers() {
            return _validIssuers;
        }
        @NonNull
        public List<String> getValidAlgorithms() {
            return _validAlgorithms;
        }
    }

    @NonNull
    public static final String DEFAULT_PREFIX = "NZCP:";
    public static final int DEFAULT_VERSION = 1;
    @NonNull
    public static final List<String> DEFAULT_VALID_ISSUERS = Collections.singletonList(WellKnownIssuerNames.NZCP);
    @NonNull
    public static final List<String> DEFAULT_VALID_ALGORITHMS = Collections.singletonList(SecurityAlgorithms.ECDSA_SHA_256);

    @NonNull
    final Options _options;

    public PassVerifier(@NonNull Options options) {
        _options = options;
    }

    public PassVerifier(@NonNull List<String> validIssuers) {
        _options = new Options(validIssuers);
    }

    // throws CwtSecurityTokenError on failure to parse, TokenValidationError on failure to validate, PassVerificationError on generic error
    @NonNull
    public CwtSecurityToken verify(@NonNull String passPayload) throws PassVerificationError, CwtSecurityTokenError, CwtSecurityTokenValidationError {
        return verify(passPayload, new Date());
    }

    @NonNull
    public CwtSecurityToken verify(@NonNull String passPayload, @Nullable Date referenceTime) throws PassVerificationError, CwtSecurityTokenError, CwtSecurityTokenValidationError {
        String[] passComponents = passPayload.split("/");
        validatePassComponents(passComponents);

        byte[] payload;
        try {
            payload = Base32.decode(passComponents[2]);
        } catch (IllegalArgumentException e) {
            throw new PassVerificationError.InvalidPayloadEncoding();
        }

        // Decode the payload and read the CWT contained
        CwtSecurityToken token = new CwtSecurityToken(payload);

        // Validate token claims and signature
        CwtSecurityTokenValidator validator = new CwtSecurityTokenValidator(_options);
        validator.validateToken(token, referenceTime);

        return token;
    }

    public void validatePassComponents(@NonNull String[] components) throws PassVerificationError {
        if(components.length != 3) {
            throw new PassVerificationError.InvalidPassComponents();
        }

        String prefix = components[0];
        String version = components[1];
        String payload = components[2];

        if(!prefix.equals(_options.getPrefix())) {
            throw new PassVerificationError.InvalidPrefix();
        }

        if(!version.equals(Integer.toString(_options.getVersion()))) { // don't need to parse the string, we're just checking it
            throw new PassVerificationError.InvalidVersion();
        }

        if(payload.length() == 0) {
            throw new PassVerificationError.MissingPayload();
        }
    }
}

