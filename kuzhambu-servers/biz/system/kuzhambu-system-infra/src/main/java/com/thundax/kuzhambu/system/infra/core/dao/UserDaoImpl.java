package com.thundax.kuzhambu.system.infra.core.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.system.application.core.dao.UserDao;
import com.thundax.kuzhambu.system.application.core.entity.User;
import com.thundax.kuzhambu.system.application.core.entity.enums.UserPrivilege;
import com.thundax.kuzhambu.system.application.core.entity.enums.UserStatus;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.core.valueobject.UserId;
import com.thundax.kuzhambu.system.infra.core.assembler.UserPersistenceAssembler;
import com.thundax.kuzhambu.system.infra.core.cache.RoleCacheSupport;
import com.thundax.kuzhambu.system.infra.core.cache.UserCacheSupport;
import com.thundax.kuzhambu.system.infra.core.dataobject.UserDO;
import com.thundax.kuzhambu.system.infra.core.dataobject.UserRoleDO;
import com.thundax.kuzhambu.system.infra.core.mapper.UserMapper;
import com.thundax.kuzhambu.system.infra.core.mapper.UserRoleMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class UserDaoImpl implements UserDao {

    private static final String DEPARTMENT_TREE_FILTER_SQL =
            "department_id IN (SELECT o.id FROM system_department query_department "
                    + "JOIN system_department o ON o.lft BETWEEN query_department.lft AND query_department.rgt "
                    + "WHERE query_department.id = {0})";
    private static final String ACCOUNT_LOGIN_NAME_FILTER_SQL = "id IN (SELECT principal_id "
            + "FROM system_auth_principal_identity WHERE principal_type = 'USER' AND identity_type = 'USER_ACCOUNT' "
            + "AND identity_value LIKE CONCAT('%',{0},'%'))";
    private final UserMapper mapper;
    private final UserRoleMapper userRoleMapper;
    private final UserCacheSupport cacheSupport;
    private final RoleCacheSupport roleCacheSupport;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public UserDaoImpl(
            UserMapper mapper,
            UserRoleMapper userRoleMapper,
            UserCacheSupport cacheSupport,
            RoleCacheSupport roleCacheSupport) {
        this.mapper = mapper;
        this.userRoleMapper = userRoleMapper;
        this.cacheSupport = cacheSupport;
        this.roleCacheSupport = roleCacheSupport;
    }

    @Override
    public User getById(UserId id) {
        User user = cacheSupport.getById(id.value());
        if (user != null) {
            return user;
        }
        user = UserPersistenceAssembler.toEntity(mapper.selectById(id.value()));
        cacheSupport.putById(user);
        return user;
    }

    @Override
    public List<User> listByIds(List<Long> idList) {
        List<User> userList = new ArrayList<>();
        List<Long> uncachedIdList = new ArrayList<>();
        for (Long id : idList) {
            User user = cacheSupport.getById(id);
            if (user == null) {
                uncachedIdList.add(id);
            } else {
                userList.add(user);
            }
        }
        if (!uncachedIdList.isEmpty()) {
            List<User> uncachedUserList = UserPersistenceAssembler.toEntityList(mapper.selectBatchIds(uncachedIdList));
            for (User user : uncachedUserList) {
                cacheSupport.putById(user);
                userList.add(user);
            }
        }
        return userList;
    }

    @Override
    public List<User> list(
            Long departmentId, String loginName, String name, UserStatus status, UserPrivilege privilege) {
        return UserPersistenceAssembler.toEntityList(
                mapper.selectList(buildListWrapper(departmentId, loginName, name, status, privilege)));
    }

    @Override
    public Page<User> page(
            Long departmentId,
            String loginName,
            String name,
            UserStatus status,
            UserPrivilege privilege,
            int pageNo,
            int pageSize) {
        Page<UserDO> dataObjectPage = mapper.selectPage(
                new Page<>(pageNo, pageSize), buildListWrapper(departmentId, loginName, name, status, privilege));
        Page<User> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(UserPersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public int countByEmail(String email, UserId excludedId) {
        return countByColumn("email", email, excludedId);
    }

    @Override
    public int countByMobile(String mobile, UserId excludedId) {
        return countByColumn("mobile", mobile, excludedId);
    }

    @Override
    public UserId insert(User entity) {
        UserDO dataObject = UserPersistenceAssembler.toDataObject(entity);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        removeUserCaches(dataObject.getId());
        return UserIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(User entity) {
        UserDO dataObject = UserPersistenceAssembler.toDataObject(entity);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(UserDO::getName, dataObject.getName())
                        .set(UserDO::getDepartmentId, dataObject.getDepartmentId())
                        .set(UserDO::getEmail, dataObject.getEmail())
                        .set(UserDO::getMobile, dataObject.getMobile())
                        .set(UserDO::getTel, dataObject.getTel())
                        .set(UserDO::getRanks, dataObject.getRanks())
                        .set(UserDO::getPrivilege, dataObject.getPrivilege())
                        .set(UserDO::getStatus, dataObject.getStatus())
                        .set(UserDO::getRemarks, dataObject.getRemarks()));
        removeUserCaches(UserIdCodec.toValue(entity.getId()));
        return count;
    }

    @Override
    public int deleteById(UserId id) {
        int count = mapper.deleteById(id.value());
        removeUserCaches(id.value());
        roleCacheSupport.removeAll();
        return count;
    }

    @Override
    public int updateStatus(User user) {
        UserDO dataObject = UserPersistenceAssembler.toDataObject(user);
        int count =
                mapper.update(null, buildIdUpdateWrapper(dataObject).set(UserDO::getStatus, dataObject.getStatus()));
        removeUserCaches(UserIdCodec.toValue(user.getId()));
        return count;
    }

    @Override
    public List<Long> listUserRoles(Long userId) {
        List<Long> roleIds = cacheSupport.getUserRoleIds(userId);
        if (roleIds == null) {
            LambdaQueryWrapper<UserRoleDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserRoleDO::getUserId, userId);
            roleIds = userRoleMapper.selectList(wrapper).stream()
                    .map(UserRoleDO::getRoleId)
                    .collect(Collectors.toList());
            cacheSupport.putUserRoleIds(userId, roleIds);
        }
        return roleIds;
    }

    @Override
    public void deleteUserRole(Long userId) {
        LambdaQueryWrapper<UserRoleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRoleDO::getUserId, userId);
        userRoleMapper.delete(wrapper);
        removeUserCaches(userId);
    }

    @Override
    public void insertUserRole(Long userId, List<Long> roleIdList) {
        for (Long roleId : roleIdList) {
            userRoleMapper.insert(UserPersistenceAssembler.toUserRoleDataObject(userId, roleId));
        }
        removeUserCaches(userId);
    }

    private LambdaUpdateWrapper<UserDO> buildIdUpdateWrapper(UserDO dataObject) {
        LambdaUpdateWrapper<UserDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserDO::getId, dataObject.getId());
        return wrapper;
    }

    private QueryWrapper<UserDO> buildListWrapper(
            Long departmentId, String loginName, String name, UserStatus status, UserPrivilege privilege) {
        QueryWrapper<UserDO> wrapper = new QueryWrapper<>();
        if (departmentId != null) {
            wrapper.apply(DEPARTMENT_TREE_FILTER_SQL, departmentId);
        }
        if (StringUtils.isNotBlank(loginName)) {
            wrapper.apply(ACCOUNT_LOGIN_NAME_FILTER_SQL, loginName);
        }
        if (StringUtils.isNotBlank(name)) {
            wrapper.like("name", name);
        }
        if (status != null) {
            wrapper.eq("status", status.value());
        }
        if (privilege != null) {
            wrapper.eq("privilege", privilege.value());
        }
        wrapper.orderByAsc("id");
        return wrapper;
    }

    private int countByColumn(String column, String value, UserId excludedId) {
        if (StringUtils.isBlank(value)) {
            return 0;
        }
        QueryWrapper<UserDO> wrapper = new QueryWrapper<>();
        wrapper.eq(column, value);
        if (excludedId != null) {
            wrapper.ne("id", UserIdCodec.toValue(excludedId));
        }
        Long count = mapper.selectCount(wrapper);
        return count == null ? 0 : count.intValue();
    }

    private void removeUserCaches(Long userId) {
        cacheSupport.removeById(userId);
        cacheSupport.removeUserRoleIds(userId);
    }
}
