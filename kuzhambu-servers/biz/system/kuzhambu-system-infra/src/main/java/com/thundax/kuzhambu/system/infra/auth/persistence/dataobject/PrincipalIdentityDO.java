package com.thundax.kuzhambu.system.infra.auth.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("system_auth_principal_identity")
public class PrincipalIdentityDO {
    @TableId(type = IdType.INPUT)
    private Long id;

    private String principalType;
    private Long principalId;
    private String identityType;
    private String identityValue;
    private String status;
}
