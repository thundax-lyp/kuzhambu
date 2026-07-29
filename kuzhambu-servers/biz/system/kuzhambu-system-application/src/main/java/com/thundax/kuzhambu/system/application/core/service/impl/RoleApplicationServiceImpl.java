package com.thundax.kuzhambu.system.application.core.service.impl;

import com.thundax.kuzhambu.common.audit.annotation.AuditLog;
import com.thundax.kuzhambu.common.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.exception.ErrorCode;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.common.core.sort.SortablePrioritySwapSupport;
import com.thundax.kuzhambu.system.application.core.command.AssignRoleUsersCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeRoleInfoCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeRoleStatusCommand;
import com.thundax.kuzhambu.system.application.core.command.CreateRoleCommand;
import com.thundax.kuzhambu.system.application.core.command.RemoveRoleCommand;
import com.thundax.kuzhambu.system.application.core.command.RoleSortCommand;
import com.thundax.kuzhambu.system.application.core.query.GetRoleQuery;
import com.thundax.kuzhambu.system.application.core.query.RoleQuery;
import com.thundax.kuzhambu.system.application.core.service.RoleApplicationService;
import com.thundax.kuzhambu.system.domain.core.model.entity.Menu;
import com.thundax.kuzhambu.system.domain.core.model.entity.Role;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.MenuId;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.RoleId;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import com.thundax.kuzhambu.system.domain.core.repository.RoleRepository;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class RoleApplicationServiceImpl implements RoleApplicationService {

    private static final int PRIORITY_STEP = 1;

    private final RoleRepository dao;
    private final ObjectProvider<List<CacheChangedListener>> cacheChangedListeners;

    public RoleApplicationServiceImpl(
            RoleRepository dao, ObjectProvider<List<CacheChangedListener>> cacheChangedListeners) {
        this.dao = dao;
        this.cacheChangedListeners = cacheChangedListeners;
    }

    public Role get(GetRoleQuery query) {
        RoleId id = query == null ? null : query.getId();
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    public List<Role> list(RoleQuery query) {
        return dao.list(
                query == null || query.getStatus() == null
                        ? null
                        : query.getStatus().value());
    }

    public PageResult<Role> page(RoleQuery query, PageQuery page) {
        return dao.page(
                query == null || query.getStatus() == null
                        ? null
                        : query.getStatus().value(),
                page.getPageNo(),
                page.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(type = "Role", id = "", action = AuditAction.CREATE, summary = "创建角色")
    public RoleId create(CreateRoleCommand command) {
        Role role = toRole(command);
        role.setPriority(dao.maxPriority() + PRIORITY_STEP);
        role.setId(dao.insert(role));
        afterWrite(role, command.getMenuIdList());
        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sort(RoleSortCommand command) {
        List<RoleId> orderedIdList =
                command == null || command.getOrderedIds() == null ? Collections.emptyList() : command.getOrderedIds();
        if (orderedIdList.isEmpty()) {
            throw new BizException(
                    ErrorCode.SORT_EMPTY_INPUT.getCode(),
                    ErrorCode.SORT_EMPTY_INPUT.getMessageKey(),
                    ErrorCode.SORT_EMPTY_INPUT.getMessage());
        }

        List<Role> currentRoles = dao.list(SortDirection.ASC);
        SortablePrioritySwapSupport.sort(
                orderedIdList,
                currentRoles,
                Role::getId,
                RoleId::value,
                Role::getPriority,
                dao::maxPriority,
                (id, priority) -> updatePriorityOrThrow(id, priority, "排序更新失败"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(
            type = "Role",
            id = "#command.id.value()",
            action = AuditAction.UPDATE,
            summary = "更新角色",
            recordWhenUnchanged = true)
    public void changeInfo(ChangeRoleInfoCommand command) {
        Role role = toRole(command);
        dao.update(role);
        afterWrite(role, command.getMenuIdList());
    }

    private void afterWrite(Role role, List<MenuId> menuIdList) {
        dao.deleteRoleMenu(role.getId());
        if (menuIdList != null && !menuIdList.isEmpty()) {
            dao.insertRoleMenu(role.getId(), menuIdList);
        }

        notifyCacheChanged();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(
            type = "Role",
            id = "#command.roleId.value()",
            action = AuditAction.UPDATE_RELATION,
            summary = "分配角色用户",
            recordWhenUnchanged = true)
    public void assignUsers(AssignRoleUsersCommand command) {
        dao.deleteRoleUser(command.getRoleId());

        if (command.getUserIds() != null && !command.getUserIds().isEmpty()) {
            dao.insertRoleUser(command.getRoleId(), command.getUserIds());
        }

        notifyCacheChanged();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(
            type = "Role",
            id = "#command.id.value()",
            action = AuditAction.UPDATE,
            summary = "变更角色状态",
            recordWhenUnchanged = true)
    public int changeStatus(ChangeRoleStatusCommand command) {
        Role role = new Role();
        role.setId(command.getId());
        role.setStatus(command.getStatus());
        int result = dao.updateStatus(role);

        notifyCacheChanged();

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @AuditLog(
            type = "Role",
            id = "#command.id == null ? null : #command.id.value()",
            action = AuditAction.DELETE,
            summary = "删除角色")
    public int remove(RemoveRoleCommand command) {
        RoleId id = command == null ? null : command.getId();
        Role role = get(new GetRoleQuery(id));
        if (role == null) {
            return 0;
        }

        dao.deleteRoleMenu(id);
        dao.deleteRoleUser(id);
        int retVal = dao.deleteById(id);

        notifyCacheChanged();

        return retVal;
    }

    @Override
    public List<User> listRoleUsers(RoleQuery query) {
        List<UserId> userIdList = dao.listRoleUsers(query.getId());
        return userIdList.stream().map(this::newUser).collect(Collectors.toList());
    }

    @Override
    public List<Menu> listRoleMenus(RoleQuery query) {
        List<MenuId> menuIdList = dao.listRoleMenus(query.getId());
        return menuIdList.stream().map(this::newMenu).collect(Collectors.toList());
    }

    private User newUser(UserId id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private Menu newMenu(MenuId id) {
        Menu menu = new Menu();
        menu.setId(id);
        return menu;
    }

    private void notifyCacheChanged() {
        listeners().forEach(CacheChangedListener::onRoleCacheChanged);
    }

    public interface CacheChangedListener {

        void onRoleCacheChanged();
    }

    private List<CacheChangedListener> listeners() {
        return cacheChangedListeners == null
                ? Collections.emptyList()
                : cacheChangedListeners.getIfAvailable(Collections::emptyList);
    }

    private void updatePriorityOrThrow(RoleId id, int priority, String message) {
        Role role = new Role();
        role.setId(id);
        role.setPriority(priority);
        int updated = dao.updatePriority(role);
        if (updated != 1) {
            throw new BizException(
                    ErrorCode.SORT_DB_FAILURE.getCode(), ErrorCode.SORT_DB_FAILURE.getMessageKey(), message);
        }
    }

    private Role toRole(CreateRoleCommand command) {
        Role role = new Role();
        role.setId(command.getId());
        role.setName(command.getName());
        role.setPrivilege(command.getPrivilege());
        role.setStatus(command.getStatus());
        role.setRemarks(command.getRemarks());
        return role;
    }

    private Role toRole(ChangeRoleInfoCommand command) {
        Role role = new Role();
        role.setId(command.getId());
        role.setName(command.getName());
        role.setPrivilege(command.getPrivilege());
        role.setStatus(command.getStatus());
        role.setRemarks(command.getRemarks());
        return role;
    }
}
