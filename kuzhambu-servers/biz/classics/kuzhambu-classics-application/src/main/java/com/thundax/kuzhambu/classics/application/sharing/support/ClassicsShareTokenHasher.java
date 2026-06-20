package com.thundax.kuzhambu.classics.application.sharing.support;

import com.thundax.kuzhambu.common.core.exception.BizException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

@Component
public class ClassicsShareTokenHasher {

    public String hash(String shareToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(shareToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new BizException("CLASSICS-14003", "classics.share.token.hash.unavailable", "分享令牌哈希算法不可用", ex);
        }
    }
}
