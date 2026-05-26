package com.thundax.kuzhambu.interfaces.admin.auth.service.provider;

public interface WecomLoginProvider {

    String resolveIdentity(String code);
}
