package com.thundax.kuzhambu.system.application.core.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.application.core.dao.DepartmentDao;
import com.thundax.kuzhambu.system.application.core.entity.Department;
import com.thundax.kuzhambu.system.application.core.service.DepartmentService;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeDepartmentInfoCommand;
import com.thundax.kuzhambu.system.application.core.service.command.CreateDepartmentCommand;
import com.thundax.kuzhambu.system.application.core.service.command.MoveDepartmentCommand;
import com.thundax.kuzhambu.system.application.core.service.query.DepartmentQuery;
import com.thundax.kuzhambu.system.domain.core.codec.DepartmentIdCodec;
import com.thundax.kuzhambu.system.domain.model.valueobject.DepartmentId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentDao dao;

    public DepartmentServiceImpl(DepartmentDao dao) {
        this.dao = dao;
    }

    public Department get(DepartmentId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    public List<Department> list(DepartmentQuery query) {
        return dao.list(
                query == null ? null : DepartmentIdCodec.toValue(query.getParentId()),
                query == null ? null : query.getName(),
                query == null ? null : query.getRemarks());
    }

    public PageResult<Department> page(DepartmentQuery query, PageQuery page) {
        IPage<Department> dataPage = dao.page(
                query == null ? null : DepartmentIdCodec.toValue(query.getParentId()),
                query == null ? null : query.getName(),
                query == null ? null : query.getRemarks(),
                page.getPageNo(),
                page.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepartmentId create(CreateDepartmentCommand command) {
        Department entity = toDepartment(command);
        entity.setId(dao.insert(entity));
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeInfo(ChangeDepartmentInfoCommand command) {
        Department entity = toDepartment(command);
        dao.update(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public int remove(DepartmentId id) {
        Department bean = this.get(id);
        if (bean == null) {
            return 0;
        }

        int count = dao.deleteById(bean.getId());

        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void move(MoveDepartmentCommand command) {
        dao.moveTreeNode(
                DepartmentIdCodec.toValue(command.getFromId()),
                DepartmentIdCodec.toValue(command.getToId()),
                command.getMoveType());
    }

    @Override
    public boolean existsChildRelation(DepartmentQuery query) {
        return query != null
                && query.getChildId() != null
                && query.getAncestorId() != null
                && dao.isChildOf(
                        DepartmentIdCodec.toValue(query.getChildId()),
                        DepartmentIdCodec.toValue(query.getAncestorId()));
    }

    private Department toDepartment(CreateDepartmentCommand command) {
        Department department = new Department();
        department.setId(command.getId());
        department.setParentId(command.getParentId());
        department.setName(command.getName());
        department.setShortName(command.getShortName());
        department.setRemarks(command.getRemarks());
        return department;
    }

    private Department toDepartment(ChangeDepartmentInfoCommand command) {
        Department department = new Department();
        department.setId(command.getId());
        department.setParentId(command.getParentId());
        department.setName(command.getName());
        department.setShortName(command.getShortName());
        department.setRemarks(command.getRemarks());
        return department;
    }
}
