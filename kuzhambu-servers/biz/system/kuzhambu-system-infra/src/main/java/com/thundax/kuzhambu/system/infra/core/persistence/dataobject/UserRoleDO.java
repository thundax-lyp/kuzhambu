package com.thundax.kuzhambu.system.infra.core.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("system_user_role")
public class UserRoleDO {
    // Database primary key is (user_id, role_id); MyBatis-Plus BaseMapper requires one TableId.
    @TableId(type = IdType.INPUT)
    private Long userId;

    private Long roleId;
}
