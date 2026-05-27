package com.thundax.kuzhambu.system.infra.core.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.tree.TreeNodeMoveType;
import com.thundax.kuzhambu.system.domain.core.codec.MenuIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.Menu;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.MenuId;
import com.thundax.kuzhambu.system.domain.core.repository.MenuRepository;
import com.thundax.kuzhambu.system.infra.core.cache.MenuCacheSupport;
import com.thundax.kuzhambu.system.infra.core.persistence.assembler.MenuPersistenceAssembler;
import com.thundax.kuzhambu.system.infra.core.persistence.dataobject.MenuDO;
import com.thundax.kuzhambu.system.infra.core.persistence.dataobject.MenuRoleDO;
import com.thundax.kuzhambu.system.infra.core.persistence.mapper.MenuMapper;
import com.thundax.kuzhambu.system.infra.core.persistence.mapper.MenuRoleMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class MenuRepositoryImpl implements MenuRepository {

    private static final Long ROOT_ID = 0L;

    private final MenuMapper mapper;
    private final MenuRoleMapper menuRoleMapper;
    private final MenuCacheSupport cacheSupport;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public MenuRepositoryImpl(MenuMapper mapper, MenuRoleMapper menuRoleMapper, MenuCacheSupport cacheSupport) {
        this.mapper = mapper;
        this.menuRoleMapper = menuRoleMapper;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public Menu getById(MenuId id) {
        Menu menu = cacheSupport.getById(id.value());
        if (menu != null) {
            return menu;
        }

        menu = MenuPersistenceAssembler.toDomain(mapper.selectById(id.value()));
        cacheSupport.putById(menu);
        return menu;
    }

    @Override
    public List<Menu> listByIds(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<MenuDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(MenuDO::getId, idList).orderByAsc(MenuDO::getLft);
        List<Menu> menuList = MenuPersistenceAssembler.toDomainList(mapper.selectList(wrapper));
        for (Menu menu : menuList) {
            cacheSupport.putById(menu);
        }
        return menuList;
    }

    @Override
    public List<Menu> list(Long parentId, String visibility, Integer maxRank) {
        return MenuPersistenceAssembler.toDomainList(
                mapper.selectList(buildListWrapper(parentId, visibility, maxRank)));
    }

    @Override
    public PageResult<Menu> page(Long parentId, String visibility, Integer maxRank, int pageNo, int pageSize) {
        IPage<MenuDO> dataObjectPage =
                mapper.selectPage(new Page<>(pageNo, pageSize), buildListWrapper(parentId, visibility, maxRank));
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                MenuPersistenceAssembler.toDomainList(dataObjectPage.getRecords()));
    }

    @Override
    public MenuId insert(Menu entity) {
        MenuDO dataObject = MenuPersistenceAssembler.toObject(entity);
        dataObject.setId(idGenerator.nextId().value());
        Integer newPosition = allocateInsertPosition(dataObject);
        entity.setParentId(MenuIdCodec.toDomain(dataObject.getParentId()));
        dataObject.setLft(newPosition);
        dataObject.setRgt(newPosition + 1);
        moveTreeRgts(newPosition, 2);
        moveTreeLfts(newPosition, 2);
        mapper.insert(dataObject);
        cacheSupport.removeAll();
        return MenuIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(Menu entity) {
        MenuDO oldNode = getTreeNode(MenuIdCodec.toValue(entity.getId()));
        MenuDO dataObject = MenuPersistenceAssembler.toObject(entity);
        normalizeParentId(dataObject);
        entity.setParentId(MenuIdCodec.toDomain(dataObject.getParentId()));
        if (oldNode != null && !Objects.equals(oldNode.getParentId(), dataObject.getParentId())) {
            moveNodeToParent(oldNode, dataObject.getParentId());
        }
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(MenuDO::getParentId, dataObject.getParentId())
                        .set(MenuDO::getName, dataObject.getName())
                        .set(MenuDO::getPerms, dataObject.getPerms())
                        .set(MenuDO::getRanks, dataObject.getRanks())
                        .set(MenuDO::getUrl, dataObject.getUrl())
                        .set(MenuDO::getTarget, dataObject.getTarget())
                        .set(MenuDO::getVisibility, dataObject.getVisibility())
                        .set(MenuDO::getDisplayParams, dataObject.getDisplayParams())
                        .set(MenuDO::getRemarks, dataObject.getRemarks()));
        cacheSupport.removeAll();
        return count;
    }

    @Override
    public int deleteById(MenuId id) {
        MenuDO node = getTreeNode(id.value());
        if (node == null) {
            return 0;
        }
        moveTreeRgts(node.getLft(), -treeSpan(node));
        moveTreeLfts(node.getLft(), -treeSpan(node));
        int count = mapper.deleteById(id.value());
        cacheSupport.removeAll();
        return count;
    }

    @Override
    public void moveTreeNode(Long fromId, Long toId, TreeNodeMoveType moveType) {
        MenuDO fromNode = getTreeNode(fromId);
        MenuDO toNode = getTreeNode(toId);

        int newPosition;
        Long newParentId;
        if (moveType == TreeNodeMoveType.AFTER) {
            newPosition = toNode.getRgt() + 1;
            newParentId = toNode.getParentId();
        } else if (moveType == TreeNodeMoveType.BEFORE) {
            newPosition = toNode.getLft();
            newParentId = toNode.getParentId();
        } else if (moveType == TreeNodeMoveType.INSIDE) {
            newPosition = toNode.getLft() + 1;
            newParentId = toId;
        } else {
            newPosition = toNode.getRgt();
            newParentId = toId;
        }

        moveTreeLfts(newPosition, treeSpan(fromNode));
        moveTreeRgts(newPosition, treeSpan(fromNode));

        fromNode = getTreeNode(fromId);
        int offset = newPosition - fromNode.getLft();
        moveTreeNodes(fromNode.getLft(), fromNode.getRgt(), offset);

        moveTreeLfts(fromNode.getLft(), -treeSpan(fromNode));
        moveTreeRgts(fromNode.getLft(), -treeSpan(fromNode));

        MenuDO parentUpdateDataObject = MenuPersistenceAssembler.toParentUpdateObject(fromId, newParentId);
        mapper.update(
                null,
                buildIdUpdateWrapper(parentUpdateDataObject)
                        .set(MenuDO::getParentId, parentUpdateDataObject.getParentId()));
        cacheSupport.removeAll();
    }

    @Override
    public boolean isChildOf(Long childId, Long parentId) {
        MenuDO child = getTreeNode(childId);
        MenuDO parent = getTreeNode(parentId);
        return child != null && parent != null && child.getLft() > parent.getLft() && child.getRgt() < parent.getRgt();
    }

    @Override
    public int updateVisibility(Menu menu) {
        MenuDO dataObject = MenuPersistenceAssembler.toObject(menu);
        int count = mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(MenuDO::getVisibility, dataObject.getVisibility()));
        cacheSupport.removeById(MenuIdCodec.toValue(menu.getId()));
        return count;
    }

    @Override
    public void deleteMenuRole(Long menuId) {
        LambdaQueryWrapper<MenuRoleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MenuRoleDO::getMenuId, menuId);
        menuRoleMapper.delete(wrapper);
    }

    private Integer allocateInsertPosition(MenuDO node) {
        normalizeParentId(node);
        if (node.getParentId() != null && !ROOT_ID.equals(node.getParentId())) {
            MenuDO parent = getTreeNode(node.getParentId());
            return parent.getRgt();
        }

        Integer maxRgt = getMaxPosition();
        if (maxRgt == null) {
            maxRgt = 0;
        }
        return maxRgt + 1;
    }

    private void moveNodeToParent(MenuDO oldNode, Long parentId) {
        Integer newPosition = getInsertPosition(parentId);
        moveTreeRgts(newPosition, treeSpan(oldNode));
        moveTreeLfts(newPosition, treeSpan(oldNode));

        oldNode = getTreeNode(oldNode.getId());
        int offset = newPosition - oldNode.getLft();
        moveTreeNodes(oldNode.getLft(), oldNode.getRgt(), offset);

        moveTreeRgts(oldNode.getLft(), -treeSpan(oldNode));
        moveTreeLfts(oldNode.getLft(), -treeSpan(oldNode));
    }

    private Integer getInsertPosition(Long parentId) {
        if (parentId != null && !ROOT_ID.equals(parentId)) {
            MenuDO parent = getTreeNode(parentId);
            return parent.getRgt();
        }
        Integer maxRgt = getMaxPosition();
        if (maxRgt == null) {
            maxRgt = 0;
        }
        return maxRgt + 1;
    }

    private MenuDO getTreeNode(Long id) {
        return mapper.selectById(id);
    }

    private Integer getMaxPosition() {
        List<Object> maxValues = mapper.selectObjs(new QueryWrapper<MenuDO>().select("MAX(rgt)"));
        if (maxValues == null || maxValues.isEmpty() || maxValues.get(0) == null) {
            return null;
        }
        return ((Number) maxValues.get(0)).intValue();
    }

    private void moveTreeRgts(Integer from, Integer offset) {
        LambdaUpdateWrapper<MenuDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.ge(MenuDO::getRgt, from).setSql("rgt = rgt + " + offset);
        mapper.update(null, wrapper);
    }

    private void moveTreeLfts(Integer from, Integer offset) {
        LambdaUpdateWrapper<MenuDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.ge(MenuDO::getLft, from).setSql("lft = lft + " + offset);
        mapper.update(null, wrapper);
    }

    private void moveTreeNodes(Integer from, Integer to, Integer offset) {
        LambdaUpdateWrapper<MenuDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.between(MenuDO::getLft, from, to)
                .setSql("lft = lft + " + offset)
                .setSql("rgt = rgt + " + offset);
        mapper.update(null, wrapper);
    }

    private LambdaUpdateWrapper<MenuDO> buildIdUpdateWrapper(MenuDO dataObject) {
        LambdaUpdateWrapper<MenuDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MenuDO::getId, dataObject.getId());
        return wrapper;
    }

    private LambdaQueryWrapper<MenuDO> buildListWrapper(Long parentId, String visibility, Integer maxRank) {
        LambdaQueryWrapper<MenuDO> wrapper = new LambdaQueryWrapper<>();
        if (parentId != null) {
            if (ROOT_ID.equals(parentId)) {
                wrapper.isNull(MenuDO::getParentId);
            } else {
                wrapper.eq(MenuDO::getParentId, parentId);
            }
        }
        if (StringUtils.isNotBlank(visibility)) {
            wrapper.eq(MenuDO::getVisibility, visibility);
        }
        if (maxRank != null) {
            wrapper.le(MenuDO::getRanks, maxRank);
        }
        wrapper.orderByAsc(MenuDO::getLft);
        return wrapper;
    }

    private static void normalizeParentId(MenuDO node) {
        if (node != null && (node.getParentId() == null || ROOT_ID.equals(node.getParentId()))) {
            node.setParentId(null);
        }
    }

    private static int treeSpan(MenuDO node) {
        return node.getRgt() - node.getLft() + 1;
    }
}
