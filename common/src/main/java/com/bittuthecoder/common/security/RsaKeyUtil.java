package com.bittuthecoder.common.security;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Utility for parsing and managing RSA-2048 cryptographic keys for RS256 JWT signing and verification.
 * Eliminates symmetric HS256 shared-secret exposure across services.
 */
public final class RsaKeyUtil {

    public static final String DEFAULT_PUBLIC_KEY_PEM =
            "-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmhOeVtkuVAi17v9phgGj\n" +
            "Je269QrVPVNUfoDlHria3g3EdKAGBZPUpoT+H3lpKuAO5x6Dh+6k2djmcM++Fzt/\n" +
            "guNivkLJcTpMujHkZ1D71uqYeK+CpMpwSOUN40wBguNZpUHla6R5VjgkExSFdkaA\n" +
            "1DT8hjvZ+CjV6j0kOkRYBjz4ayzfgXA5fv/bWV4+HIIFVuh0wL0BUuqFzM2tuEHb\n" +
            "KBHyIigszyQXqx50/egRwWytjjK5QXfJPTT9Kj77XjDU3+RPBN5k3O+JZkb0lNji\n" +
            "WzgDIohd9HuuhwR18p6McjVW/JRVdb0A7mHqa8HcBSqWh3c30sPu6d2YvWNU44FT\n" +
            "RwIDAQAB\n" +
            "-----END PUBLIC KEY-----";

    public static final String DEFAULT_PRIVATE_KEY_PEM =
            "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCaE55W2S5UCLXu\n" +
            "/2mGAaMl7br1CtU9U1R+gOUeuJreDcR0oAYFk9SmhP4feWkq4A7nHoOH7qTZ2OZw\n" +
            "z74XO3+C42K+QslxOky6MeRnUPvW6ph4r4KkynBI5Q3jTAGC41mlQeVrpHlWOCQT\n" +
            "FIV2RoDUNPyGO9n4KNXqPSQ6RFgGPPhrLN+BcDl+/9tZXj4cggVW6HTAvQFS6oXM\n" +
            "za24QdsoEfIiKCzPJBerHnT96BHBbK2OMrlBd8k9NP0qPvteMNTf5E8E3mTc74lm\n" +
            "RvSU2OJbOAMiiF30e66HBHXynoxyNVb8lFV1vQDuYeprwdwFKpaHdzfSw+7p3Zi9\n" +
            "Y1TjgVNHAgMBAAECggEAD4Whx6lng8BrkQvwIIzyV7QvHS1Iiui7NBII2GMhTvTf\n" +
            "cXRcRvGRVG6aKfzm6GfcIyfYI7iZ/kxd+B16+VzdcrKKVGqRYAkkiQQqY3ItfNrz\n" +
            "c1e9zJuMW6ZKaOH+VlD7IK1BJJVUQqhd51eDk9p0c2xxyEAoAa/Iz9P1s10GlSd6\n" +
            "lDX6bX3wRTLTQq5ZSCaS3sqfI8nT+vRPoilnOy24a96cGNecZEKOiFvrh7LvEy1g\n" +
            "scAO71GElDrKLY8wTqzk12r1Eo33GYF1OewFv6iVmynq1uGkP1kApSQmBu7SY3O2\n" +
            "81jH1ro6bE3H2Epjg9UJj/S/eJ9Uqr5KLLTXnzfE8QKBgQDOw1SnVQw5S2SLb/Sc\n" +
            "E7mxOW4Pemc6Ihoeq0ejyVVhA7jtXxSfiIuHPEaFv64hCs9UL2jzmF0Dyd8+YoSa\n" +
            "SGbhdnV6XEr49kG3DtKZo1Tpf1etmqojHW6GW2SIp2kREX3MmVpYn1+Y3PUixW+2\n" +
            "lQNwee+Ekjo2TtXN7Ffc1CVm7wKBgQC+xGzoEtZQ9n6xI0QTPEsMZQXyTm1cm+JV\n" +
            "HxRTJGh6wJ6ODoeA339X2g/3r0y3O5euHNsXLNQnkmeUFtbHr9Syc/lxOL/r+tHn\n" +
            "qmVPixL8oTdvBq4EADhJeMsWx2veoQoP77c74+uZNoy8qTGsiBtb4nPvRejPXPe0\n" +
            "Q1mz2PGZKQKBgBgMwmEAVH6plVVevV4WMUg6/OFSBIXPh6g/lgKoHYU+UJlTsOtp\n" +
            "j4k4ap+ODywKvNj29sc21sLlDVGNVg04FLdu9vU3nQTeaABp+fci93J9fG5WCiox\n" +
            "dzSrlsMKbf8tQKu2vrqGNzFpqh3UR00+gfroRALwm1LL0rS0I/gsuPkDAoGARHux\n" +
            "eRKiG4iR7LdzTdB8RKPToavj+LYBZ3tzyXEtjLuvb+HpZStWxMwEpW8qBDGBf0De\n" +
            "qhAuzVHymygjzKXjnnih2LJ9u5JcXmU+X3LWyxh5FQN53vfRPRb/GVtX7yEywovU\n" +
            "5MwqnOMHXQcETSLDZ5YA9qnLLP378punDThZaxkCgYANs88Hnrg3Q5HCK9HbPWJ6\n" +
            "jPbZAevm6AqZZrwFrO4UZvEBVwNDKIOTEf66rdDB8aDy31Cq5SdJ0tUiF8vuPhoZ\n" +
            "SqkHqFFka3sF8wX1HVuURBrm6MypHyyWIU7cayU/VKF6PvxbVAgZfrCaCAAm9v4t\n" +
            "XAz4/kAabxvXkbn3T/DGiw==\n" +
            "-----END PRIVATE KEY-----";

    private RsaKeyUtil() {
    }

    public static PrivateKey parsePrivateKey(String pem) {
        try {
            String clean = pem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(clean);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalArgumentException("Failed to parse RSA Private Key from PEM", e);
        }
    }

    public static PublicKey parsePublicKey(String pem) {
        try {
            String clean = pem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(clean);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalArgumentException("Failed to parse RSA Public Key from PEM", e);
        }
    }
}
