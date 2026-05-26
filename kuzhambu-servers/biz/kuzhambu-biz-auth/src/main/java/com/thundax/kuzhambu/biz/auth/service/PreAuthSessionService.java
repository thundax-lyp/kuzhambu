package com.thundax.kuzhambu.biz.auth.service;

import com.thundax.kuzhambu.biz.auth.entity.PreAuthSession;
import com.thundax.kuzhambu.biz.auth.entity.valueobject.PreAuthSessionId;
import com.thundax.kuzhambu.biz.auth.entity.valueobject.PreAuthSessionToken;
import com.thundax.kuzhambu.biz.auth.service.command.CreatePreAuthSessionCommand;
import com.thundax.kuzhambu.biz.auth.service.command.RefreshPreAuthSessionCommand;
import com.thundax.kuzhambu.biz.auth.service.command.ReleasePreAuthSessionCommand;
import com.thundax.kuzhambu.biz.auth.service.command.UpsertPreAuthSessionValueCommand;
import com.thundax.kuzhambu.biz.auth.service.query.PreAuthSessionValueQuery;
import com.thundax.kuzhambu.biz.auth.service.query.PreAuthSessionValueValidateQuery;

public interface PreAuthSessionService {

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
