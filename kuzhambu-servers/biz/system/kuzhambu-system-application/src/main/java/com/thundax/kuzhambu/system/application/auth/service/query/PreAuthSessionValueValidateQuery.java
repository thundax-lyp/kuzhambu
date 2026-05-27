package com.thundax.kuzhambu.system.application.auth.service.query;

import com.thundax.kuzhambu.system.application.auth.entity.valueobject.PreAuthSessionId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PreAuthSessionValueValidateQuery {
    private PreAuthSessionId id;
    private String name;
    private String value;
    private String bindName;
    private String bindValue;
}
