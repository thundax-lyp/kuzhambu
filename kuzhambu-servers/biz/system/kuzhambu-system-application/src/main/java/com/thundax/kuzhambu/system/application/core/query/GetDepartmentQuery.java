package com.thundax.kuzhambu.system.application.core.query;

import com.thundax.kuzhambu.system.domain.core.model.valueobject.DepartmentId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetDepartmentQuery {
    private DepartmentId id;
}
