package com.thundax.kuzhambu.system.infra.core.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.system.domain.core.codec.MenuIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.RoleIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.Role;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.MenuId;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.RoleId;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import com.thundax.kuzhambu.system.domain.core.repository.RoleRepository;
import com.thundax.kuzhambu.system.infra.core.cache.RoleCacheSupport;
import com.thundax.kuzhambu.system.infra.core.cache.UserCacheSupport;
import com.thundax.kuzhambu.system.infra.core.persistence.assembler.RolePersistenceAssembler;
import com.thundax.kuzhambu.system.infra.core.persistence.dataobject.MenuRoleDO;
import com.thundax.kuzhambu.system.infra.core.persistence.dataobject.RoleDO;
import com.thundax.kuzhambu.system.infra.core.persistence.dataobject.UserRoleDO;
import com.thundax.kuzhambu.system.infra.core.persistence.mapper.MenuRoleMapper;
import com.thundax.kuzhambu.system.infra.core.persistence.mapper.RoleMapper;
import com.thundax.kuzhambu.system.infra.core.persistence.mapper.UserRoleMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class RoleRepositoryImpl implements RoleRepository {

    private final RoleMapper mapper;
    private final MenuRoleMapper menuRoleMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleCacheSupport cacheSupport;
    private final UserCacheSupport userCacheSupport;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public RoleRepositoryImpl(
            RoleMapper mapper,
            MenuRoleMapper menuRoleMapper,
            UserRoleMapper userRoleMapper,
            RoleCacheSupport cacheSupport,
            UserCacheSupport userCacheSupport) {
        this.mapper = mapper;
        this.menuRoleMapper = menuRoleMapper;
        this.userRoleMapper = userRoleMapper;
        this.cacheSupport = cacheSupport;
        this.userCacheSupport = userCacheSupport;
    }

    @Override
    public Role getById(RoleId id) {
        Role role = cacheSupport.getById(id.value());
        if (role != null) {
            return role;
        }
        role = RolePersistenceAssembler.toDomain(mapper.selectById(id.value()));
        cacheSupport.putById(role);
        return role;
    }

    @Override
    public List<Role> listByIds(List<RoleId> idList) {
        List<Role> roleList = new ArrayList<>();
        List<Long> uncachedIdList = new ArrayList<>();
        for (RoleId id : idList) {
            Long value = RoleIdCodec.toValue(id);
            Role role = cacheSupport.getById(value);
            if (role == null) {
                uncachedIdList.add(value);
            } else {
                roleList.add(role);
            }
        }
        if (!uncachedIdList.isEmpty()) {
            List<Role> uncachedRoleList = RolePersistenceAssembler.toDomainList(mapper.selectBatchIds(uncachedIdList));
            for (Role role : uncachedRoleList) {
                cacheSupport.putById(role);
                roleList.add(role);
            }
        }
        return roleList;
    }

    @Override
    public List<Role> list(String status) {
        return RolePersistenceAssembler.toDomainList(mapper.selectList(buildListWrapper(status)));
    }

    @Override
    public int maxPriority() {
        QueryWrapper<RoleDO> wrapper = new QueryWrapper<>();
        Object max = mapper.selectObjs(wrapper.select("max(priority)")).stream()
                .findFirst()
                .orElse(null);
        if (max == null) {
            return 0;
        }
        if (max instanceof Number) {
            return ((Number) max).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(max));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    @Override
    public List<Role> list(SortDirection sortDirection) {
        return RolePersistenceAssembler.toDomainList(mapper.selectList(buildListWrapper(sortDirection)));
    }

    @Override
    public PageResult<Role> page(String status, int pageNo, int pageSize) {
        Page<RoleDO> dataObjectPage = mapper.selectPage(new Page<>(pageNo, pageSize), buildListWrapper(status));
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                RolePersistenceAssembler.toDomainList(dataObjectPage.getRecords()));
    }

    @Override
    public RoleId insert(Role entity) {
        RoleDO dataObject = RolePersistenceAssembler.toObject(entity);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        cacheSupport.removeById(dataObject.getId());
        return RoleIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(Role entity) {
        RoleDO dataObject = RolePersistenceAssembler.toObject(entity);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(RoleDO::getName, dataObject.getName())
                        .set(RoleDO::getPrivilege, dataObject.getPrivilege())
                        .set(RoleDO::getStatus, dataObject.getStatus())
                        .set(RoleDO::getRemarks, dataObject.getRemarks()));
        cacheSupport.removeById(RoleIdCodec.toValue(entity.getId()));
        return count;
    }

    @Override
    public int updatePriority(Role entity) {
        RoleDO dataObject = RolePersistenceAssembler.toObject(entity);
        int count = mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(RoleDO::getPriority, dataObject.getPriority()));
        cacheSupport.removeById(RoleIdCodec.toValue(entity.getId()));
        return count;
    }

    @Override
    public int deleteById(RoleId id) {
        int count = mapper.deleteById(id.value());
        removeRoleCaches(id.value());
        return count;
    }

    @Override
    public int updateStatus(Role role) {
        RoleDO dataObject = RolePersistenceAssembler.toObject(role);
        int count =
                mapper.update(null, buildIdUpdateWrapper(dataObject).set(RoleDO::getStatus, dataObject.getStatus()));
        cacheSupport.removeById(RoleIdCodec.toValue(role.getId()));
        return count;
    }

    @Override
    public List<MenuId> listRoleMenus(RoleId roleId) {
        Long roleIdValue = RoleIdCodec.toValue(roleId);
        List<Long> menuIds = cacheSupport.getRoleMenuIds(roleIdValue);
        if (menuIds == null) {
            LambdaQueryWrapper<MenuRoleDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MenuRoleDO::getRoleId, roleIdValue);
            menuIds = menuRoleMapper.selectList(wrapper).stream()
                    .map(MenuRoleDO::getMenuId)
                    .collect(Collectors.toList());
            cacheSupport.putRoleMenuIds(roleIdValue, menuIds);
        }
        return MenuIdCodec.toDomains(menuIds);
    }

    @Override
    public void deleteRoleMenu(RoleId roleId) {
        Long roleIdValue = RoleIdCodec.toValue(roleId);
        LambdaQueryWrapper<MenuRoleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MenuRoleDO::getRoleId, roleIdValue);
        menuRoleMapper.delete(wrapper);
        removeRoleCaches(roleIdValue);
    }

    @Override
    public void insertRoleMenu(RoleId roleId, List<MenuId> menuIdList) {
        Long roleIdValue = RoleIdCodec.toValue(roleId);
        for (Long menuId : MenuIdCodec.toValues(menuIdList)) {
            menuRoleMapper.insert(RolePersistenceAssembler.toMenuRoleObject(roleIdValue, menuId));
        }
        removeRoleCaches(roleIdValue);
    }

    @Override
    public List<UserId> listRoleUsers(RoleId roleId) {
        Long roleIdValue = RoleIdCodec.toValue(roleId);
        List<Long> userIds = cacheSupport.getRoleUserIds(roleIdValue);
        if (userIds == null) {
            LambdaQueryWrapper<UserRoleDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserRoleDO::getRoleId, roleIdValue);
            userIds = userRoleMapper.selectList(wrapper).stream()
                    .map(UserRoleDO::getUserId)
                    .collect(Collectors.toList());
            cacheSupport.putRoleUserIds(roleIdValue, userIds);
        }
        return UserIdCodec.toDomains(userIds);
    }

    @Override
    public void deleteRoleUser(RoleId roleId) {
        Long roleIdValue = RoleIdCodec.toValue(roleId);
        LambdaQueryWrapper<UserRoleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRoleDO::getRoleId, roleIdValue);
        userRoleMapper.delete(wrapper);
        removeRoleCaches(roleIdValue);
    }

    @Override
    public void insertRoleUser(RoleId roleId, List<UserId> userIdList) {
        Long roleIdValue = RoleIdCodec.toValue(roleId);
        for (Long userId : UserIdCodec.toValues(userIdList)) {
            userRoleMapper.insert(RolePersistenceAssembler.toUserRoleObject(userId, roleIdValue));
        }
        removeRoleCaches(roleIdValue);
    }

    private LambdaUpdateWrapper<RoleDO> buildIdUpdateWrapper(RoleDO dataObject) {
        LambdaUpdateWrapper<RoleDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(RoleDO::getId, dataObject.getId());
        return wrapper;
    }

    private LambdaQueryWrapper<RoleDO> buildListWrapper(String status) {
        LambdaQueryWrapper<RoleDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(RoleDO::getStatus, status);
        }
        wrapper.orderByAsc(RoleDO::getPriority, RoleDO::getId);
        return wrapper;
    }

    private LambdaQueryWrapper<RoleDO> buildListWrapper(SortDirection sortDirection) {
        LambdaQueryWrapper<RoleDO> wrapper = new LambdaQueryWrapper<>();
        if (SortDirection.DESC == sortDirection) {
            wrapper.orderByDesc(RoleDO::getPriority);
        } else {
            wrapper.orderByAsc(RoleDO::getPriority);
        }
        wrapper.orderByAsc(RoleDO::getId);
        return wrapper;
    }

    private void removeRoleCaches(Long roleId) {
        cacheSupport.removeById(roleId);
        cacheSupport.removeRoleUserIds(roleId);
        cacheSupport.removeRoleMenuIds(roleId);
        userCacheSupport.removeAll();
    }
}
