package com.thundax.kuzhambu.system.application.audit.runtime.sys;

import com.thundax.kuzhambu.system.application.audit.runtime.AuditObjectLoader;
import com.thundax.kuzhambu.system.application.core.service.MenuApplicationService;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.MenuId;
import org.springframework.stereotype.Component;

@Component
public class MenuAuditObjectLoader implements AuditObjectLoader {

    private final MenuApplicationService menuService;

    public MenuAuditObjectLoader(MenuApplicationService menuService) {
        this.menuService = menuService;
    }

    @Override
    public String objectType() {
        return "Menu";
    }

    @Override
    public Object load(String objectId) {
        return menuService.get(MenuId.of(Long.valueOf(objectId)));
    }
}
