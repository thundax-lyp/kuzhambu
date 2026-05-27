package com.thundax.kuzhambu.system.infra.auth.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("system_auth_principal_credential")
public class PrincipalCredentialDO {
    @TableId(type = IdType.INPUT)
    private Long id;

    private String principalType;
    private Long principalId;
    private Long identityId;
    private String credentialType;
    private String credentialValue;
    private String status;
    private Boolean needChangePassword;
    private Integer failedCount;
    private Integer failedLimit;
    private Date lockedUntil;
    private Date expiresAt;
    private Date lastVerifiedAt;
}
