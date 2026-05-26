package com.thundax.kuzhambu.common.core.crypto;

import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.bouncycastle.crypto.digests.SM3Digest;

public class Sm3Digest {

    public static String sm3Hex(final byte[] data) {
        return Hex.encodeHexString(Sm3.encrypt(data));
    }

    public static String sm3Hex(final String data) {
        return Hex.encodeHexString(Sm3.encrypt(StringUtils.getBytesUtf8(data)));
    }

    private static class Sm3 {

        private static final int DIGEST_LENGTH = 32;

        public static byte[] encrypt(byte[] input) {
            byte[] output = new byte[DIGEST_LENGTH];

            SM3Digest digest = new SM3Digest();
            digest.update(input, 0, input.length);
            digest.doFinal(output, 0);

            return output;
        }

        public static byte[] encrypt(String text) {
            return encrypt(text.getBytes(StandardCharsets.UTF_8));
        }
    }
}
