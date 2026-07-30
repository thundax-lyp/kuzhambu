package com.thundax.kuzhambu.system.infra.auth.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("system_auth_principal_login_event")
public class PrincipalLoginEventDO {
    @TableId(type = IdType.INPUT)
    private String id;

    private String principalType;
    private Long principalId;
    private String clientId;
    private String eventType;
    private String authenticationMethod;
    private String identityType;
    private Instant occurredAt;
    private String ip;
    private String userAgent;
    private String reason;
}
