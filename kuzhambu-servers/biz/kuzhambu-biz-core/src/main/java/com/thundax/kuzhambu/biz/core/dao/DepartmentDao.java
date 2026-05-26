package com.thundax.kuzhambu.biz.core.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.biz.core.entity.Department;
import com.thundax.kuzhambu.biz.core.entity.valueobject.DepartmentId;
import com.thundax.kuzhambu.common.core.tree.TreeNodeMoveType;
import java.util.List;

public interface DepartmentDao {

    Department getById(DepartmentId id);

    List<Department> listByIds(List<Long> idList);

    List<Department> list(Long parentId, String name, String remarks);

    Page<Department> page(Long parentId, String name, String remarks, int pageNo, int pageSize);

    DepartmentId insert(Department department);

    int update(Department department);

    int deleteById(DepartmentId id);

    void moveTreeNode(Long fromId, Long toId, TreeNodeMoveType moveType);

    boolean isChildOf(Long childId, Long parentId);
}
