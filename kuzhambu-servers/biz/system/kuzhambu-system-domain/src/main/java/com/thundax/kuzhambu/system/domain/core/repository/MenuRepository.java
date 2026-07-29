package com.thundax.kuzhambu.system.domain.core.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.tree.TreeNodeMoveType;
import com.thundax.kuzhambu.system.domain.core.model.entity.Menu;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.MenuId;
import java.util.List;

public interface MenuRepository {

    Menu getById(MenuId id);

    List<Menu> listByIds(List<MenuId> idList);

    List<Menu> list(MenuId parentId, String visibility, Integer maxRank);

    PageResult<Menu> page(MenuId parentId, String visibility, Integer maxRank, int pageNo, int pageSize);

    MenuId insert(Menu menu);

    int update(Menu menu);

    int deleteById(MenuId id);

    void moveTreeNode(MenuId fromId, MenuId toId, TreeNodeMoveType moveType);

    boolean isChildOf(MenuId childId, MenuId parentId);

    int updateVisibility(Menu menu);

    void deleteMenuRole(MenuId menuId);
}
