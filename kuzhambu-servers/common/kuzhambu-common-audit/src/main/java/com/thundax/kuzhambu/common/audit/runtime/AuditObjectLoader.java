package com.thundax.kuzhambu.common.audit.runtime;

public interface AuditObjectLoader {

    String objectType();

    Object load(String objectId);
}
