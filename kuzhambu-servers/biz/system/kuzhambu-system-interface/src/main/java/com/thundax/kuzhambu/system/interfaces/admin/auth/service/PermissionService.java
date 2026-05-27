package com.thundax.kuzhambu.system.interfaces.admin.auth.service;

import com.thundax.kuzhambu.common.core.arch.LayerPublicApi;
import java.util.Set;

public interface PermissionService {

    Set<String> createPermissions(String token, String userId);

    Set<String> getPermissions(String token);

    @LayerPublicApi(reason = "权限认证适配层按令牌校验业务权限的稳定入口")
    boolean isPermitted(String token, String permission);
}
