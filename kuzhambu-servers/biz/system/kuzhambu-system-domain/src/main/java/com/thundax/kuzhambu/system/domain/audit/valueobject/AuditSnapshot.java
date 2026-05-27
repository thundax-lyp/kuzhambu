package com.thundax.kuzhambu.system.domain.audit.valueobject;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditSnapshot {

    private int schemaVersion = 1;
    private String objectType;
    private String objectId;
    private String displayName;
    private List<AuditField> fields = new ArrayList<>();
}
