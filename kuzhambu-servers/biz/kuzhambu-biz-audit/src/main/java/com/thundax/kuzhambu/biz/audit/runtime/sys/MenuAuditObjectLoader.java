package com.thundax.kuzhambu.biz.audit.runtime.sys;

import com.thundax.kuzhambu.biz.audit.runtime.AuditObjectLoader;
import com.thundax.kuzhambu.biz.core.entity.valueobject.MenuId;
import com.thundax.kuzhambu.biz.core.service.MenuService;
import org.springframework.stereotype.Component;

@Component
public class MenuAuditObjectLoader implements AuditObjectLoader {

    private final MenuService menuService;

    public MenuAuditObjectLoader(MenuService menuService) {
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
