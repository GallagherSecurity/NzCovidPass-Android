package com.gallagher.nzcovidpass;

import androidx.annotation.NonNull;

import java.util.List;

public class VerifiableCredential {
    /// The JSON-LD context property value associated with the base verifiable credential structure.
    /// see https://www.w3.org/TR/vc-data-model/#contexts"
    public static final String BASE_CONTEXT = "https://www.w3.org/2018/credentials/v1";

    /// The type property value associated with the base verifiable credential type.
    /// see https://www.w3.org/TR/vc-data-model/#types
    public static final String BASE_CREDENTIAL_TYPE = "VerifiableCredential";

    @NonNull
    private final String _version;

    @NonNull
    private final List<String> _context;

    @NonNull
    private final List<String> _type;

    // C# has VerifiableCredential<T> but in practice we only ever have PublicCovidPass so we can strip the generics
    @NonNull
    private final PublicCovidPass _credentialSubject;

    public VerifiableCredential(@NonNull String version, @NonNull List<String> context, @NonNull List<String> type, @NonNull PublicCovidPass credentialSubject) {
        _version = version;
        _context = context;
        _type = type;
        _credentialSubject = credentialSubject;
    }

    @NonNull
    public String getVersion() {
        return _version;
    }
    @NonNull
    public List<String> getContext() {
        return _context;
    }
    @NonNull
    public List<String> getType() {
        return _type;
    }
    @NonNull
    public PublicCovidPass getCredentialSubject() {
        return _credentialSubject;
    }
}
