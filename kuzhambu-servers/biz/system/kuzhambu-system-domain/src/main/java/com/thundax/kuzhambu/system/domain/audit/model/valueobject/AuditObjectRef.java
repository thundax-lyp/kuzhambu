package com.thundax.kuzhambu.system.domain.audit.model.valueobject;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class AuditObjectRef {

    private String objectType;
    private String objectId;

    public static AuditObjectRef of(String objectType, String objectId) {
        return new AuditObjectRef(objectType, objectId);
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(objectType) && StringUtils.isNotBlank(objectId);
    }
}
