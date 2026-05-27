package com.thundax.kuzhambu.system.infra.core.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.thundax.kuzhambu.common.mybatis.typehandler.DefaultEncryptTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "system_user", autoResultMap = true)
public class UserDO {
    @TableId(type = IdType.INPUT)
    private Long id;

    private Long departmentId;

    @TableField(typeHandler = DefaultEncryptTypeHandler.class)
    private String email;

    @TableField(typeHandler = DefaultEncryptTypeHandler.class)
    private String mobile;

    private String tel;
    private String name;
    private Integer ranks;
    private String privilege;
    private String status;
    private String remarks;
}
