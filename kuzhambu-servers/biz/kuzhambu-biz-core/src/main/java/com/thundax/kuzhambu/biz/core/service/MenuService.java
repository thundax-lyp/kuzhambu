package com.thundax.kuzhambu.biz.core.service;

import com.thundax.kuzhambu.biz.core.entity.Menu;
import com.thundax.kuzhambu.biz.core.entity.valueobject.MenuId;
import com.thundax.kuzhambu.biz.core.service.command.ChangeMenuInfoCommand;
import com.thundax.kuzhambu.biz.core.service.command.ChangeMenuVisibilityCommand;
import com.thundax.kuzhambu.biz.core.service.command.CreateMenuCommand;
import com.thundax.kuzhambu.biz.core.service.command.MoveMenuCommand;
import com.thundax.kuzhambu.biz.core.service.query.MenuQuery;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.List;

public interface MenuService {

    Menu get(MenuId id);

    List<Menu> list(MenuQuery query);

    PageResult<Menu> page(MenuQuery query, PageQuery page);

    MenuId create(CreateMenuCommand command);

    void changeInfo(ChangeMenuInfoCommand command);

    int remove(MenuId id);

    int changeVisibility(ChangeMenuVisibilityCommand command);

    void move(MoveMenuCommand command);

    boolean existsChildRelation(MenuQuery query);
}
