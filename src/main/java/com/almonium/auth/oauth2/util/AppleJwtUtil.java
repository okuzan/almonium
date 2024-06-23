package com.almonium.auth.oauth2.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppleJwtUtil {
    @Value("${app.oauth2.appleTokenUrl}")
    String appleTokenUrl;

    @Value("${spring.security.oauth2.client.provider.apple.jwk-set-uri}")
    String appleJwkUri;

    @Value("${app.oauth2.appleServiceId}")
    String appleServiceId;

    @SneakyThrows
    public Map<String, Object> verifyAndParseToken(String idToken) {
        RSAPublicKey publicKey =
                JwksUtil.getPublicKey(appleJwkUri, JWT.decode(idToken).getKeyId());

        Algorithm algorithm = Algorithm.RSA256(publicKey, null);

        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(appleTokenUrl)
                .withAudience(appleServiceId)
                .build();

        DecodedJWT jwt = verifier.verify(idToken);

        String email = "email";
        String emailVerified = "email_verified";
        return Map.of(
                email,
                jwt.getClaim(email).asString(),
                emailVerified,
                jwt.getClaim(emailVerified).asBoolean());
    }
}
