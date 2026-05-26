package com.thundax.kuzhambu.common.core.crypto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.BigIntegers;

/**
 * 仅适配前端 sm-crypto
 */
@Slf4j
public final class Sm2Crypto {

    private static final String SM2_CURVE_NAME = "sm2p256v1";

    private static final String ALGORITHM = "EC";

    private static final String ENCRYPTED_PREFIX = "04";

    private static final X9ECParameters X9EC_PARAMETERS = GMNamedCurves.getByName(SM2_CURVE_NAME);
    private static final ECDomainParameters DOMAIN_PARAMETERS = new ECDomainParameters(
            X9EC_PARAMETERS.getCurve(), X9EC_PARAMETERS.getG(), X9EC_PARAMETERS.getN(), X9EC_PARAMETERS.getH());

    private Sm2Crypto() {}

    public static StringKeyPair generateKeyPair() {
        try {
            SecureRandom random = new SecureRandom();
            KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM, new BouncyCastleProvider());
            generator.initialize(new ECGenParameterSpec(SM2_CURVE_NAME), random);

            return new StringKeyPair(generator.generateKeyPair());

        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            log.warn(e.getMessage());
            return null;
        }
    }

    public static String encrypt(String plainText, String publicKeyString) {
        if (StringUtils.isEmpty(publicKeyString) || StringUtils.isEmpty(plainText)) {
            return plainText;
        }

        try {
            ECPoint point = DOMAIN_PARAMETERS.getCurve().decodePoint(Hex.decodeHex(publicKeyString));
            ECPublicKeyParameters publicKeyParameters = new ECPublicKeyParameters(point, DOMAIN_PARAMETERS);

            byte[] buffer = plainText.getBytes(StandardCharsets.UTF_8);

            SM2Engine engine = new SM2Engine();
            engine.init(true, new ParametersWithRandom(publicKeyParameters, new SecureRandom()));
            byte[] encryptedBuffer = engine.processBlock(buffer, 0, buffer.length);

            return Hex.encodeHexString(encryptedBuffer);

        } catch (InvalidCipherTextException | DecoderException e) {
            log.warn(e.getMessage());
            return null;
        }
    }

    public static String decrypt(String encryptedText, String privateKeyString) {
        if (StringUtils.isEmpty(privateKeyString) || StringUtils.isEmpty(encryptedText)) {
            return null;
        }

        if (!encryptedText.startsWith(ENCRYPTED_PREFIX)) {
            encryptedText = ENCRYPTED_PREFIX + encryptedText;
        }

        try {
            ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(
                    BigIntegers.fromUnsignedByteArray(Hex.decodeHex(privateKeyString)), DOMAIN_PARAMETERS);

            byte[] encryptedBuffer = Hex.decodeHex(encryptedText);

            SM2Engine engine = new SM2Engine();
            engine.init(false, privateKeyParameters);
            byte[] buffer = engine.processBlock(encryptedBuffer, 0, encryptedBuffer.length);

            return new String(buffer, StandardCharsets.UTF_8);

        } catch (DecoderException | InvalidCipherTextException e) {
            log.warn(e.getMessage());
            return null;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StringKeyPair {

        private String publicKey;
        private String privateKey;

        public StringKeyPair() {}

        public StringKeyPair(KeyPair keyPair) {
            this.publicKey = Hex.encodeHexString(
                    ((BCECPublicKey) keyPair.getPublic()).getQ().getEncoded(false));

            this.privateKey = Hex.encodeHexString(
                    ((BCECPrivateKey) keyPair.getPrivate()).getD().toByteArray());
        }

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public void setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
        }

        @Override
        public String toString() {
            return "{\"publicKey\":\"" + escape(publicKey) + "\",\"privateKey\":\"" + escape(privateKey) + "\"}";
        }

        private static String escape(String value) {
            if (value == null) {
                return "";
            }
            return value.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }

    public static void main(String[] argv) {
        StringKeyPair keyPair = generateKeyPair();
        if (keyPair == null) {
            log.error("null key pair");
            return;
        }

        System.out.println(keyPair.toString());

        String plainText = "this is a test text";

        String encryptedText = encrypt(plainText, keyPair.getPublicKey());
        System.out.println(encryptedText);

        String decryptedText = decrypt(encryptedText, keyPair.getPrivateKey());
        System.out.println(decryptedText);
    }
}
