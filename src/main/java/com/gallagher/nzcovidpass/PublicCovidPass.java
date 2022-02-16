package com.gallagher.nzcovidpass;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PublicCovidPass {
    @NonNull
    private final String _givenName; // required

    @Nullable
    private final String _familyName; // optional

    @NonNull
    private final String _dateOfBirth; // required // contains a simplified string like "1960-04-27"

    public PublicCovidPass(@NonNull String givenName, @Nullable String familyName, @NonNull String dateOfBirth) {
        _givenName = givenName;
        _familyName = familyName;
        _dateOfBirth = dateOfBirth;
    }

    // see https://nzcp.covid19.health.nz/#verifiable-credential-claim-structure
    @NonNull
    public String getContext() { return "https://nzcp.covid19.health.nz/contexts/v1"; }

    // see https://nzcp.covid19.health.nz/#publiccovidpass
    @NonNull
    public String getType() { return "PublicCovidPass"; }

    @NonNull public String getGivenName() { return _givenName; }
    @Nullable public String getFamilyName() { return _familyName; }
    @NonNull public String getDateOfBirth() { return _dateOfBirth; }
}
