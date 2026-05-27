package com.thundax.kuzhambu.system.interfaces.admin.auth.service.provider;

public interface WecomLoginProvider {

    String resolveIdentity(String code);
}
