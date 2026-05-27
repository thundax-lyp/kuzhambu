package com.thundax.kuzhambu.system.interfaces.admin.auth.assembler;

import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.response.CaptchaRefreshResponse;
import org.springframework.lang.NonNull;

public final class CaptchaInterfaceAssembler {
    private CaptchaInterfaceAssembler() {}

    @NonNull
    public static CaptchaRefreshResponse toRefreshResponse(boolean refreshed) {
        return CaptchaRefreshResponse.builder().refreshed(refreshed).build();
    }
}
