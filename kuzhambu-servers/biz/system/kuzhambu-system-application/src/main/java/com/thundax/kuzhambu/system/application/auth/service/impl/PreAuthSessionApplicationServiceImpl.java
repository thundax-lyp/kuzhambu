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
    public int countActiveSessions() {
        return preAuthSessionRepository.count();
    }

    @Override
    public PreAuthSession create(CreatePreAuthSessionCommand command) {
        PreAuthSession session = PreAuthSession.create(command.getExpiredSeconds());
        preAuthSessionRepository.insert(session);
        return session;
    }

    @Override
    public PreAuthSessionId getIdByToken(PreAuthSessionQuery query) {
        return preAuthSessionRepository.getByToken(query == null ? null : query.getToken());
    }

    @Override
    public PreAuthSessionId getIdByRefreshToken(PreAuthSessionQuery query) {
        return preAuthSessionRepository.getByRefreshToken(query == null ? null : query.getRefreshToken());
    }

    @Override
    public PreAuthSession get(PreAuthSessionQuery query) {
        PreAuthSession session = preAuthSessionRepository.getById(query == null ? null : query.getId());
        if (session == null || session.isExpired()) {
            throw new BizException("AUTH-00006", "auth.exception.invalid-token", "token 已失效");
        }
        return session;
    }

    @Override
    public PreAuthSession refresh(RefreshPreAuthSessionCommand command) {
        PreAuthSession session = get(new PreAuthSessionQuery(command.getId(), null, null));
        session.refresh(command.getExpiredSeconds(), command.getRefreshTokenGraceSeconds());
        preAuthSessionRepository.update(session);
        return session;
    }

    @Override
    public void release(ReleasePreAuthSessionCommand command) {
        preAuthSessionRepository.deleteById(command.getId());
    }

    @Override
    public void upsertValue(UpsertPreAuthSessionValueCommand command) {
        PreAuthSession session = get(new PreAuthSessionQuery(command.getId(), null, null));
        session.upsertValue(command.getName(), command.getValue(), command.getExpiredAt());
        preAuthSessionRepository.update(session);
    }

    @Override
    public String getValue(PreAuthSessionValueQuery query) {
        return get(new PreAuthSessionQuery(query.getId(), null, null)).findValue(query.getName());
    }

    @Override
    public boolean existsValidatedValue(PreAuthSessionValueValidateQuery query) {
        if (query == null) {
            return false;
        }
        if (captchaWhitelistPolicy.matches(query.getValue())) {
            return true;
        }
        PreAuthSession session = get(new PreAuthSessionQuery(query.getId(), null, null));
        if (!StringUtils.equals(query.getValue(), session.findValue(query.getName()))) {
            return false;
        }
        return StringUtils.isBlank(query.getBindName())
                || StringUtils.equals(query.getBindValue(), session.findValue(query.getBindName()));
    }
}
