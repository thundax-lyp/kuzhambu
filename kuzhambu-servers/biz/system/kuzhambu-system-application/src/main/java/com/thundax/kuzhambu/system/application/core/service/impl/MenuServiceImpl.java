package com.thundax.kuzhambu.system.application.core.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.thundax.kuzhambu.system.application.core.codec.AccessRankCodec;
import com.thundax.kuzhambu.system.application.core.dao.MenuDao;
import com.thundax.kuzhambu.system.application.core.entity.Menu;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.MenuId;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.MenuIdCodec;
import com.thundax.kuzhambu.system.application.core.service.MenuService;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeMenuInfoCommand;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeMenuVisibilityCommand;
import com.thundax.kuzhambu.system.application.core.service.command.CreateMenuCommand;
import com.thundax.kuzhambu.system.application.core.service.command.MoveMenuCommand;
import com.thundax.kuzhambu.system.application.core.service.query.MenuQuery;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class MenuServiceImpl implements MenuService {

    private final MenuDao dao;
    private final List<CacheChangedListener> cacheChangedListeners;

    public MenuServiceImpl(MenuDao dao) {
        this.dao = dao;
        this.cacheChangedListeners = Collections.emptyList();
    }

    @Autowired
    public MenuServiceImpl(MenuDao dao, ObjectProvider<List<CacheChangedListener>> cacheChangedListeners) {
        this.dao = dao;
        this.cacheChangedListeners = cacheChangedListeners == null
                ? Collections.emptyList()
                : cacheChangedListeners.getIfAvailable(Collections::emptyList);
    }

    public Menu get(MenuId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    public List<Menu> list(MenuQuery query) {
        if (query != null && query.getIds() != null) {
            return dao.listByIds(MenuIdCodec.toValues(query.getIds()));
        }
        return dao.list(
                query == null ? null : MenuIdCodec.toValue(query.getParentId()),
                query == null || query.getVisibility() == null
                        ? null
                        : query.getVisibility().value(),
                query == null ? null : AccessRankCodec.toValue(query.getMaxRank()));
    }

    public PageResult<Menu> page(MenuQuery query, PageQuery page) {
        IPage<Menu> dataPage = dao.page(
                query == null ? null : MenuIdCodec.toValue(query.getParentId()),
                query == null || query.getVisibility() == null
                        ? null
                        : query.getVisibility().value(),
                query == null ? null : AccessRankCodec.toValue(query.getMaxRank()),
                page.getPageNo(),
                page.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MenuId create(CreateMenuCommand command) {
        Menu menu = toMenu(command);
        menu.setId(dao.insert(menu));
        afterWrite(menu);
        return menu.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeInfo(ChangeMenuInfoCommand command) {
        Menu menu = toMenu(command);
        dao.update(menu);
        afterWrite(menu);
    }

    private void afterWrite(Menu menu) {
        notifyCacheChanged();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeVisibility(ChangeMenuVisibilityCommand command) {
        Menu menu = new Menu();
        menu.setId(command.getId());
        menu.setVisibility(command.getVisibility());
        int result = dao.updateVisibility(menu);
        notifyCacheChanged();
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public int remove(MenuId id) {
        dao.deleteMenuRole(MenuIdCodec.toValue(id));
        Menu bean = this.get(id);
        if (bean == null) {
            return 0;
        }

        int retVal = dao.deleteById(bean.getId());

        notifyCacheChanged();

        return retVal;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void move(MoveMenuCommand command) {
        dao.moveTreeNode(
                MenuIdCodec.toValue(command.getFromId()),
                MenuIdCodec.toValue(command.getToId()),
                command.getMoveType());
        notifyCacheChanged();
    }

    @Override
    public boolean existsChildRelation(MenuQuery query) {
        return query != null
                && query.getChildId() != null
                && query.getAncestorId() != null
                && dao.isChildOf(MenuIdCodec.toValue(query.getChildId()), MenuIdCodec.toValue(query.getAncestorId()));
    }

    private void notifyCacheChanged() {
        cacheChangedListeners.forEach(CacheChangedListener::onMenuCacheChanged);
    }

    public interface CacheChangedListener {

        void onMenuCacheChanged();
    }

    private Menu toMenu(CreateMenuCommand command) {
        Menu menu = new Menu();
        menu.setId(command.getId());
        menu.setParentId(command.getParentId());
        menu.setName(command.getName());
        menu.setPerms(command.getPerms());
        menu.setRank(command.getRank());
        menu.setVisibility(command.getVisibility());
        menu.setDisplayParams(command.getDisplayParams());
        menu.setUrl(command.getUrl());
        menu.setTarget(command.getTarget());
        menu.setRemarks(command.getRemarks());
        return menu;
    }

    private Menu toMenu(ChangeMenuInfoCommand command) {
        Menu menu = new Menu();
        menu.setId(command.getId());
        menu.setParentId(command.getParentId());
        menu.setName(command.getName());
        menu.setPerms(command.getPerms());
        menu.setRank(command.getRank());
        menu.setVisibility(command.getVisibility());
        menu.setDisplayParams(command.getDisplayParams());
        menu.setUrl(command.getUrl());
        menu.setTarget(command.getTarget());
        menu.setRemarks(command.getRemarks());
        return menu;
    }
}
