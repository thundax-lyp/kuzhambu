package com.thundax.kuzhambu.system.domain.auth.model.valueobject;

import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalKey {
    private PrincipalType principalType;
    private Long principalId;

    public static PrincipalKey of(PrincipalType principalType, Long principalId) {
        return new PrincipalKey(principalType, principalId);
    }

    public boolean isUser() {
        return PrincipalType.USER == principalType;
    }

    public boolean isMember() {
        return PrincipalType.MEMBER == principalType;
    }

    public boolean isOpenClient() {
        return PrincipalType.OPEN_CLIENT == principalType;
    }
}
