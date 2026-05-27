package com.thundax.kuzhambu.system.domain.core.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.tree.TreeNodeMoveType;
import com.thundax.kuzhambu.system.domain.core.model.entity.Department;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.DepartmentId;
import java.util.List;

public interface DepartmentRepository {

    Department getById(DepartmentId id);

    List<Department> listByIds(List<Long> idList);

    List<Department> list(Long parentId, String name, String remarks);

    PageResult<Department> page(Long parentId, String name, String remarks, int pageNo, int pageSize);

    DepartmentId insert(Department department);

    int update(Department department);

    int deleteById(DepartmentId id);

    void moveTreeNode(Long fromId, Long toId, TreeNodeMoveType moveType);

    boolean isChildOf(Long childId, Long parentId);
}
