package com.thundax.kuzhambu.system.application.auth.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.system.application.auth.command.CreatePreAuthSessionCommand;
import com.thundax.kuzhambu.system.application.auth.command.RefreshPreAuthSessionCommand;
import com.thundax.kuzhambu.system.application.auth.command.ReleasePreAuthSessionCommand;
import com.thundax.kuzhambu.system.application.auth.command.UpsertPreAuthSessionValueCommand;
import com.thundax.kuzhambu.system.application.auth.configure.CaptchaWhitelistProperties;
import com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionQuery;
import com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionValueQuery;
import com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionValueValidateQuery;
import com.thundax.kuzhambu.system.application.auth.service.PreAuthSessionApplicationService;
import com.thundax.kuzhambu.system.application.auth.support.CaptchaWhitelistPolicy;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PreAuthSession;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionId;
import com.thundax.kuzhambu.system.domain.auth.repository.PreAuthSessionRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class PreAuthSessionApplicationServiceImpl implements PreAuthSessionApplicationService {

    private final PreAuthSessionRepository preAuthSessionRepository;
    private final CaptchaWhitelistPolicy captchaWhitelistPolicy;

    @Autowired
    public PreAuthSessionApplicationServiceImpl(
            PreAuthSessionRepository preAuthSessionRepository, CaptchaWhitelistProperties captchaWhitelistProperties) {
        this.preAuthSessionRepository = preAuthSessionRepository;
        this.captchaWhitelistPolicy = CaptchaWhitelistPolicy.from(captchaWhitelistProperties);
    }

    @Override
    public long summaryActiveSessionCount() {
        return preAuthSessionRepository.count();
    }

    @Override
    public PreAuthSession create(CreatePreAuthSessionCommand command) {
        PreAuthSession session = PreAuthSession.create(command.expiredSeconds());
        preAuthSessionRepository.insert(session);
        return session;
    }

    @Override
    public PreAuthSessionId getIdByToken(PreAuthSessionQuery query) {
        return preAuthSessionRepository.getByToken(query == null ? null : query.token());
    }

    @Override
    public PreAuthSessionId getIdByRefreshToken(PreAuthSessionQuery query) {
        return preAuthSessionRepository.getByRefreshToken(query == null ? null : query.refreshToken());
    }

    @Override
    public PreAuthSession get(PreAuthSessionQuery query) {
        PreAuthSession session = preAuthSessionRepository.getById(query == null ? null : query.id());
        if (session == null || session.isExpired()) {
            throw new BizException("AUTH-00006", "auth.exception.invalid-token", "token 已失效");
        }
        return session;
    }

    @Override
    public PreAuthSession refresh(RefreshPreAuthSessionCommand command) {
        PreAuthSession session = get(new PreAuthSessionQuery(command.id(), null, null));
        session.refresh(command.expiredSeconds(), command.refreshTokenGraceSeconds());
        preAuthSessionRepository.update(session);
        return session;
    }

    @Override
    public void release(ReleasePreAuthSessionCommand command) {
        preAuthSessionRepository.deleteById(command.id());
    }

    @Override
    public void upsertValue(UpsertPreAuthSessionValueCommand command) {
        PreAuthSession session = get(new PreAuthSessionQuery(command.id(), null, null));
        session.upsertValue(command.name(), command.value(), command.expiredAt());
        preAuthSessionRepository.update(session);
    }

    @Override
    public String getValue(PreAuthSessionValueQuery query) {
        return get(new PreAuthSessionQuery(query.id(), null, null)).findValue(query.name());
    }

    @Override
    public boolean existsValidatedValue(PreAuthSessionValueValidateQuery query) {
        if (query == null) {
            return false;
        }
        if (captchaWhitelistPolicy.matches(query.value())) {
            return true;
        }
        PreAuthSession session = get(new PreAuthSessionQuery(query.id(), null, null));
        if (!StringUtils.equals(query.value(), session.findValue(query.name()))) {
            return false;
        }
        return StringUtils.isBlank(query.bindName())
                || StringUtils.equals(query.bindValue(), session.findValue(query.bindName()));
    }
}
