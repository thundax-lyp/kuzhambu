package com.thundax.kuzhambu.system.application.audit.runtime.sys;

import com.thundax.kuzhambu.system.application.audit.runtime.AuditObjectLoader;
import com.thundax.kuzhambu.system.application.core.service.DictService;
import com.thundax.kuzhambu.system.domain.core.codec.DictIdCodec;
import org.springframework.stereotype.Component;

@Component
public class DictAuditObjectLoader implements AuditObjectLoader {

    private final DictService dictService;

    public DictAuditObjectLoader(DictService dictService) {
        this.dictService = dictService;
    }

    @Override
    public String objectType() {
        return "Dict";
    }

    @Override
    public Object load(String objectId) {
        return dictService.get(DictIdCodec.toDomain(Long.valueOf(objectId)));
    }
}
