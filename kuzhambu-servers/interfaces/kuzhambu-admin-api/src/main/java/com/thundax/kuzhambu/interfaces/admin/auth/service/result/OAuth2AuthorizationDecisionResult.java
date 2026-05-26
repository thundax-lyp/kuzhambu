package com.thundax.kuzhambu.interfaces.admin.auth.service.result;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuth2AuthorizationDecisionResult {
    private boolean approved;
    private String authorizationCode;
    private String state;
}
