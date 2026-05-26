package com.thundax.kuzhambu.common.core.jasypt;

import com.thundax.kuzhambu.common.core.crypto.DesCrypto;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.StringEncryptor;

/**
 * 为配置中的敏感信息加密
 */
public class JasyptStringEncryptor implements StringEncryptor {

    public static final String DEFAULT_PREFIX = "WDIT:";
    public static final String DEFAULT_PASSWORD = "http://www.wdit.com.cn/encryptor";

    private final String prefix;
    private final String password;

    public JasyptStringEncryptor() {
        this.prefix = DEFAULT_PREFIX;
        this.password = DEFAULT_PASSWORD;
    }

    public JasyptStringEncryptor(String prefix, String password) {
        this.prefix = StringUtils.isBlank(prefix) ? DEFAULT_PREFIX : prefix;
        this.password = StringUtils.isBlank(password) ? DEFAULT_PASSWORD : password;
    }

    @Override
    public String encrypt(String message) {
        if (StringUtils.isEmpty(message)) {
            return message;
        }
        return prefix + DesCrypto.encrypt(message, password);
    }

    @Override
    public String decrypt(String encryptedMessage) {
        if (!StringUtils.startsWith(encryptedMessage, prefix)) {
            return encryptedMessage;
        }
        return DesCrypto.decrypt(encryptedMessage.substring(prefix.length()), password);
    }
}
