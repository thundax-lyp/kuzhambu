package com.thundax.kuzhambu.system.application.auth.command;

import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalCredentialId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecordPrincipalCredentialFailureCommand {
    private PrincipalCredentialId id;
    private int failedLimit;
    private Instant lockedUntil;
}
