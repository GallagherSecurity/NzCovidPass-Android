package com.gallagher.nzcovidpass;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.UUID;

public class CwtSecurityTokenReaderTests {
    @Test
    public void testReadTokenSuccessful() throws CwtSecurityTokenError, Cbor.ReadError, ParseException {
        // this one is from nzcp.covid19.health.nz, also used by the C# project
        byte[] payload = Base32.decode("2KCEVIQEIVVWK6JNGEASNICZAEP2KALYDZSGSZB2O5SWEOTOPJRXALTDN53GSZBRHEXGQZLBNR2GQLTOPICRUYMBTIFAIGTUKBAAUYTWMOSGQQDDN5XHIZLYOSBHQJTIOR2HA4Z2F4XXO53XFZ3TGLTPOJTS6MRQGE4C6Y3SMVSGK3TUNFQWY4ZPOYYXQKTIOR2HA4Z2F4XW46TDOAXGG33WNFSDCOJONBSWC3DUNAXG46RPMNXW45DFPB2HGL3WGFTXMZLSONUW63TFGEXDALRQMR2HS4DFQJ2FMZLSNFTGSYLCNRSUG4TFMRSW45DJMFWG6UDVMJWGSY2DN53GSZCQMFZXG4LDOJSWIZLOORUWC3CTOVRGUZLDOSRWSZ3JOZSW4TTBNVSWISTBMNVWUZTBNVUWY6KOMFWWKZ2TOBQXE4TPO5RWI33CNIYTSNRQFUYDILJRGYDVAYFE6VGU4MCDGK7DHLLYWHVPUS2YIDJOA6Y524TD3AZRM263WTY2BE4DPKIF27WKF3UDNNVSVWRDYIYVJ65IRJJJ6Z25M2DO4YZLBHWFQGVQR5ZLIWEQJOZTS3IQ7JTNCFDX");

        CwtSecurityToken token = new CwtSecurityToken(payload);
        assertEquals("key-1", token.getHeader().getKeyId());
        assertEquals("ES256", token.getHeader().getAlgorithm());

        assertEquals(UUID.fromString("60A4F54D-4E30-4332-BE33-AD78B1EAFA4B"), token.getPayload().getCti());
        assertEquals("did:web:nzcp.covid19.health.nz", token.getPayload().getIssuer());

        DateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        assertEquals(iso8601.parse("2031-11-02T20:05:30+0000"), token.getPayload().getExpiry());
        assertEquals(iso8601.parse("2021-11-02T20:05:30+0000"), token.getPayload().getNotBefore());

        VerifiableCredential credential = token.getPayload().getCredential();
        if(credential == null) {
            throw new ParseException("no credential", 0); // wrong exception but it'll do
        }

        assertEquals("1.0.0", credential.getVersion());
        assertEquals(Arrays.asList("VerifiableCredential", "PublicCovidPass"), credential.getType());
        assertEquals(Arrays.asList("https://www.w3.org/2018/credentials/v1", "https://nzcp.covid19.health.nz/contexts/v1"), credential.getContext());
        assertEquals("Jack", credential.getCredentialSubject().getGivenName());
        assertEquals("Sparrow", credential.getCredentialSubject().getFamilyName());
        assertEquals("1960-04-16", credential.getCredentialSubject().getDateOfBirth());
    }

    @Test
    public void testReadExpiredPass() throws CwtSecurityTokenError, Cbor.ReadError, ParseException {
        byte[] payload = Base32.decode("2KCEVIQEIVVWK6JNGEASNICZAEP2KALYDZSGSZB2O5SWEOTOPJRXALTDN53GSZBRHEXGQZLBNR2GQLTOPICRUX5AM2FQIGTBPBPYWYTWMOSGQQDDN5XHIZLYOSBHQJTIOR2HA4Z2F4XXO53XFZ3TGLTPOJTS6MRQGE4C6Y3SMVSGK3TUNFQWY4ZPOYYXQKTIOR2HA4Z2F4XW46TDOAXGG33WNFSDCOJONBSWC3DUNAXG46RPMNXW45DFPB2HGL3WGFTXMZLSONUW63TFGEXDALRQMR2HS4DFQJ2FMZLSNFTGSYLCNRSUG4TFMRSW45DJMFWG6UDVMJWGSY2DN53GSZCQMFZXG4LDOJSWIZLOORUWC3CTOVRGUZLDOSRWSZ3JOZSW4TTBNVSWISTBMNVWUZTBNVUWY6KOMFWWKZ2TOBQXE4TPO5RWI33CNIYTSNRQFUYDILJRGYDVA56TNJCCUN2NVK5NGAYOZ6VIWACYIBM3QXW7SLCMD2WTJ3GSEI5JH7RXAEURGATOHAHXC2O6BEJKBSVI25ICTBR5SFYUDSVLB2F6SJ63LWJ6Z3FWNHOXF6A2QLJNUFRQNTRU");

        CwtSecurityToken token = new CwtSecurityToken(payload);
        assertEquals("key-1", token.getHeader().getKeyId());
        assertEquals("ES256", token.getHeader().getAlgorithm());

        assertEquals(UUID.fromString("77D36A44-2A37-4DAA-BAD3-030ECFAA8B00"), token.getPayload().getCti());
        assertEquals("did:web:nzcp.covid19.health.nz", token.getPayload().getIssuer());

        DateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        assertEquals(iso8601.parse("2021-10-26T20:05:31+0000"), token.getPayload().getExpiry());
        assertEquals(iso8601.parse("2020-11-02T20:05:31+0000"), token.getPayload().getNotBefore());

        VerifiableCredential credential = token.getPayload().getCredential();
        if(credential == null) {
            throw new ParseException("no credential", 0); // wrong exception but it'll do
        }

        assertEquals("1.0.0", credential.getVersion());
        assertEquals(Arrays.asList("VerifiableCredential", "PublicCovidPass"), credential.getType());
        assertEquals(Arrays.asList("https://www.w3.org/2018/credentials/v1", "https://nzcp.covid19.health.nz/contexts/v1"), credential.getContext());
        assertEquals("Jack", credential.getCredentialSubject().getGivenName());
        assertEquals("Sparrow", credential.getCredentialSubject().getFamilyName());
        assertEquals("1960-04-16", credential.getCredentialSubject().getDateOfBirth());
    }
}
