package com.thundax.kuzhambu.system.application.auth.service.command;

import com.thundax.kuzhambu.system.domain.auth.valueobject.PreAuthSessionId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReleasePreAuthSessionCommand {
    private PreAuthSessionId id;
}
