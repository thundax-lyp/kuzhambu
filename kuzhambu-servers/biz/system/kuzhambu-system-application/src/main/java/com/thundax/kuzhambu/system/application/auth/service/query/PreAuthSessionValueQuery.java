package com.thundax.kuzhambu.system.application.auth.service.query;

import com.thundax.kuzhambu.system.domain.model.valueobject.PreAuthSessionId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PreAuthSessionValueQuery {
    private PreAuthSessionId id;
    private String name;
}
