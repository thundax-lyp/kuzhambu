package com.thundax.kuzhambu.system.infra.auth.dataobject;

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
@TableName("system_auth_oauth_client")
public class OAuthClientDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String clientId;

    private String clientSecretHash;

    private String clientName;

    private String clientType;

    private String grantTypes;

    private String scopes;

    private String redirectUris;

    private long accessTokenTtlSeconds;

    private long refreshTokenTtlSeconds;

    private String status;

    private String contact;

    private String remark;
}
