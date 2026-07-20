package com.thundax.kuzhambu.system.application.auth.service;

import java.util.Set;

public interface PermissionApplicationService {

    Set<String> createPermissions(String token, String userId);

    Set<String> getPermissions(String token);

    boolean isPermitted(String token, String permission);
}
