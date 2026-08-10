package com.thundax.kuzhambu.system.application.core.service.impl;

import com.thundax.kuzhambu.common.audit.annotation.AuditLog;
import com.thundax.kuzhambu.common.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.application.core.command.ChangeDepartmentInfoCommand;
import com.thundax.kuzhambu.system.application.core.command.CreateDepartmentCommand;
import com.thundax.kuzhambu.system.application.core.command.MoveDepartmentCommand;
import com.thundax.kuzhambu.system.application.core.command.RemoveDepartmentCommand;
import com.thundax.kuzhambu.system.application.core.query.DepartmentQuery;
import com.thundax.kuzhambu.system.application.core.query.GetDepartmentQuery;
import com.thundax.kuzhambu.system.application.core.service.DepartmentManagementApplicationService;
import com.thundax.kuzhambu.system.domain.core.model.entity.Department;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.DepartmentId;
import com.thundax.kuzhambu.system.domain.core.repository.DepartmentRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class DepartmentManagementApplicationServiceImpl implements DepartmentManagementApplicationService {

    private final DepartmentRepository dao;

    public DepartmentManagementApplicationServiceImpl(DepartmentRepository dao) {
        this.dao = dao;
    }

    public Department get(GetDepartmentQuery query) {
        DepartmentId id = query == null ? null : query.id();
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    public List<Department> list(DepartmentQuery query) {
        return dao.list(
                query == null ? null : query.parentId(),
                query == null ? null : query.name(),
                query == null ? null : query.remarks());
    }

    public PageResult<Department> page(DepartmentQuery query, PageQuery page) {
        return dao.page(
                query == null ? null : query.parentId(),
                query == null ? null : query.name(),
                query == null ? null : query.remarks(),
                page.getPageNo(),
                page.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(type = "Department", id = "", action = AuditAction.CREATE, summary = "创建部门")
    public DepartmentId create(CreateDepartmentCommand command) {
        Department entity = toDepartment(command);
        entity.setId(dao.insert(entity));
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(type = "Department", id = "#command.id().value()", action = AuditAction.UPDATE, summary = "更新部门")
    public void changeInfo(ChangeDepartmentInfoCommand command) {
        Department entity = toDepartment(command);
        dao.update(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    @AuditLog(
            type = "Department",
            id = "#command.id() == null ? null : #command.id().value()",
            action = AuditAction.DELETE,
            summary = "删除部门")
    public int remove(RemoveDepartmentCommand command) {
        DepartmentId id = command == null ? null : command.id();
        Department bean = this.get(new GetDepartmentQuery(id));
        if (bean == null) {
            return 0;
        }

        int count = dao.deleteById(bean.getId());

        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(type = "Department", id = "#command.fromId().value()", action = AuditAction.UPDATE, summary = "移动部门")
    public void move(MoveDepartmentCommand command) {
        dao.moveTreeNode(command.fromId(), command.toId(), command.moveType());
    }

    @Override
    public boolean existsChildRelation(DepartmentQuery query) {
        return query != null
                && query.childId() != null
                && query.ancestorId() != null
                && dao.isChildOf(query.childId(), query.ancestorId());
    }

    private Department toDepartment(CreateDepartmentCommand command) {
        Department department = new Department();
        department.setId(command.id());
        department.setParentId(command.parentId());
        department.setName(command.name());
        department.setShortName(command.shortName());
        department.setRemarks(command.remarks());
        return department;
    }

    private Department toDepartment(ChangeDepartmentInfoCommand command) {
        Department department = new Department();
        department.setId(command.id());
        department.setParentId(command.parentId());
        department.setName(command.name());
        department.setShortName(command.shortName());
        department.setRemarks(command.remarks());
        return department;
    }
}
