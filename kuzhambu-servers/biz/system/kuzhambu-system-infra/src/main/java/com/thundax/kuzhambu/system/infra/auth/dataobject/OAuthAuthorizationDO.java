package com.thundax.kuzhambu.system.infra.auth.dataobject;

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
@TableName("system_auth_oauth_authorization")
public class OAuthAuthorizationDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String authorizationCode;

    private String clientId;

    private String principalType;

    private Long principalId;

    private String redirectUri;

    private String scopes;

    private String state;

    private String codeChallenge;

    private String codeChallengeMethod;

    private Date issuedAt;

    private Date expireAt;

    private boolean used;
}
