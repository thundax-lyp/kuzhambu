package com.thundax.kuzhambu.infra.core.persistence.dataobject;

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
@TableName("sys_role_menu")
public class MenuRoleDO {

    // Database primary key is (role_id, menu_id); MyBatis-Plus BaseMapper requires one TableId.
    @TableId(type = IdType.INPUT)
    private Long roleId;

    private Long menuId;
}
