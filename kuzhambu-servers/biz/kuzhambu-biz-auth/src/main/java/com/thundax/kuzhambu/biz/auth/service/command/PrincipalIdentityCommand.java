package com.thundax.kuzhambu.biz.auth.service.command;

import com.thundax.kuzhambu.biz.auth.entity.PrincipalIdentity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalIdentityCommand {
    private PrincipalIdentity principalIdentity;
}
