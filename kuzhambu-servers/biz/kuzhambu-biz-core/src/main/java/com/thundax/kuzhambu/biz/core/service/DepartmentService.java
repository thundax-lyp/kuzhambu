package com.thundax.kuzhambu.biz.core.service;

import com.thundax.kuzhambu.biz.core.entity.Department;
import com.thundax.kuzhambu.biz.core.entity.valueobject.DepartmentId;
import com.thundax.kuzhambu.biz.core.service.command.ChangeDepartmentInfoCommand;
import com.thundax.kuzhambu.biz.core.service.command.CreateDepartmentCommand;
import com.thundax.kuzhambu.biz.core.service.command.MoveDepartmentCommand;
import com.thundax.kuzhambu.biz.core.service.query.DepartmentQuery;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.List;

public interface DepartmentService {

    Department get(DepartmentId id);

    List<Department> list(DepartmentQuery query);

    PageResult<Department> page(DepartmentQuery query, PageQuery page);

    DepartmentId create(CreateDepartmentCommand command);

    void changeInfo(ChangeDepartmentInfoCommand command);

    int remove(DepartmentId id);

    void move(MoveDepartmentCommand command);

    boolean existsChildRelation(DepartmentQuery query);
}
