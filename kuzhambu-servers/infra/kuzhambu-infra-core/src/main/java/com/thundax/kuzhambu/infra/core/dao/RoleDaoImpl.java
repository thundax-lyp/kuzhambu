package com.thundax.kuzhambu.infra.core.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.biz.core.dao.RoleDao;
import com.thundax.kuzhambu.biz.core.entity.Role;
import com.thundax.kuzhambu.biz.core.entity.valueobject.RoleId;
import com.thundax.kuzhambu.biz.core.entity.valueobject.RoleIdCodec;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.infra.core.assembler.RolePersistenceAssembler;
import com.thundax.kuzhambu.infra.core.cache.RoleCacheSupport;
import com.thundax.kuzhambu.infra.core.cache.UserCacheSupport;
import com.thundax.kuzhambu.infra.core.dataobject.MenuRoleDO;
import com.thundax.kuzhambu.infra.core.dataobject.RoleDO;
import com.thundax.kuzhambu.infra.core.dataobject.UserRoleDO;
import com.thundax.kuzhambu.infra.core.mapper.MenuRoleMapper;
import com.thundax.kuzhambu.infra.core.mapper.RoleMapper;
import com.thundax.kuzhambu.infra.core.mapper.UserRoleMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class RoleDaoImpl implements RoleDao {

    private final RoleMapper mapper;
    private final MenuRoleMapper menuRoleMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleCacheSupport cacheSupport;
    private final UserCacheSupport userCacheSupport;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public RoleDaoImpl(
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
        role = RolePersistenceAssembler.toEntity(mapper.selectById(id.value()));
        cacheSupport.putById(role);
        return role;
    }

    @Override
    public List<Role> listByIds(List<Long> idList) {
        List<Role> roleList = new ArrayList<>();
        List<Long> uncachedIdList = new ArrayList<>();
        for (Long id : idList) {
            Role role = cacheSupport.getById(id);
            if (role == null) {
                uncachedIdList.add(id);
            } else {
                roleList.add(role);
            }
        }
        if (!uncachedIdList.isEmpty()) {
            List<Role> uncachedRoleList = RolePersistenceAssembler.toEntityList(mapper.selectBatchIds(uncachedIdList));
            for (Role role : uncachedRoleList) {
                cacheSupport.putById(role);
                roleList.add(role);
            }
        }
        return roleList;
    }

    @Override
    public List<Role> list(String status) {
        return RolePersistenceAssembler.toEntityList(mapper.selectList(buildListWrapper(status)));
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
        return RolePersistenceAssembler.toEntityList(mapper.selectList(buildListWrapper(sortDirection)));
    }

    @Override
    public Page<Role> page(String status, int pageNo, int pageSize) {
        Page<RoleDO> dataObjectPage = mapper.selectPage(new Page<>(pageNo, pageSize), buildListWrapper(status));
        Page<Role> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(RolePersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public RoleId insert(Role entity) {
        RoleDO dataObject = RolePersistenceAssembler.toDataObject(entity);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        cacheSupport.removeById(dataObject.getId());
        return RoleIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(Role entity) {
        RoleDO dataObject = RolePersistenceAssembler.toDataObject(entity);
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
        RoleDO dataObject = RolePersistenceAssembler.toDataObject(entity);
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
        RoleDO dataObject = RolePersistenceAssembler.toDataObject(role);
        int count =
                mapper.update(null, buildIdUpdateWrapper(dataObject).set(RoleDO::getStatus, dataObject.getStatus()));
        cacheSupport.removeById(RoleIdCodec.toValue(role.getId()));
        return count;
    }

    @Override
    public List<Long> listRoleMenus(Long roleId) {
        List<Long> menuIds = cacheSupport.getRoleMenuIds(roleId);
        if (menuIds == null) {
            LambdaQueryWrapper<MenuRoleDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MenuRoleDO::getRoleId, roleId);
            menuIds = menuRoleMapper.selectList(wrapper).stream()
                    .map(MenuRoleDO::getMenuId)
                    .collect(Collectors.toList());
            cacheSupport.putRoleMenuIds(roleId, menuIds);
        }
        return menuIds;
    }

    @Override
    public void deleteRoleMenu(Long roleId) {
        LambdaQueryWrapper<MenuRoleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MenuRoleDO::getRoleId, roleId);
        menuRoleMapper.delete(wrapper);
        removeRoleCaches(roleId);
    }

    @Override
    public void insertRoleMenu(Long roleId, List<Long> menuIdList) {
        for (Long menuId : menuIdList) {
            menuRoleMapper.insert(RolePersistenceAssembler.toMenuRoleDataObject(roleId, menuId));
        }
        removeRoleCaches(roleId);
    }

    @Override
    public List<Long> listRoleUsers(Long roleId) {
        List<Long> userIds = cacheSupport.getRoleUserIds(roleId);
        if (userIds == null) {
            LambdaQueryWrapper<UserRoleDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserRoleDO::getRoleId, roleId);
            userIds = userRoleMapper.selectList(wrapper).stream()
                    .map(UserRoleDO::getUserId)
                    .collect(Collectors.toList());
            cacheSupport.putRoleUserIds(roleId, userIds);
        }
        return userIds;
    }

    @Override
    public void deleteRoleUser(Long roleId) {
        LambdaQueryWrapper<UserRoleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRoleDO::getRoleId, roleId);
        userRoleMapper.delete(wrapper);
        removeRoleCaches(roleId);
    }

    @Override
    public void insertRoleUser(Long roleId, List<Long> userIdList) {
        for (Long userId : userIdList) {
            userRoleMapper.insert(RolePersistenceAssembler.toUserRoleDataObject(userId, roleId));
        }
        removeRoleCaches(roleId);
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
