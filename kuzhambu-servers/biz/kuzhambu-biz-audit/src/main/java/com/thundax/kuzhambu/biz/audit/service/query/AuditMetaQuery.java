package com.thundax.kuzhambu.biz.audit.service.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditMetaQuery {

    private String objectType;
    private String objectId;
}
