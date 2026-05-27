package com.thundax.kuzhambu.system.application.core.utils;

public class SysApiUtils {

    public static final String PASSWORD_VALIDATE_PATTERN =
            "^(?![A-Za-z0-9]+$)(?![A-Z0-9\\W]+$)(?![a-z0-9\\W]+$)[a-zA-Z0-9\\W]{8,}$";
    public static final String PASSWORD_VALIDATE_MESSAGE = "密码由大写字母、小写字母、数字、特殊字符组成，且必须包含大写字母、小写字母和特殊字符，长度不小于8位";
}
