package com.thundax.kuzhambu.system.application.core.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.application.core.command.ChangeMenuInfoCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeMenuVisibilityCommand;
import com.thundax.kuzhambu.system.application.core.command.CreateMenuCommand;
import com.thundax.kuzhambu.system.application.core.command.MoveMenuCommand;
import com.thundax.kuzhambu.system.application.core.command.RemoveMenuCommand;
import com.thundax.kuzhambu.system.application.core.query.GetMenuQuery;
import com.thundax.kuzhambu.system.application.core.query.MenuQuery;
import com.thundax.kuzhambu.system.domain.core.model.entity.Menu;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.MenuId;
import java.util.List;

public interface MenuManagementApplicationService {

    Menu get(GetMenuQuery query);

    default Menu get(MenuId id) {
        return get(new GetMenuQuery(id));
    }

    List<Menu> list(MenuQuery query);

    PageResult<Menu> page(MenuQuery query, PageQuery page);

    MenuId create(CreateMenuCommand command);

    void changeInfo(ChangeMenuInfoCommand command);

    int remove(RemoveMenuCommand command);

    int changeVisibility(ChangeMenuVisibilityCommand command);

    void move(MoveMenuCommand command);

    boolean existsChildRelation(MenuQuery query);
}
