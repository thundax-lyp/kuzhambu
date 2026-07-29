package com.thundax.kuzhambu.system.application.core.service.impl;

import com.thundax.kuzhambu.common.audit.annotation.AuditLog;
import com.thundax.kuzhambu.common.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.application.core.command.ChangeUserInfoCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeUserStatusCommand;
import com.thundax.kuzhambu.system.application.core.command.CreateUserCommand;
import com.thundax.kuzhambu.system.application.core.query.UserQuery;
import com.thundax.kuzhambu.system.application.core.service.UserApplicationService;
import com.thundax.kuzhambu.system.application.core.service.handler.UserDeleteCascadeHandler;
import com.thundax.kuzhambu.system.domain.core.model.entity.Role;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.RoleId;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import com.thundax.kuzhambu.system.domain.core.repository.UserRepository;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class UserApplicationServiceImpl implements UserApplicationService {

    private final UserRepository dao;
    private final List<UserDeleteCascadeHandler> deleteCascadeHandlers;
    private final ObjectProvider<List<RoleApplicationServiceImpl.CacheChangedListener>> roleCacheChangedListeners;

    public UserApplicationServiceImpl(
            UserRepository dao,
            ObjectProvider<List<UserDeleteCascadeHandler>> deleteCascadeHandlers,
            ObjectProvider<List<RoleApplicationServiceImpl.CacheChangedListener>> roleCacheChangedListeners) {
        this.dao = dao;
        this.deleteCascadeHandlers = deleteCascadeHandlers == null
                ? Collections.emptyList()
                : deleteCascadeHandlers.getIfAvailable(Collections::emptyList);
        this.roleCacheChangedListeners = roleCacheChangedListeners;
    }

    public User get(UserId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    public List<User> list(UserQuery query) {
        return dao.list(
                query == null ? null : query.getDepartmentId(),
                query == null ? null : query.getLoginName(),
                query == null ? null : query.getName(),
                query == null ? null : query.getStatus(),
                query == null ? null : query.getPrivilege());
    }

    public PageResult<User> page(UserQuery query, PageQuery page) {
        return dao.page(
                query == null ? null : query.getDepartmentId(),
                query == null ? null : query.getLoginName(),
                query == null ? null : query.getName(),
                query == null ? null : query.getStatus(),
                query == null ? null : query.getPrivilege(),
                page.getPageNo(),
                page.getPageSize());
    }

    @Override
    public boolean existsEmail(UserQuery query) {
        return query != null && dao.countByEmail(query.getEmail(), query.getExcludedId()) > 0;
    }

    @Override
    public boolean existsMobile(UserQuery query) {
        return query != null && dao.countByMobile(query.getMobile(), query.getExcludedId()) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(type = "User", id = "", action = AuditAction.CREATE, summary = "创建后台用户")
    public UserId create(CreateUserCommand command) {
        User user = toUser(command);
        user.setId(dao.insert(user));
        rewriteUserRoles(user.getId(), command.getRoleIdList());
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(
            type = "User",
            id = "#command.id.value()",
            action = AuditAction.UPDATE,
            summary = "更新后台用户",
            recordWhenUnchanged = true)
    public void changeInfo(ChangeUserInfoCommand command) {
        User user = toUser(command);
        dao.update(user);
        rewriteUserRoles(user.getId(), command.getRoleIdList());
    }

    private void rewriteUserRoles(UserId userId, List<RoleId> roleIdList) {
        if (roleIdList != null) {
            dao.deleteUserRole(userId);
            if (!roleIdList.isEmpty()) {
                dao.insertUserRole(userId, roleIdList);
            }
            notifyRoleCacheChanged();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(
            type = "User",
            id = "#command.id.value()",
            action = AuditAction.UPDATE,
            summary = "变更后台用户状态",
            before = "#command.beforeUser",
            after = "#command.afterUser",
            recordWhenUnchanged = true)
    public int changeStatus(ChangeUserStatusCommand command) {
        User user = new User();
        user.setId(command.getId());
        user.setStatus(command.getStatus());
        command.setAfterUser(auditAfterUser(command));
        int result = dao.updateStatus(user);
        notifyRoleCacheChanged();
        return result;
    }

    private User auditAfterUser(ChangeUserStatusCommand command) {
        User user = new User();
        User beforeUser = command.getBeforeUser();
        if (beforeUser != null) {
            user.setId(beforeUser.getId());
            user.setName(beforeUser.getName());
            user.setPrivilege(beforeUser.getPrivilege());
        } else {
            user.setId(command.getId());
        }
        user.setStatus(command.getStatus());
        return user;
    }

    @Transactional(rollbackFor = Exception.class)
    @AuditLog(type = "User", id = "#id == null ? null : #id.value()", action = AuditAction.DELETE, summary = "删除后台用户")
    public int remove(UserId id) {
        User user = get(id);
        if (user == null) {
            return 0;
        }

        for (UserDeleteCascadeHandler deleteCascadeHandler : deleteCascadeHandlers) {
            deleteCascadeHandler.beforeDelete(user);
        }
        dao.deleteUserRole(id);
        notifyRoleCacheChanged();

        return dao.deleteById(id);
    }

    @Override
    public List<Role> listUserRoles(UserQuery query) {
        return dao.listUserRoles(query.getId()).stream().map(this::newRole).collect(Collectors.toList());
    }

    private Role newRole(RoleId id) {
        Role role = new Role();
        role.setId(id);
        return role;
    }

    private void notifyRoleCacheChanged() {
        roleCacheChangedListeners().forEach(RoleApplicationServiceImpl.CacheChangedListener::onRoleCacheChanged);
    }

    private List<RoleApplicationServiceImpl.CacheChangedListener> roleCacheChangedListeners() {
        return roleCacheChangedListeners == null
                ? Collections.emptyList()
                : roleCacheChangedListeners.getIfAvailable(Collections::emptyList);
    }

    private User toUser(CreateUserCommand command) {
        User user = new User();
        user.setId(command.getId());
        user.setDepartmentId(command.getDepartmentId());
        user.setEmail(command.getEmail());
        user.setMobile(command.getMobile());
        user.setTel(command.getTel());
        user.setName(command.getName());
        user.setRank(command.getRank());
        user.setPrivilege(command.getPrivilege());
        user.setStatus(command.getStatus());
        user.setRemarks(command.getRemarks());
        return user;
    }

    private User toUser(ChangeUserInfoCommand command) {
        User user = new User();
        user.setId(command.getId());
        user.setDepartmentId(command.getDepartmentId());
        user.setEmail(command.getEmail());
        user.setMobile(command.getMobile());
        user.setTel(command.getTel());
        user.setName(command.getName());
        user.setRank(command.getRank());
        user.setPrivilege(command.getPrivilege());
        user.setStatus(command.getStatus());
        user.setRemarks(command.getRemarks());
        return user;
    }
}
