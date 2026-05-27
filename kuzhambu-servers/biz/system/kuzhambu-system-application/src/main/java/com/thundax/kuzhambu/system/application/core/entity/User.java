package com.thundax.kuzhambu.system.application.core.entity;

import com.thundax.kuzhambu.system.application.core.entity.enums.UserPrivilege;
import com.thundax.kuzhambu.system.application.core.entity.enums.UserStatus;
import com.thundax.kuzhambu.system.domain.model.valueobject.AccessRank;
import com.thundax.kuzhambu.system.domain.model.valueobject.DepartmentId;
import com.thundax.kuzhambu.system.domain.model.valueobject.UserId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;

/**
 * 后台用户主体。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private UserId id;

    private DepartmentId departmentId;

    private String email;
    private String mobile;
    private String tel;
    private String name;
    private AccessRank rank = AccessRank.of(0);

    private UserPrivilege privilege = UserPrivilege.NORMAL;
    private UserStatus status;
    private String remarks;

    @NonNull
    public AccessRank getRank() {
        return rank == null ? AccessRank.of(null) : rank;
    }

    public void setRank(AccessRank rank) {
        this.rank = rank == null ? AccessRank.of(null) : rank;
    }

    public boolean isSuper() {
        return UserPrivilege.SUPER == getPrivilege();
    }

    public boolean isAdmin() {
        return UserPrivilege.ADMIN == getPrivilege();
    }

    public boolean isEnable() {
        return UserStatus.ENABLED == getStatus();
    }
}
