package com.thundax.kuzhambu.system.application.core.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.thundax.kuzhambu.system.application.core.dao.RoleDao;
import com.thundax.kuzhambu.system.application.core.entity.Menu;
import com.thundax.kuzhambu.system.application.core.entity.Role;
import com.thundax.kuzhambu.system.application.core.entity.User;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.MenuIdCodec;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.RoleId;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.RoleIdCodec;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.UserIdCodec;
import com.thundax.kuzhambu.system.application.core.service.RoleService;
import com.thundax.kuzhambu.system.application.core.service.command.AssignRoleUsersCommand;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeRoleInfoCommand;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeRoleStatusCommand;
import com.thundax.kuzhambu.system.application.core.service.command.CreateRoleCommand;
import com.thundax.kuzhambu.system.application.core.service.command.RoleSortCommand;
import com.thundax.kuzhambu.system.application.core.service.query.RoleQuery;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.exception.ErrorCode;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class RoleServiceImpl implements RoleService {

    private static final int PRIORITY_STEP = 10;

    private final RoleDao dao;
    private final List<CacheChangedListener> cacheChangedListeners;

    public RoleServiceImpl(RoleDao dao) {
        this.dao = dao;
        this.cacheChangedListeners = Collections.emptyList();
    }

    @Autowired
    public RoleServiceImpl(RoleDao dao, ObjectProvider<List<CacheChangedListener>> cacheChangedListeners) {
        this.dao = dao;
        this.cacheChangedListeners = cacheChangedListeners == null
                ? Collections.emptyList()
                : cacheChangedListeners.getIfAvailable(Collections::emptyList);
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
        IPage<Role> dataPage = dao.page(
                query == null || query.getStatus() == null
                        ? null
                        : query.getStatus().value(),
                page.getPageNo(),
                page.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleId create(CreateRoleCommand command) {
        Role role = toRole(command);
        role.setPriority(dao.maxPriority() + PRIORITY_STEP);
        role.setId(dao.insert(role));
        afterWrite(role);
        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sort(RoleSortCommand command) {
        SortDirection effectiveDirection =
                command == null || command.getSortDirection() == null ? SortDirection.ASC : command.getSortDirection();
        List<RoleId> orderedIdList =
                command == null || command.getOrderedIds() == null ? Collections.emptyList() : command.getOrderedIds();
        if (orderedIdList.isEmpty()) {
            throw new BizException(
                    ErrorCode.SORT_EMPTY_INPUT.getCode(),
                    ErrorCode.SORT_EMPTY_INPUT.getMessageKey(),
                    ErrorCode.SORT_EMPTY_INPUT.getMessage());
        }

        List<Role> selectedRoles = dao.listByIds(toValues(orderedIdList));
        if (selectedRoles == null || selectedRoles.isEmpty()) {
            throw new BizException(
                    ErrorCode.SORT_MISSING_ID.getCode(),
                    ErrorCode.SORT_MISSING_ID.getMessageKey(),
                    ErrorCode.SORT_MISSING_ID.getMessage());
        }

        for (Role role : selectedRoles) {
            if (role == null || role.getId() == null) {
                throw new BizException(
                        ErrorCode.SORT_DB_FAILURE.getCode(),
                        ErrorCode.SORT_DB_FAILURE.getMessageKey(),
                        ErrorCode.SORT_DB_FAILURE.getMessage());
            }
        }

        for (RoleId orderedId : orderedIdList) {
            if (orderedId == null || orderedId.value() == null) {
                throw new BizException(
                        ErrorCode.SORT_MISSING_ID.getCode(),
                        ErrorCode.SORT_MISSING_ID.getMessageKey(),
                        ErrorCode.SORT_MISSING_ID.getMessage());
            }
        }

        List<Role> currentRoles = dao.list(effectiveDirection);
        if (currentRoles == null || currentRoles.isEmpty()) {
            throw new BizException(
                    ErrorCode.SORT_MISSING_ID.getCode(),
                    ErrorCode.SORT_MISSING_ID.getMessageKey(),
                    ErrorCode.SORT_MISSING_ID.getMessage());
        }
        if (currentRoles.size() != orderedIdList.size()) {
            throw new BizException(
                    ErrorCode.SORT_MISSING_ID.getCode(),
                    ErrorCode.SORT_MISSING_ID.getMessageKey(),
                    ErrorCode.SORT_MISSING_ID.getMessage());
        }

        Map<Long, Integer> indexById = new HashMap<>(currentRoles.size());
        Map<Long, Integer> priorityById = new HashMap<>(currentRoles.size());
        List<RoleId> currentOrderedIds = new ArrayList<>(currentRoles.size());
        for (int i = 0; i < currentRoles.size(); i++) {
            Role role = currentRoles.get(i);
            if (role == null || role.getId() == null) {
                throw new BizException(
                        ErrorCode.SORT_DB_FAILURE.getCode(),
                        ErrorCode.SORT_DB_FAILURE.getMessageKey(),
                        ErrorCode.SORT_DB_FAILURE.getMessage());
            }
            long roleId = role.getId().value();
            indexById.put(roleId, i);
            priorityById.put(roleId, role.getPriority());
            currentOrderedIds.add(role.getId());
        }

        for (RoleId orderedId : orderedIdList) {
            if (!indexById.containsKey(orderedId.value())) {
                throw new BizException(
                        ErrorCode.SORT_MISSING_ID.getCode(),
                        ErrorCode.SORT_MISSING_ID.getMessageKey(),
                        ErrorCode.SORT_MISSING_ID.getMessage());
            }
        }

        int temporaryPriority = dao.maxPriority() + PRIORITY_STEP;
        for (int i = 0; i < currentOrderedIds.size(); i++) {
            RoleId targetId = orderedIdList.get(i);
            RoleId currentId = currentOrderedIds.get(i);
            if (targetId.equals(currentId)) {
                continue;
            }

            int targetIndex = indexById.get(targetId.value());
            int currentPriority = priorityById.get(currentId.value());
            int targetPriority = priorityById.get(targetId.value());

            updatePriorityOrThrow(targetId, temporaryPriority++, "暂态更新失败");
            updatePriorityOrThrow(currentId, targetPriority, "交换更新失败");
            updatePriorityOrThrow(targetId, currentPriority, "交换更新失败");

            priorityById.put(targetId.value(), currentPriority);
            priorityById.put(currentId.value(), targetPriority);

            currentOrderedIds.set(i, targetId);
            currentOrderedIds.set(targetIndex, currentId);
            indexById.put(targetId.value(), i);
            indexById.put(currentId.value(), targetIndex);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
    public int changeStatus(ChangeRoleStatusCommand command) {
        Role role = new Role();
        role.setId(command.getId());
        role.setStatus(command.getStatus());
        int result = dao.updateStatus(role);

        notifyCacheChanged();

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
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
        cacheChangedListeners.forEach(CacheChangedListener::onRoleCacheChanged);
    }

    public interface CacheChangedListener {

        void onRoleCacheChanged();
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
