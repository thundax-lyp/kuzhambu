package com.thundax.kuzhambu.system.application.auth.service;

import com.thundax.kuzhambu.system.application.auth.command.CreatePreAuthSessionCommand;
import com.thundax.kuzhambu.system.application.auth.command.RefreshPreAuthSessionCommand;
import com.thundax.kuzhambu.system.application.auth.command.ReleasePreAuthSessionCommand;
import com.thundax.kuzhambu.system.application.auth.command.UpsertPreAuthSessionValueCommand;
import com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionValueQuery;
import com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionValueValidateQuery;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PreAuthSession;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionToken;

public interface PreAuthSessionApplicationService {

    int countActiveSessions();

    PreAuthSession create(CreatePreAuthSessionCommand command);

    PreAuthSessionId getIdByToken(PreAuthSessionToken token);

    PreAuthSessionId getIdByRefreshToken(PreAuthSessionToken refreshToken);

    PreAuthSession get(PreAuthSessionId id);

    PreAuthSession refresh(RefreshPreAuthSessionCommand command);

    void release(ReleasePreAuthSessionCommand command);

    void upsertValue(UpsertPreAuthSessionValueCommand command);

    String getValue(PreAuthSessionValueQuery query);

    boolean existsValidatedValue(PreAuthSessionValueValidateQuery query);
}
