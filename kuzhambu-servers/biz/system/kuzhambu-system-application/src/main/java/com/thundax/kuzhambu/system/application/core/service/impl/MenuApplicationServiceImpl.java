package com.thundax.kuzhambu.system.application.core.service.impl;

import com.thundax.kuzhambu.common.audit.annotation.AuditLog;
import com.thundax.kuzhambu.common.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.application.core.command.ChangeMenuInfoCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeMenuVisibilityCommand;
import com.thundax.kuzhambu.system.application.core.command.CreateMenuCommand;
import com.thundax.kuzhambu.system.application.core.command.MoveMenuCommand;
import com.thundax.kuzhambu.system.application.core.query.MenuQuery;
import com.thundax.kuzhambu.system.application.core.service.MenuApplicationService;
import com.thundax.kuzhambu.system.domain.core.codec.AccessRankCodec;
import com.thundax.kuzhambu.system.domain.core.codec.MenuIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.Menu;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.MenuId;
import com.thundax.kuzhambu.system.domain.core.repository.MenuRepository;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class MenuApplicationServiceImpl implements MenuApplicationService {

    private final MenuRepository dao;
    private final ObjectProvider<List<CacheChangedListener>> cacheChangedListeners;

    public MenuApplicationServiceImpl(
            MenuRepository dao, ObjectProvider<List<CacheChangedListener>> cacheChangedListeners) {
        this.dao = dao;
        this.cacheChangedListeners = cacheChangedListeners;
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
                maxRankValue(query));
    }

    public PageResult<Menu> page(MenuQuery query, PageQuery page) {
        return dao.page(
                query == null ? null : MenuIdCodec.toValue(query.getParentId()),
                query == null || query.getVisibility() == null
                        ? null
                        : query.getVisibility().value(),
                maxRankValue(query),
                page.getPageNo(),
                page.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(type = "Menu", id = "", action = AuditAction.CREATE, summary = "创建菜单")
    public MenuId create(CreateMenuCommand command) {
        Menu menu = toMenu(command);
        menu.setId(dao.insert(menu));
        afterWrite(menu);
        return menu.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(type = "Menu", id = "#command.id.value()", action = AuditAction.UPDATE, summary = "更新菜单")
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
    @AuditLog(type = "Menu", id = "#command.id.value()", action = AuditAction.UPDATE, summary = "变更菜单可见性")
    public int changeVisibility(ChangeMenuVisibilityCommand command) {
        Menu menu = new Menu();
        menu.setId(command.getId());
        menu.setVisibility(command.getVisibility());
        int result = dao.updateVisibility(menu);
        notifyCacheChanged();
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @AuditLog(type = "Menu", id = "#id == null ? null : #id.value()", action = AuditAction.DELETE, summary = "删除菜单")
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
    @AuditLog(type = "Menu", id = "#command.fromId.value()", action = AuditAction.UPDATE, summary = "移动菜单")
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
        listeners().forEach(CacheChangedListener::onMenuCacheChanged);
    }

    private Integer maxRankValue(MenuQuery query) {
        return query == null || query.getMaxRank() == null ? null : AccessRankCodec.toValue(query.getMaxRank());
    }

    public interface CacheChangedListener {

        void onMenuCacheChanged();
    }

    private List<CacheChangedListener> listeners() {
        return cacheChangedListeners == null
                ? Collections.emptyList()
                : cacheChangedListeners.getIfAvailable(Collections::emptyList);
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
