package com.thundax.kuzhambu.system.application.audit.runtime;

import com.thundax.kuzhambu.system.domain.audit.valueobject.AuditChangedField;
import com.thundax.kuzhambu.system.domain.audit.valueobject.AuditField;
import com.thundax.kuzhambu.system.domain.audit.valueobject.AuditSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class AuditDiffService {

    public List<AuditChangedField> diff(AuditSnapshot beforeSnapshot, AuditSnapshot afterSnapshot) {
        List<AuditChangedField> changedFields = new ArrayList<>();
        Map<String, AuditField> beforeFields = toFieldMap(beforeSnapshot);
        Map<String, AuditField> afterFields = toFieldMap(afterSnapshot);
        for (Map.Entry<String, AuditField> entry : afterFields.entrySet()) {
            AuditField before = beforeFields.get(entry.getKey());
            AuditField after = entry.getValue();
            Object beforeValue = before == null ? null : before.getValue();
            if (!Objects.equals(beforeValue, after.getValue())) {
                changedFields.add(new AuditChangedField(
                        after.getFieldName(),
                        after.getFieldLabel(),
                        beforeValue,
                        before == null ? null : before.getDisplayValue(),
                        after.getValue(),
                        after.getDisplayValue()));
            }
        }
        return changedFields;
    }

    private Map<String, AuditField> toFieldMap(AuditSnapshot snapshot) {
        Map<String, AuditField> fieldMap = new HashMap<>();
        if (snapshot == null || snapshot.getFields() == null) {
            return fieldMap;
        }
        for (AuditField field : snapshot.getFields()) {
            fieldMap.put(field.getFieldName(), field);
        }
        return fieldMap;
    }
}
