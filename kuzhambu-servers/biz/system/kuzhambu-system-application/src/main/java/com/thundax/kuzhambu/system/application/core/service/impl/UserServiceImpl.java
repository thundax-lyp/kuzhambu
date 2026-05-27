package com.thundax.kuzhambu.system.application.core.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.application.core.service.UserService;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeUserInfoCommand;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeUserStatusCommand;
import com.thundax.kuzhambu.system.application.core.service.command.CreateUserCommand;
import com.thundax.kuzhambu.system.application.core.service.handler.UserDeleteCascadeHandler;
import com.thundax.kuzhambu.system.application.core.service.query.UserQuery;
import com.thundax.kuzhambu.system.domain.core.codec.DepartmentIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.RoleIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
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
public class UserServiceImpl implements UserService {

    private final UserRepository dao;
    private final List<UserDeleteCascadeHandler> deleteCascadeHandlers;

    public UserServiceImpl(UserRepository dao, ObjectProvider<List<UserDeleteCascadeHandler>> deleteCascadeHandlers) {
        this.dao = dao;
        this.deleteCascadeHandlers = deleteCascadeHandlers == null
                ? Collections.emptyList()
                : deleteCascadeHandlers.getIfAvailable(Collections::emptyList);
    }

    public User get(UserId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    public List<User> list(UserQuery query) {
        return dao.list(
                query == null ? null : DepartmentIdCodec.toValue(query.getDepartmentId()),
                query == null ? null : query.getLoginName(),
                query == null ? null : query.getName(),
                query == null ? null : query.getStatus(),
                query == null ? null : query.getPrivilege());
    }

    public PageResult<User> page(UserQuery query, PageQuery page) {
        IPage<User> dataPage = dao.page(
                query == null ? null : DepartmentIdCodec.toValue(query.getDepartmentId()),
                query == null ? null : query.getLoginName(),
                query == null ? null : query.getName(),
                query == null ? null : query.getStatus(),
                query == null ? null : query.getPrivilege(),
                page.getPageNo(),
                page.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
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
    public UserId create(CreateUserCommand command) {
        User user = toUser(command);
        user.setId(dao.insert(user));
        rewriteUserRoles(user.getId(), command.getRoleIdList());
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeInfo(ChangeUserInfoCommand command) {
        User user = toUser(command);
        dao.update(user);
        rewriteUserRoles(user.getId(), command.getRoleIdList());
    }

    private void rewriteUserRoles(UserId userId, List<RoleId> roleIdList) {
        if (roleIdList != null) {
            dao.deleteUserRole(UserIdCodec.toValue(userId));
            if (!roleIdList.isEmpty()) {
                dao.insertUserRole(UserIdCodec.toValue(userId), RoleIdCodec.toValues(roleIdList));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(ChangeUserStatusCommand command) {
        User user = new User();
        user.setId(command.getId());
        user.setStatus(command.getStatus());
        return dao.updateStatus(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public int remove(UserId id) {
        User user = get(id);
        if (user == null) {
            return 0;
        }

        for (UserDeleteCascadeHandler deleteCascadeHandler : deleteCascadeHandlers) {
            deleteCascadeHandler.beforeDelete(user);
        }
        dao.deleteUserRole(UserIdCodec.toValue(id));

        return dao.deleteById(id);
    }

    @Override
    public List<Role> listUserRoles(UserQuery query) {
        return dao.listUserRoles(UserIdCodec.toValue(query.getId())).stream()
                .map(this::newRole)
                .collect(Collectors.toList());
    }

    private Role newRole(Long id) {
        Role role = new Role();
        role.setId(RoleIdCodec.toDomain(id));
        return role;
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
