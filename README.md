# NzCovidPass-Android
Java / Android library for verification of the NZ Covid Vaccination Pass according to https://nzcp.covid19.health.nz/

Copyright (c) 2022 Gallagher Group Ltd

Licensed under the MIT License

## Overview:

This is a self-contained implementation of a verifier for the NZ COVID Pass format, written entirely in Java, targeting the Android platform.
It does not rely on any external servers, API's or other things, and can function purely offline. The only third party libraries it relies on are org.json for JSON parsing, and androidx annotations to express nullability

### Using the library:

Clone the git repository and add the library in as a module in your Android Studio project. A git submodule may be appropriate

### Example (host app in kotlin):

```kotlin
import com.gallagher.nzcovidpass.*;

val passPayload = "NZCP:/1/...." // get this from scanning a QR code
val allowedIssuerNames = if(BuildConfig.DEBUG)
    listOf(WellKnownIssuerNames.NZCP, WellKnownIssuerNames.NZCP_TEST) else
    listOf(WellKnownIssuerNames.NZCP)

val verifier = PassVerifier(allowedIssuerNames)
try {

    val passContents = verifier.verify(barcode)
    val givenName = passContents.payload.credential?.credentialSubject?.givenName ?: ""
    val familyNameCandidate = passContents.payload.credential?.credentialSubject?.familyName
    val dateOfBirth = passContents.payload.credential?.credentialSubject?.dateOfBirth ?: ""

    // no need for translatable string formatting, this is the New Zealand covid pass specifically
    val fullName = if (familyNameCandidate != null) {
        "$givenName $familyNameCandidate"
    } else {
        givenName
    }

    val expiry = passContents.payload.expiry ?: Date(0)
            
} catch (err: Exception) {
    // translate the covid pass internal error into a simplified one with just a message, for the view to display
    if(error is PassVerificationError.InvalidPrefix || error is PassVerificationError.InvalidPassComponents) {
        print("This is not an NZ Covid Pass QR Code.")
    } else if(error is CwtSecurityTokenValidationError.Expired) {
        print("Pass Expired.")
    } else if(error is CwtSecurityTokenValidationError.NotYetValid) {
        print("Pass Not Active.")
    } else {
        // any kind of structural or signature error just results in "not issued by the ministry of health"
        print("This pass was not issued by the Ministry of Health.")
    }
}
```

## Notes:
Currently this does not dynamically download DID documents (public keys); rather the NZCP test key, and production key z12Kf7UQ are embedded in the source code. This has the advantage that it always works offline, there is no "first run" internet connection required, however it does mean if the ministry of health issues a new production keypair, then the library will need to be updated.

We expect to add dynamic downloading of DID documents in future.

## Acknowledgements:

This library was implemented using the .NET NZ Covid Pass verifier by Jed Simson https://github.com/JedS6391/NzCovidPass as a reference.

While this library shares some structure, it is not a direct port. It follows different patterns and practices to suit Java, Kotlin and Android.
Some parts needed to be added (CBOR and Base32), and some parts have been simplified, however credit must be given to Jed; his implementation helped us develop this library more quickly and to a higher degree of quality.
