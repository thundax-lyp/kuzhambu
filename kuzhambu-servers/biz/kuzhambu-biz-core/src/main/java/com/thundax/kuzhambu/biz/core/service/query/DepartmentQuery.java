package com.thundax.kuzhambu.biz.core.service.query;

import com.thundax.kuzhambu.biz.core.entity.valueobject.DepartmentId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentQuery {
    private DepartmentId childId;
    private DepartmentId ancestorId;
    private DepartmentId parentId;
    private String name;
    private String remarks;
}
