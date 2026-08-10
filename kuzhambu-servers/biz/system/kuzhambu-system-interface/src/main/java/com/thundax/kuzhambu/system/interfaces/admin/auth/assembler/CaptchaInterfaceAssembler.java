package com.thundax.kuzhambu.system.interfaces.admin.auth.assembler;

import com.thundax.kuzhambu.system.application.auth.command.UpsertPreAuthSessionValueCommand;
import com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionQuery;
import com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionValueQuery;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionToken;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.response.CaptchaRefreshResponse;
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class CaptchaInterfaceAssembler {
    private CaptchaInterfaceAssembler() {}

    @NonNull
    public static UpsertPreAuthSessionValueCommand toUpsertPreAuthSessionValueCommand(
            @NonNull PreAuthSessionId id, @NonNull String name, @NonNull String value, long expiredAt) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(value, "value must not be null");
        return new UpsertPreAuthSessionValueCommand(id, name, value, expiredAt);
    }

    @NonNull
    public static PreAuthSessionQuery toPreAuthSessionTokenQuery(@NonNull String token) {
        Objects.requireNonNull(token, "token must not be null");
        return new PreAuthSessionQuery(null, PreAuthSessionToken.of(token), null);
    }

    @NonNull
    public static PreAuthSessionValueQuery toPreAuthSessionValueQuery(
            @NonNull PreAuthSessionId id, @NonNull String name) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        return new PreAuthSessionValueQuery(id, name);
    }

    @NonNull
    public static CaptchaRefreshResponse toRefreshResponse(boolean refreshed) {
        return CaptchaRefreshResponse.builder().refreshed(refreshed).build();
    }
}
