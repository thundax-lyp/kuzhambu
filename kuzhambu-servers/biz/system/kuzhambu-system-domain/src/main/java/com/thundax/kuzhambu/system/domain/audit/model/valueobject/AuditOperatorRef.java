package com.thundax.kuzhambu.system.domain.audit.model.valueobject;

import com.thundax.kuzhambu.system.domain.audit.model.enums.AuditOperatorType;
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
public class AuditOperatorRef {

    private AuditOperatorType operatorType;
    private String operatorId;

    public static AuditOperatorRef of(AuditOperatorType operatorType, String operatorId) {
        return new AuditOperatorRef(operatorType, operatorId);
    }

    public boolean isValid() {
        return operatorType != null && StringUtils.isNotBlank(operatorId);
    }
}
