package com.thundax.kuzhambu.biz.auth.service.command;

import com.thundax.kuzhambu.biz.auth.entity.valueobject.PreAuthSessionId;
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
