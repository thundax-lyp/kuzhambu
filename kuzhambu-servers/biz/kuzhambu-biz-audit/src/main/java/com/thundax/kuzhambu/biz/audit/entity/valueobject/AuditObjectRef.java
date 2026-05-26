package com.thundax.kuzhambu.biz.audit.entity.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditObjectRef {

    private String objectType;
    private String objectId;

    public boolean isValid() {
        return StringUtils.isNotBlank(objectType) && StringUtils.isNotBlank(objectId);
    }
}
