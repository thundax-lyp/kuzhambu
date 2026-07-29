package com.thundax.kuzhambu.system.application.core.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.application.core.command.ChangeDepartmentInfoCommand;
import com.thundax.kuzhambu.system.application.core.command.CreateDepartmentCommand;
import com.thundax.kuzhambu.system.application.core.command.MoveDepartmentCommand;
import com.thundax.kuzhambu.system.application.core.command.RemoveDepartmentCommand;
import com.thundax.kuzhambu.system.application.core.query.DepartmentQuery;
import com.thundax.kuzhambu.system.application.core.query.GetDepartmentQuery;
import com.thundax.kuzhambu.system.domain.core.model.entity.Department;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.DepartmentId;
import java.util.List;

public interface DepartmentApplicationService {

    Department get(GetDepartmentQuery query);

    List<Department> list(DepartmentQuery query);

    PageResult<Department> page(DepartmentQuery query, PageQuery page);

    DepartmentId create(CreateDepartmentCommand command);

    void changeInfo(ChangeDepartmentInfoCommand command);

    int remove(RemoveDepartmentCommand command);

    void move(MoveDepartmentCommand command);

    boolean existsChildRelation(DepartmentQuery query);
}
