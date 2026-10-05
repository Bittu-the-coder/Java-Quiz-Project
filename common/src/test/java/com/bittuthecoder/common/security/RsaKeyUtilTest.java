package com.bittuthecoder.common.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

import static org.assertj.core.api.Assertions.assertThat;

class RsaKeyUtilTest {

    @Test
    @DisplayName("Should successfully parse default RSA 2048 public and private keys")
    void shouldParseDefaultRsaKeys() {
        PrivateKey privateKey = RsaKeyUtil.parsePrivateKey(RsaKeyUtil.DEFAULT_PRIVATE_KEY_PEM);
        PublicKey publicKey = RsaKeyUtil.parsePublicKey(RsaKeyUtil.DEFAULT_PUBLIC_KEY_PEM);

        assertThat(privateKey).isNotNull();
        assertThat(privateKey.getAlgorithm()).isEqualTo("RSA");

        assertThat(publicKey).isNotNull();
        assertThat(publicKey.getAlgorithm()).isEqualTo("RSA");
    }
}
