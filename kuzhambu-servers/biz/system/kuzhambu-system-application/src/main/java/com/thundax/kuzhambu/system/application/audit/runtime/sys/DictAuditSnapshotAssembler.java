package com.thundax.kuzhambu.system.application.audit.runtime.sys;

import com.thundax.kuzhambu.system.application.audit.runtime.AuditSnapshotAssembler;
import com.thundax.kuzhambu.system.application.audit.runtime.AuditSnapshots;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditField;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditSnapshot;
import com.thundax.kuzhambu.system.domain.core.model.entity.Dict;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DictAuditSnapshotAssembler implements AuditSnapshotAssembler {

    @Override
    public String objectType() {
        return "Dict";
    }

    @Override
    public String objectTypeLabel() {
        return "字典";
    }

    @Override
    public List<AuditField> fields() {
        return Arrays.asList(
                AuditSnapshots.field("type", "类型", null),
                AuditSnapshots.field("label", "标签", null),
                AuditSnapshots.field("value", "值", null));
    }

    @Override
    public AuditSnapshot assemble(Object object) {
        Dict dict = (Dict) object;
        if (dict == null) {
            return null;
        }
        return AuditSnapshots.of(
                objectType(),
                dict.getId(),
                dict.getLabel(),
                AuditSnapshots.field("type", "类型", dict.getType()),
                AuditSnapshots.field("label", "标签", dict.getLabel()),
                AuditSnapshots.field("value", "值", dict.getValue()));
    }
}
