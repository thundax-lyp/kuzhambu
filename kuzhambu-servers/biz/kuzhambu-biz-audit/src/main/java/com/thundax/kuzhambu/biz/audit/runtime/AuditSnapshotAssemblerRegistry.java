package com.thundax.kuzhambu.biz.audit.runtime;

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
                assemblers.put(assembler.objectType(), assembler);
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
}
