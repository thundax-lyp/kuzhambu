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
import com.thundax.kuzhambu.system.application.core.command.RoleSortCommand;
import com.thundax.kuzhambu.system.application.core.query.RoleQuery;
import com.thundax.kuzhambu.system.application.core.service.RoleApplicationService;
import com.thundax.kuzhambu.system.domain.core.codec.MenuIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.RoleIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.Menu;
import com.thundax.kuzhambu.system.domain.core.model.entity.Role;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.RoleId;
import com.thundax.kuzhambu.system.domain.core.repository.RoleRepository;
import java.util.ArrayList;
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

    public Role get(RoleId id) {
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
        afterWrite(role);
        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(type = "Role", id = "#command.orderedIds.![value()]", action = AuditAction.UPDATE, summary = "角色排序")
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
        afterWrite(role);
    }

    private void afterWrite(Role role) {
        dao.deleteRoleMenu(RoleIdCodec.toValue(role.getId()));
        if (role.getMenuIdList() != null && !role.getMenuIdList().isEmpty()) {
            dao.insertRoleMenu(RoleIdCodec.toValue(role.getId()), role.getMenuIdList());
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
        dao.deleteRoleUser(RoleIdCodec.toValue(command.getRoleId()));

        if (command.getUserIds() != null && !command.getUserIds().isEmpty()) {
            dao.insertRoleUser(
                    RoleIdCodec.toValue(command.getRoleId()),
                    command.getUserIds().stream().map(UserIdCodec::toValue).collect(Collectors.toList()));
        }

        notifyCacheChanged();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(type = "Role", id = "#command.id.value()", action = AuditAction.UPDATE, summary = "变更角色状态")
    public int changeStatus(ChangeRoleStatusCommand command) {
        Role role = new Role();
        role.setId(command.getId());
        role.setStatus(command.getStatus());
        int result = dao.updateStatus(role);

        notifyCacheChanged();

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @AuditLog(type = "Role", id = "#id == null ? null : #id.value()", action = AuditAction.DELETE, summary = "删除角色")
    public int remove(RoleId id) {
        Role role = get(id);
        if (role == null) {
            return 0;
        }

        dao.deleteRoleMenu(RoleIdCodec.toValue(id));
        dao.deleteRoleUser(RoleIdCodec.toValue(id));
        int retVal = dao.deleteById(id);

        notifyCacheChanged();

        return retVal;
    }

    @Override
    public List<User> listRoleUsers(RoleQuery query) {
        List<Long> userIdList = dao.listRoleUsers(RoleIdCodec.toValue(query.getId()));
        return userIdList.stream().map(this::newUser).collect(Collectors.toList());
    }

    @Override
    public List<Menu> listRoleMenus(RoleQuery query) {
        List<Long> menuIdList = dao.listRoleMenus(RoleIdCodec.toValue(query.getId()));
        return menuIdList.stream().map(this::newMenu).collect(Collectors.toList());
    }

    private User newUser(Long id) {
        User user = new User();
        user.setId(UserIdCodec.toDomain(id));
        return user;
    }

    private Menu newMenu(Long id) {
        Menu menu = new Menu();
        menu.setId(MenuIdCodec.toDomain(id));
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

    private List<Long> toValues(List<RoleId> ids) {
        List<Long> values = new ArrayList<>(ids.size());
        for (RoleId id : ids) {
            values.add(id.value());
        }
        return values;
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
        role.setMenuIdList(MenuIdCodec.toValues(command.getMenuIdList()));
        return role;
    }

    private Role toRole(ChangeRoleInfoCommand command) {
        Role role = new Role();
        role.setId(command.getId());
        role.setName(command.getName());
        role.setPrivilege(command.getPrivilege());
        role.setStatus(command.getStatus());
        role.setRemarks(command.getRemarks());
        role.setMenuIdList(MenuIdCodec.toValues(command.getMenuIdList()));
        return role;
    }
}
