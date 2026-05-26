package com.thundax.kuzhambu.biz.auth.utils;

import java.util.Random;

public final class PreAuthCodeHelper {
    private static final int CAPTCHA_LENGTH = 4;
    private static final int SMS_VALIDATE_CODE_LENGTH = 6;
    private static final int EMAIL_VALIDATE_CODE_LENGTH = 6;
    private static final char[] CAPTCHA_CHARS = {'2', '3', '4', '5', '6', '7', '8', '9'};
    private static final char[] DIGIT_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    private PreAuthCodeHelper() {}

    public static String generateCaptcha() {
        return create(CAPTCHA_CHARS, CAPTCHA_LENGTH);
    }

    public static String generateSmsCode() {
        return create(DIGIT_CHARS, SMS_VALIDATE_CODE_LENGTH);
    }

    public static String generateEmailCode() {
        return create(DIGIT_CHARS, EMAIL_VALIDATE_CODE_LENGTH);
    }

    private static String create(char[] validateChars, int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int idx = 0; idx < length; idx++) {
            sb.append(validateChars[random.nextInt(validateChars.length)]);
        }
        return sb.toString();
    }
}
