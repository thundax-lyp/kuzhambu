package com.thundax.kuzhambu.system.application.auth.service.command;

import com.thundax.kuzhambu.system.domain.model.valueobject.PreAuthSessionId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpsertPreAuthSessionValueCommand {
    private PreAuthSessionId id;
    private String name;
    private String value;
    private long expiredAt;
}
