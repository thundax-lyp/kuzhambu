package com.thundax.kuzhambu.system.infra.core.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.common.core.tree.TreeNodeMoveType;
import com.thundax.kuzhambu.system.application.core.dao.DepartmentDao;
import com.thundax.kuzhambu.system.application.core.entity.Department;
import com.thundax.kuzhambu.system.domain.core.codec.DepartmentIdCodec;
import com.thundax.kuzhambu.system.domain.model.valueobject.DepartmentId;
import com.thundax.kuzhambu.system.infra.core.assembler.DepartmentPersistenceAssembler;
import com.thundax.kuzhambu.system.infra.core.cache.DepartmentCacheSupport;
import com.thundax.kuzhambu.system.infra.core.dataobject.DepartmentDO;
import com.thundax.kuzhambu.system.infra.core.mapper.DepartmentMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class DepartmentDaoImpl implements DepartmentDao {

    private static final Long ROOT_ID = 0L;

    private final DepartmentMapper mapper;
    private final DepartmentCacheSupport cacheSupport;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public DepartmentDaoImpl(DepartmentMapper mapper, DepartmentCacheSupport cacheSupport) {
        this.mapper = mapper;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public Department getById(DepartmentId id) {
        Department department = cacheSupport.getById(id.value());
        if (department != null) {
            return department;
        }

        department = DepartmentPersistenceAssembler.toEntity(mapper.selectById(id.value()));
        cacheSupport.putById(department);
        return department;
    }

    @Override
    public List<Department> listByIds(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<DepartmentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(DepartmentDO::getId, idList).orderByAsc(DepartmentDO::getLft);
        List<Department> departments = DepartmentPersistenceAssembler.toEntityList(mapper.selectList(wrapper));
        for (Department department : departments) {
            cacheSupport.putById(department);
        }
        return departments;
    }

    @Override
    public List<Department> list(Long parentId, String name, String remarks) {
        return DepartmentPersistenceAssembler.toEntityList(
                mapper.selectList(buildListWrapper(parentId, name, remarks)));
    }

    @Override
    public Page<Department> page(Long parentId, String name, String remarks, int pageNo, int pageSize) {
        IPage<DepartmentDO> dataObjectPage =
                mapper.selectPage(new Page<>(pageNo, pageSize), buildListWrapper(parentId, name, remarks));
        Page<Department> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(DepartmentPersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public DepartmentId insert(Department entity) {
        DepartmentDO dataObject = DepartmentPersistenceAssembler.toDataObject(entity);
        dataObject.setId(idGenerator.nextId().value());
        Integer newPosition = allocateInsertPosition(dataObject);
        entity.setParentId(DepartmentIdCodec.toDomain(dataObject.getParentId()));
        dataObject.setLft(newPosition);
        dataObject.setRgt(newPosition + 1);
        moveTreeRgts(newPosition, 2);
        moveTreeLfts(newPosition, 2);
        mapper.insert(dataObject);
        cacheSupport.removeAll();
        return DepartmentIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(Department entity) {
        DepartmentDO oldNode = getTreeNode(DepartmentIdCodec.toValue(entity.getId()));
        DepartmentDO dataObject = DepartmentPersistenceAssembler.toDataObject(entity);
        normalizeParentId(dataObject);
        entity.setParentId(DepartmentIdCodec.toDomain(dataObject.getParentId()));
        if (oldNode != null && !Objects.equals(oldNode.getParentId(), dataObject.getParentId())) {
            moveNodeToParent(oldNode, dataObject.getParentId());
        }
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(DepartmentDO::getParentId, dataObject.getParentId())
                        .set(DepartmentDO::getName, dataObject.getName())
                        .set(DepartmentDO::getShortName, dataObject.getShortName())
                        .set(DepartmentDO::getRemarks, dataObject.getRemarks()));
        cacheSupport.removeAll();
        return count;
    }

    @Override
    public int deleteById(DepartmentId id) {
        DepartmentDO node = getTreeNode(id.value());
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
        DepartmentDO fromNode = getTreeNode(fromId);
        DepartmentDO toNode = getTreeNode(toId);

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

        DepartmentDO parentUpdateDataObject =
                DepartmentPersistenceAssembler.toParentUpdateDataObject(fromId, newParentId);
        mapper.update(
                null,
                buildIdUpdateWrapper(parentUpdateDataObject)
                        .set(DepartmentDO::getParentId, parentUpdateDataObject.getParentId()));
        cacheSupport.removeAll();
    }

    @Override
    public boolean isChildOf(Long childId, Long parentId) {
        DepartmentDO child = getTreeNode(childId);
        DepartmentDO parent = getTreeNode(parentId);
        return child != null && parent != null && child.getLft() > parent.getLft() && child.getRgt() < parent.getRgt();
    }

    private Integer allocateInsertPosition(DepartmentDO node) {
        normalizeParentId(node);
        if (node.getParentId() != null && !ROOT_ID.equals(node.getParentId())) {
            DepartmentDO parent = getTreeNode(node.getParentId());
            return parent.getRgt();
        }

        Integer maxRgt = getMaxPosition();
        if (maxRgt == null) {
            maxRgt = 0;
        }
        return maxRgt + 1;
    }

    private void moveNodeToParent(DepartmentDO oldNode, Long parentId) {
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
            DepartmentDO parent = getTreeNode(parentId);
            return parent.getRgt();
        }
        Integer maxRgt = getMaxPosition();
        if (maxRgt == null) {
            maxRgt = 0;
        }
        return maxRgt + 1;
    }

    private DepartmentDO getTreeNode(Long id) {
        return mapper.selectById(id);
    }

    private Integer getMaxPosition() {
        List<Object> maxValues = mapper.selectObjs(new QueryWrapper<DepartmentDO>().select("MAX(rgt)"));
        if (maxValues == null || maxValues.isEmpty() || maxValues.get(0) == null) {
            return null;
        }
        return ((Number) maxValues.get(0)).intValue();
    }

    private void moveTreeRgts(Integer from, Integer offset) {
        LambdaUpdateWrapper<DepartmentDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.ge(DepartmentDO::getRgt, from).setSql("rgt = rgt + " + offset);
        mapper.update(null, wrapper);
    }

    private void moveTreeLfts(Integer from, Integer offset) {
        LambdaUpdateWrapper<DepartmentDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.ge(DepartmentDO::getLft, from).setSql("lft = lft + " + offset);
        mapper.update(null, wrapper);
    }

    private void moveTreeNodes(Integer from, Integer to, Integer offset) {
        LambdaUpdateWrapper<DepartmentDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.between(DepartmentDO::getLft, from, to)
                .setSql("lft = lft + " + offset)
                .setSql("rgt = rgt + " + offset);
        mapper.update(null, wrapper);
    }

    private LambdaUpdateWrapper<DepartmentDO> buildIdUpdateWrapper(DepartmentDO dataObject) {
        LambdaUpdateWrapper<DepartmentDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(DepartmentDO::getId, dataObject.getId());
        return wrapper;
    }

    private LambdaQueryWrapper<DepartmentDO> buildListWrapper(Long parentId, String name, String remarks) {
        LambdaQueryWrapper<DepartmentDO> wrapper = new LambdaQueryWrapper<>();
        if (parentId != null) {
            if (ROOT_ID.equals(parentId)) {
                wrapper.isNull(DepartmentDO::getParentId);
            } else {
                wrapper.eq(DepartmentDO::getParentId, parentId);
            }
        }
        if (StringUtils.isNotBlank(name)) {
            wrapper.and(nested -> nested.like(DepartmentDO::getName, name).or().like(DepartmentDO::getShortName, name));
        }
        if (StringUtils.isNotBlank(remarks)) {
            wrapper.like(DepartmentDO::getRemarks, remarks);
        }
        wrapper.orderByAsc(DepartmentDO::getLft);
        return wrapper;
    }

    private static void normalizeParentId(DepartmentDO node) {
        if (node != null && (node.getParentId() == null || ROOT_ID.equals(node.getParentId()))) {
            node.setParentId(null);
        }
    }

    private static int treeSpan(DepartmentDO node) {
        return node.getRgt() - node.getLft() + 1;
    }
}
