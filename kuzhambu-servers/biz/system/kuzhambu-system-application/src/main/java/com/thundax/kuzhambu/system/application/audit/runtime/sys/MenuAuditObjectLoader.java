package com.thundax.kuzhambu.system.application.audit.runtime.sys;

import com.thundax.kuzhambu.common.audit.runtime.AuditObjectLoader;
import com.thundax.kuzhambu.system.application.core.query.GetMenuQuery;
import com.thundax.kuzhambu.system.application.core.service.MenuManagementApplicationService;
import com.thundax.kuzhambu.system.domain.core.codec.MenuIdCodec;
import org.springframework.stereotype.Component;

@Component
public class MenuAuditObjectLoader implements AuditObjectLoader {

    private final MenuManagementApplicationService menuService;

    public MenuAuditObjectLoader(MenuManagementApplicationService menuService) {
        this.menuService = menuService;
    }

    @Override
    public String objectType() {
        return "Menu";
    }

    @Override
    public Object load(String objectId) {
        return menuService.get(new GetMenuQuery(MenuIdCodec.toDomain(Long.valueOf(objectId))));
    }
}
