package com.thundax.kuzhambu.system.application.auth.service.command;

import com.thundax.kuzhambu.system.domain.model.entity.PrincipalIdentity;
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
