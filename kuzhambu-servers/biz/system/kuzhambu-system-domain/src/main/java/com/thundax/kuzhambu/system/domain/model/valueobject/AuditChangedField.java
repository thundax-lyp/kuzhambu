package com.thundax.kuzhambu.system.domain.model.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditChangedField {

    private String fieldName;
    private String fieldLabel;
    private Object beforeValue;
    private String beforeDisplayValue;
    private Object afterValue;
    private String afterDisplayValue;
}
