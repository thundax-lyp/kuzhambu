package com.thundax.kuzhambu.system.domain.core.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.tree.TreeNodeMoveType;
import com.thundax.kuzhambu.system.domain.core.model.entity.Menu;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.MenuId;
import java.util.List;

public interface MenuRepository {

    Menu getById(MenuId id);

    List<Menu> listByIds(List<Long> idList);

    List<Menu> list(Long parentId, String visibility, Integer maxRank);

    PageResult<Menu> page(Long parentId, String visibility, Integer maxRank, int pageNo, int pageSize);

    MenuId insert(Menu menu);

    int update(Menu menu);

    int deleteById(MenuId id);

    void moveTreeNode(Long fromId, Long toId, TreeNodeMoveType moveType);

    boolean isChildOf(Long childId, Long parentId);

    int updateVisibility(Menu menu);

    void deleteMenuRole(Long menuId);
}
