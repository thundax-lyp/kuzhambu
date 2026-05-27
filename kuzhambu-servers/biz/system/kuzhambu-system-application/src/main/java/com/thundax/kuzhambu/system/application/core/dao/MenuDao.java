package com.thundax.kuzhambu.system.application.core.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.system.application.core.entity.Menu;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.MenuId;
import com.thundax.kuzhambu.common.core.tree.TreeNodeMoveType;
import java.util.List;

public interface MenuDao {

    Menu getById(MenuId id);

    List<Menu> listByIds(List<Long> idList);

    List<Menu> list(Long parentId, String visibility, Integer maxRank);

    Page<Menu> page(Long parentId, String visibility, Integer maxRank, int pageNo, int pageSize);

    MenuId insert(Menu menu);

    int update(Menu menu);

    int deleteById(MenuId id);

    void moveTreeNode(Long fromId, Long toId, TreeNodeMoveType moveType);

    boolean isChildOf(Long childId, Long parentId);

    int updateVisibility(Menu menu);

    void deleteMenuRole(Long menuId);
}
