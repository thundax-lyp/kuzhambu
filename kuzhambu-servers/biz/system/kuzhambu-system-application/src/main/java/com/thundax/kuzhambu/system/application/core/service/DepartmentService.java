package com.thundax.kuzhambu.system.application.core.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.application.core.entity.Department;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.DepartmentId;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeDepartmentInfoCommand;
import com.thundax.kuzhambu.system.application.core.service.command.CreateDepartmentCommand;
import com.thundax.kuzhambu.system.application.core.service.command.MoveDepartmentCommand;
import com.thundax.kuzhambu.system.application.core.service.query.DepartmentQuery;
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
