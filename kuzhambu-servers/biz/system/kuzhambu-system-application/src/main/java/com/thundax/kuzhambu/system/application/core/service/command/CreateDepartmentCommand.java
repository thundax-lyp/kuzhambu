package com.thundax.kuzhambu.system.application.core.service.command;

import com.thundax.kuzhambu.system.domain.core.model.valueobject.DepartmentId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateDepartmentCommand {
    private DepartmentId id;
    private DepartmentId parentId;
    private String name;
    private String shortName;
    private String remarks;
}
