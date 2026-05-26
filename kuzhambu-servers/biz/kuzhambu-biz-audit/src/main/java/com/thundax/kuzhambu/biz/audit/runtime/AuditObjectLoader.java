package com.thundax.kuzhambu.biz.audit.runtime;

public interface AuditObjectLoader {

    String objectType();

    Object load(String objectId);
}
