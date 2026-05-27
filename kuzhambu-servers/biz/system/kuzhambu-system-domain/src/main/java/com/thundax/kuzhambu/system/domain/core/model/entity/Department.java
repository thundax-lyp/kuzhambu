package com.thundax.kuzhambu.system.domain.core.model.entity;

import com.thundax.kuzhambu.system.domain.core.model.valueobject.DepartmentId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Department {
    public static final String ROOT_ID = "ROOT";

    private DepartmentId id;

    private DepartmentId parentId;

    private String name;
    private String shortName;
    private String remarks;

    /**
     * 获取显示名称。如果存在简称，则显示简称；如果没有简称，则显示全名
     *
     * @return 显示名称
     */
    public String getDisplayName() {
        if (this.getShortName() != null && !this.getShortName().isBlank()) {
            return this.getShortName();
        }
        return this.getName();
    }
}
