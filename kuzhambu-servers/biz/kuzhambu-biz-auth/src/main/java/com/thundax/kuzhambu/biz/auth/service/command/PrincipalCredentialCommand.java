package com.thundax.kuzhambu.biz.auth.service.command;

import com.thundax.kuzhambu.biz.auth.entity.PrincipalCredential;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalCredentialCommand {
    private PrincipalCredential principalCredential;
}
