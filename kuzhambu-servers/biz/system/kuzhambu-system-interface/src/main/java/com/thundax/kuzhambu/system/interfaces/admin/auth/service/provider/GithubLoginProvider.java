package com.thundax.kuzhambu.system.interfaces.admin.auth.service.provider;

public interface GithubLoginProvider {

    String resolveIdentity(String code);
}
