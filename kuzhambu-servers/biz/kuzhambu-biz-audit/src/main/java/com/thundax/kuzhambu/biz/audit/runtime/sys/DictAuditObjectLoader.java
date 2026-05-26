package com.thundax.kuzhambu.biz.audit.runtime.sys;

import com.thundax.kuzhambu.biz.audit.runtime.AuditObjectLoader;
import com.thundax.kuzhambu.biz.core.entity.valueobject.DictIdCodec;
import com.thundax.kuzhambu.biz.core.service.DictService;
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
