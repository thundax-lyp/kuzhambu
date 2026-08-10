package com.thundax.kuzhambu.system.application.auth.command;

import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalCredentialStatus;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalCredentialId;

public record ChangePrincipalCredentialStatusCommand(PrincipalCredentialId id, PrincipalCredentialStatus status) {}
