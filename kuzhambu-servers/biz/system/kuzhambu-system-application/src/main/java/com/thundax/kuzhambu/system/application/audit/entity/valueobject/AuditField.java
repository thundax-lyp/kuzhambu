package com.thundax.kuzhambu.system.application.audit.entity.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditField {

    private String fieldName;
    private String fieldLabel;
    private Object value;
    private String displayValue;
    private String valueType;
    private boolean sensitive;
}
