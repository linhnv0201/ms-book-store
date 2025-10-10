package user_service.user.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            SignedJWT signedJWT = SignedJWT.parse(token);

            if (!signedJWT.verify(new MACVerifier(SIGNER_KEY.getBytes()))) {
                throw new JwtException("Invalid signature");
            }

            Map<String, Object> claims = new HashMap<>(signedJWT.getJWTClaimsSet().getClaims());

            // Convert timestamps to Instant for Spring Security
            if (claims.containsKey("exp")) claims.put("exp", ((java.util.Date) claims.get("exp")).toInstant());
            if (claims.containsKey("iat")) claims.put("iat", ((java.util.Date) claims.get("iat")).toInstant());
            if (claims.containsKey("nbf")) claims.put("nbf", ((java.util.Date) claims.get("nbf")).toInstant());

            return Jwt.withTokenValue(token)
                    .headers(h -> h.put("alg", "HS512"))
                    .claims(c -> c.putAll(claims))
                    .build();

        } catch (ParseException | JOSEException e) {
            throw new JwtException("Cannot decode token", e);
        }
    }
}
