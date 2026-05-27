package com.thundax.kuzhambu.system.infra.core.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("system_department")
public class DepartmentDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long parentId;

    private Integer lft;

    private Integer rgt;

    private String name;

    private String shortName;

    private String remarks;
}
