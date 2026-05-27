package com.thundax.kuzhambu.system.application.auth.entity;

import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalIdentityStatus;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.model.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.system.domain.model.valueobject.PrincipalKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalIdentity {
    private PrincipalIdentityId id;
    private PrincipalKey principalKey;
    private PrincipalIdentityType type;
    private String identityValue;
    private PrincipalIdentityStatus status = PrincipalIdentityStatus.ENABLED;

    public boolean isEnabled() {
        return PrincipalIdentityStatus.ENABLED == status;
    }

    public boolean isDisabled() {
        return PrincipalIdentityStatus.DISABLED == status;
    }

    public boolean matches(String value) {
        return StringUtils.equals(identityValue, value);
    }

    public boolean isAccount() {
        return type != null && type.isAccount();
    }

    public boolean isMobile() {
        return type != null && type.isMobile();
    }

    public boolean isEmail() {
        return type != null && type.isEmail();
    }

    public void enable() {
        this.status = PrincipalIdentityStatus.ENABLED;
    }

    public void disable() {
        this.status = PrincipalIdentityStatus.DISABLED;
    }
}
