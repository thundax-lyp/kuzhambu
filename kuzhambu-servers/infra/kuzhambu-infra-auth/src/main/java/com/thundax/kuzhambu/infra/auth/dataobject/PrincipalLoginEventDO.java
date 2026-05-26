package com.thundax.kuzhambu.infra.auth.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("auth_principal_login_event")
public class PrincipalLoginEventDO {

    @TableId(type = IdType.INPUT)
    private String id;

    private String principalType;

    private Long principalId;

    private String clientId;

    private String eventType;

    private String authenticationMethod;

    private String identityType;

    private Date occurredAt;

    private String ip;

    private String userAgent;

    private String reason;
}
