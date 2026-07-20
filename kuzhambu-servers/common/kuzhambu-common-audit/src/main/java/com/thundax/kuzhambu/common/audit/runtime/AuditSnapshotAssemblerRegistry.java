package com.thundax.kuzhambu.common.audit.runtime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AuditSnapshotAssemblerRegistry {

    private final Map<String, AuditSnapshotAssembler> assemblers = new LinkedHashMap<>();

    public AuditSnapshotAssemblerRegistry(List<AuditSnapshotAssembler> assemblerList) {
        if (assemblerList != null) {
            for (AuditSnapshotAssembler assembler : assemblerList) {
                String objectType = assembler.objectType();
                if (!hasText(objectType)) {
                    throw new IllegalStateException("Audit snapshot assembler objectType must not be blank.");
                }
                AuditSnapshotAssembler previous = assemblers.putIfAbsent(objectType, assembler);
                if (previous != null) {
                    throw new IllegalStateException("Duplicate audit snapshot assembler objectType: " + objectType);
                }
            }
        }
    }

    public AuditSnapshotAssembler get(String objectType) {
        return assemblers.get(objectType);
    }

    public List<AuditSnapshotAssembler> list() {
        List<AuditSnapshotAssembler> result = new ArrayList<>(assemblers.values());
        result.sort(Comparator.comparing(AuditSnapshotAssembler::objectType));
        return result;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
