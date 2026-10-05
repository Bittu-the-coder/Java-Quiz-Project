package com.bittuthecoder.api_gateway.security;

import com.bittuthecoder.common.security.RsaKeyUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.PublicKey;

/**
 * Gateway RS256 JWT Verifier.
 * Only requires the RSA-2048 public key. No private key is ever exposed or known to the gateway.
 */
@Component
public class JwtUtil {

    private final PublicKey publicKey;

    public JwtUtil(
            @Value("${assessify.jwt.rsa.public-key:#{null}}") String customPubKeyPem
    ) {
        String pubPem = (customPubKeyPem != null && !customPubKeyPem.isBlank())
                ? customPubKeyPem : RsaKeyUtil.DEFAULT_PUBLIC_KEY_PEM;
        this.publicKey = RsaKeyUtil.parsePublicKey(pubPem);
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
