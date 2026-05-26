package com.thundax.kuzhambu.biz.core.service.command;

import com.thundax.kuzhambu.biz.core.entity.valueobject.DepartmentId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeDepartmentInfoCommand {
    private DepartmentId id;
    private DepartmentId parentId;
    private String name;
    private String shortName;
    private String remarks;
}
