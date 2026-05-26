package com.thundax.kuzhambu.biz.core.service.query;

import com.thundax.kuzhambu.biz.core.entity.enums.UserPrivilege;
import com.thundax.kuzhambu.biz.core.entity.enums.UserStatus;
import com.thundax.kuzhambu.biz.core.entity.valueobject.AccessRank;
import com.thundax.kuzhambu.biz.core.entity.valueobject.UserId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserQuery {
    private UserId userId;
    private UserPrivilege privilege;
    private UserStatus status;
    private AccessRank rank;
}
