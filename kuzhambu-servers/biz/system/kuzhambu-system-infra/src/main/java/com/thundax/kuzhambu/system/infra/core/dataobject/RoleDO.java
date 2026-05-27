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
@TableName("system_role")
public class RoleDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String name;

    private String privilege;

    private String status;

    private Integer priority;

    private String remarks;
}
