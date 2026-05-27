package com.thundax.kuzhambu.system.application.auth.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalPasswordPolicyDTO {
    private boolean lockEnabled;
    private int maxFailedCount;
    private int lockSeconds;

    public static PrincipalPasswordPolicyDTO disabled() {
        return new PrincipalPasswordPolicyDTO(false, 0, 0);
    }
}
