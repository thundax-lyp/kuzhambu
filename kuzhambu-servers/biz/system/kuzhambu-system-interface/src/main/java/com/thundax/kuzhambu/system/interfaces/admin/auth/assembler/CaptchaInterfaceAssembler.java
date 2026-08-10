package com.thundax.kuzhambu.system.interfaces.admin.auth.assembler;

import com.thundax.kuzhambu.system.application.auth.command.UpsertPreAuthSessionValueCommand;
import com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionQuery;
import com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionValueQuery;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionToken;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.response.CaptchaRefreshResponse;
import org.springframework.lang.NonNull;

public final class CaptchaInterfaceAssembler {
    private CaptchaInterfaceAssembler() {}

    @NonNull
    public static UpsertPreAuthSessionValueCommand toUpsertPreAuthSessionValueCommand(
            PreAuthSessionId id, String name, String value, long expiredAt) {
        return new UpsertPreAuthSessionValueCommand(id, name, value, expiredAt);
    }

    @NonNull
    public static PreAuthSessionQuery toPreAuthSessionTokenQuery(String token) {
        return new PreAuthSessionQuery(null, PreAuthSessionToken.of(token), null);
    }

    @NonNull
    public static PreAuthSessionValueQuery toPreAuthSessionValueQuery(PreAuthSessionId id, String name) {
        return new PreAuthSessionValueQuery(id, name);
    }

    @NonNull
    public static CaptchaRefreshResponse toRefreshResponse(boolean refreshed) {
        return CaptchaRefreshResponse.builder().refreshed(refreshed).build();
    }
}
