package com.thundax.kuzhambu.biz.auth.service.impl;

import com.thundax.kuzhambu.biz.auth.configure.CaptchaWhitelistProperties;
import com.thundax.kuzhambu.biz.auth.dao.PreAuthSessionDao;
import com.thundax.kuzhambu.biz.auth.entity.PreAuthSession;
import com.thundax.kuzhambu.biz.auth.entity.valueobject.PreAuthSessionId;
import com.thundax.kuzhambu.biz.auth.entity.valueobject.PreAuthSessionToken;
import com.thundax.kuzhambu.biz.auth.service.PreAuthSessionService;
import com.thundax.kuzhambu.biz.auth.service.command.CreatePreAuthSessionCommand;
import com.thundax.kuzhambu.biz.auth.service.command.RefreshPreAuthSessionCommand;
import com.thundax.kuzhambu.biz.auth.service.command.ReleasePreAuthSessionCommand;
import com.thundax.kuzhambu.biz.auth.service.command.UpsertPreAuthSessionValueCommand;
import com.thundax.kuzhambu.biz.auth.service.query.PreAuthSessionValueQuery;
import com.thundax.kuzhambu.biz.auth.service.query.PreAuthSessionValueValidateQuery;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class PreAuthSessionServiceImpl implements PreAuthSessionService {

    private final PreAuthSessionDao preAuthSessionDao;
    private final CaptchaWhitelistProperties captchaWhitelistProperties;

    @Autowired
    public PreAuthSessionServiceImpl(
            PreAuthSessionDao preAuthSessionDao, CaptchaWhitelistProperties captchaWhitelistProperties) {
        this.preAuthSessionDao = preAuthSessionDao;
        this.captchaWhitelistProperties =
                captchaWhitelistProperties == null ? CaptchaWhitelistProperties.disabled() : captchaWhitelistProperties;
    }

    @Override
    public int countActiveSessions() {
        return preAuthSessionDao.count();
    }

    @Override
    public PreAuthSession create(CreatePreAuthSessionCommand command) {
        PreAuthSession session = PreAuthSession.create(command.getExpiredSeconds());
        preAuthSessionDao.insert(session);
        return session;
    }

    @Override
    public PreAuthSessionId getIdByToken(PreAuthSessionToken token) {
        return preAuthSessionDao.getByToken(token);
    }

    @Override
    public PreAuthSessionId getIdByRefreshToken(PreAuthSessionToken refreshToken) {
        return preAuthSessionDao.getByRefreshToken(refreshToken);
    }

    @Override
    public PreAuthSession get(PreAuthSessionId id) {
        PreAuthSession session = preAuthSessionDao.getById(id);
        if (session == null || session.isExpired()) {
            throw new BizException("AUTH-00006", "auth.exception.invalid-token", "token 已失效");
        }
        return session;
    }

    @Override
    public PreAuthSession refresh(RefreshPreAuthSessionCommand command) {
        PreAuthSession session = get(command.getId());
        session.refresh(command.getExpiredSeconds(), command.getRefreshTokenGraceSeconds());
        preAuthSessionDao.update(session);
        return session;
    }

    @Override
    public void release(ReleasePreAuthSessionCommand command) {
        preAuthSessionDao.deleteById(command.getId());
    }

    @Override
    public void upsertValue(UpsertPreAuthSessionValueCommand command) {
        PreAuthSession session = get(command.getId());
        session.upsertValue(command.getName(), command.getValue(), command.getExpiredAt());
        preAuthSessionDao.update(session);
    }

    @Override
    public String getValue(PreAuthSessionValueQuery query) {
        return get(query.getId()).findValue(query.getName());
    }

    @Override
    public boolean existsValidatedValue(PreAuthSessionValueValidateQuery query) {
        if (query == null) {
            return false;
        }
        if (captchaWhitelistProperties.matches(query.getValue())) {
            return true;
        }
        PreAuthSession session = get(query.getId());
        if (!StringUtils.equals(query.getValue(), session.findValue(query.getName()))) {
            return false;
        }
        return StringUtils.isBlank(query.getBindName())
                || StringUtils.equals(query.getBindValue(), session.findValue(query.getBindName()));
    }
}
