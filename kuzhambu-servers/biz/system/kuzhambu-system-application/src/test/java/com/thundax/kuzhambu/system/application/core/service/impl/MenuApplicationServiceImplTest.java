package com.thundax.kuzhambu.system.application.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.tree.TreeNodeMoveType;
import com.thundax.kuzhambu.system.application.core.query.MenuQuery;
import com.thundax.kuzhambu.system.domain.core.model.entity.Menu;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.AccessRank;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.MenuId;
import com.thundax.kuzhambu.system.domain.core.repository.MenuRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MenuApplicationServiceImplTest {

    @Test
    void listShouldNotApplyRankFilterWhenMaxRankIsAbsent() {
        CapturingMenuRepository repository = new CapturingMenuRepository();
        MenuApplicationServiceImpl service = new MenuApplicationServiceImpl(repository);

        service.list(new MenuQuery());

        assertNull(repository.lastMaxRank);
    }

    @Test
    void listShouldApplyRankFilterWhenMaxRankIsPresent() {
        CapturingMenuRepository repository = new CapturingMenuRepository();
        MenuApplicationServiceImpl service = new MenuApplicationServiceImpl(repository);
        MenuQuery query = new MenuQuery();
        query.setMaxRank(AccessRank.of(9));

        service.list(query);

        assertEquals(9, repository.lastMaxRank);
    }

    @Test
    void pageShouldNotApplyRankFilterWhenMaxRankIsAbsent() {
        CapturingMenuRepository repository = new CapturingMenuRepository();
        MenuApplicationServiceImpl service = new MenuApplicationServiceImpl(repository);

        service.page(new MenuQuery(), new PageQuery(1, 10));

        assertNull(repository.lastMaxRank);
    }

    private static class CapturingMenuRepository implements MenuRepository {

        private Integer lastMaxRank;

        @Override
        public Menu getById(MenuId id) {
            return null;
        }

        @Override
        public List<Menu> listByIds(List<Long> idList) {
            return new ArrayList<>();
        }

        @Override
        public List<Menu> list(Long parentId, String visibility, Integer maxRank) {
            this.lastMaxRank = maxRank;
            return new ArrayList<>();
        }

        @Override
        public PageResult<Menu> page(Long parentId, String visibility, Integer maxRank, int pageNo, int pageSize) {
            this.lastMaxRank = maxRank;
            return PageResult.of(pageNo, pageSize, 0, new ArrayList<>());
        }

        @Override
        public MenuId insert(Menu menu) {
            return null;
        }

        @Override
        public int update(Menu menu) {
            return 0;
        }

        @Override
        public int deleteById(MenuId id) {
            return 0;
        }

        @Override
        public void moveTreeNode(Long fromId, Long toId, TreeNodeMoveType moveType) {}

        @Override
        public boolean isChildOf(Long childId, Long parentId) {
            return false;
        }

        @Override
        public int updateVisibility(Menu menu) {
            return 0;
        }

        @Override
        public void deleteMenuRole(Long menuId) {}
    }
}
