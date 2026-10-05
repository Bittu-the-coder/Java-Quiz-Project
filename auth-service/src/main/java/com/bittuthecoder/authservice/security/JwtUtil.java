package com.bittuthecoder.authservice.security;

import com.bittuthecoder.common.security.RsaKeyUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.UUID;

/**
 * RS256 Asymmetric JWT Utility.
 * Signs tokens using an RSA-2048 private key.
 * Verifiers only require the corresponding RSA-2048 public key.
 */
@Component
public class JwtUtil {

    private static final long EXPIRATION = 1000 * 60 * 60 * 24; // 24 hours

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final String publicKeyPem;

    public JwtUtil(
            @Value("${assessify.jwt.rsa.private-key:#{null}}") String customPrivKeyPem,
            @Value("${assessify.jwt.rsa.public-key:#{null}}") String customPubKeyPem
    ) {
        String privPem = (customPrivKeyPem != null && !customPrivKeyPem.isBlank())
                ? customPrivKeyPem : RsaKeyUtil.DEFAULT_PRIVATE_KEY_PEM;
        String pubPem = (customPubKeyPem != null && !customPubKeyPem.isBlank())
                ? customPubKeyPem : RsaKeyUtil.DEFAULT_PUBLIC_KEY_PEM;

        this.privateKey = RsaKeyUtil.parsePrivateKey(privPem);
        this.publicKey = RsaKeyUtil.parsePublicKey(pubPem);
        this.publicKeyPem = pubPem;
    }

    public String getPublicKeyPem() {
        return publicKeyPem;
    }

    public String generateToken(UUID userId, String email, UUID orgId, String role) {
        var builder = Jwts.builder()
                .setSubject(email)
                .claim("user_id", userId != null ? userId.toString() : null)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION));

        if (orgId != null) {
            builder.claim("org_id", orgId.toString());
        }

        return builder.signWith(privateKey, SignatureAlgorithm.RS256).compact();
    }

    public String generateToken(String email, String role) {
        return generateToken(null, email, null, role);
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
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
