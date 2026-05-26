package com.thundax.kuzhambu.interfaces.admin.auth.service.provider;

public interface GithubLoginProvider {

    String resolveIdentity(String code);
}
