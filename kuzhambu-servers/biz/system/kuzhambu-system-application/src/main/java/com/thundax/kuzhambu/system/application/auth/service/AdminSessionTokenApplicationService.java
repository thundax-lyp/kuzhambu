package com.thundax.kuzhambu.system.application.auth.service;

import com.thundax.kuzhambu.system.application.auth.command.CreateAdminAccessTokenCommand;
import com.thundax.kuzhambu.system.application.auth.command.DeleteAdminAccessTokenCommand;
import com.thundax.kuzhambu.system.application.auth.command.InvalidateAdminSessionCommand;
import com.thundax.kuzhambu.system.application.auth.command.RecordPrincipalLoginFailureCommand;
import com.thundax.kuzhambu.system.application.auth.command.RefreshAdminAccessTokenCommand;
import com.thundax.kuzhambu.system.application.auth.query.AdminAccessTokenQuery;
import com.thundax.kuzhambu.system.application.auth.result.AdminAccessTokenResult;
import com.thundax.kuzhambu.system.application.auth.result.AdminTokenQueryResult;
import com.thundax.kuzhambu.system.application.auth.result.AdminTokenRefreshResult;

public interface AdminSessionTokenApplicationService {

    AdminAccessTokenResult createAccessToken(CreateAdminAccessTokenCommand command);

    AdminAccessTokenResult getAccessToken(AdminAccessTokenQuery query);

    int deleteAccessTokensByUserId(DeleteAdminAccessTokenCommand command);

    boolean validateToken(AdminAccessTokenQuery query);

    void activeAccessToken(AdminAccessTokenQuery query);

    void deleteAccessToken(DeleteAdminAccessTokenCommand command);

    AdminTokenQueryResult getTokenInfo(AdminAccessTokenQuery query);

    AdminTokenRefreshResult refreshAccessToken(RefreshAdminAccessTokenCommand command);

    void invalidateSessionByToken(InvalidateAdminSessionCommand command);

    int invalidateSessionsByUserId(InvalidateAdminSessionCommand command);

    void recordLoginFailed(RecordPrincipalLoginFailureCommand command);
}
