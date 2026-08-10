package com.thundax.kuzhambu.system.application.auth.query;

import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalAccessTokenCode;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.PermissionCode;

public record PermissionQuery(PrincipalAccessTokenCode token, PermissionCode permission) {}
