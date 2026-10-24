package com.mentify.tests.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;

final class ExpiredJwtFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final long ISSUED_AT_OFFSET_SECONDS = 120;
    private static final long EXPIRATION_OFFSET_SECONDS = 60;

    private ExpiredJwtFactory() {
    }

    static String expiredCopy(String validToken, String privateKeyBase64) {
        if (privateKeyBase64 == null || privateKeyBase64.isBlank()) {
            throw new IllegalStateException(
                    "Set AUTH_JWT_PRIVATE_KEY_BASE64 to the local Auth Service signing key for this security test");
        }

        try {
            String[] segments = validToken.split("\\.", -1);
            if (segments.length != 3) {
                throw new IllegalArgumentException("The supplied access token is not a signed JWT");
            }

            byte[] payloadBytes = Base64.getUrlDecoder().decode(segments[1]);
            JsonNode payload = OBJECT_MAPPER.readTree(payloadBytes);
            if (!(payload instanceof ObjectNode claims)) {
                throw new IllegalArgumentException("JWT claims must be a JSON object");
            }

            Instant now = Instant.now();
            claims.put("iat", now.minusSeconds(ISSUED_AT_OFFSET_SECONDS).getEpochSecond());
            claims.put("exp", now.minusSeconds(EXPIRATION_OFFSET_SECONDS).getEpochSecond());

            String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(OBJECT_MAPPER.writeValueAsBytes(claims));
            String signingInput = segments[0] + "." + encodedPayload;
            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(privateKey(privateKeyBase64));
            signer.update(signingInput.getBytes(StandardCharsets.US_ASCII));
            String encodedSignature = Base64.getUrlEncoder().withoutPadding().encodeToString(signer.sign());
            return signingInput + "." + encodedSignature;
        } catch (IOException | GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unable to create a correctly signed expired JWT", exception);
        }
    }

    private static PrivateKey privateKey(String encodedKey) throws GeneralSecurityException {
        String normalized = encodedKey.trim()
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(normalized);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }
}
