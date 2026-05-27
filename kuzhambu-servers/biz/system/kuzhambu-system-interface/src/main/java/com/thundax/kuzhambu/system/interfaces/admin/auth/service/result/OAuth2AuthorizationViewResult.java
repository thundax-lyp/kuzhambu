package com.thundax.kuzhambu.system.interfaces.admin.auth.service.result;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuth2AuthorizationViewResult {
    private String clientId;
    private String clientName;
    private String redirectUri;
    private Set<String> scopes;
    private String state;
}
