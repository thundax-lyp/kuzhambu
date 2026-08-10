package com.thundax.kuzhambu.system.application.core.query;

import com.thundax.kuzhambu.system.domain.core.model.enums.UserPrivilege;
import com.thundax.kuzhambu.system.domain.core.model.enums.UserStatus;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.AccessRank;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;

public record CurrentUserQuery(UserId userId, UserPrivilege privilege, UserStatus status, AccessRank rank) {}
