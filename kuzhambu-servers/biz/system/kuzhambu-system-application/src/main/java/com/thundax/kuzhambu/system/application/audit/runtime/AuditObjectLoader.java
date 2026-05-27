package com.thundax.kuzhambu.system.application.audit.runtime;

public interface AuditObjectLoader {

    String objectType();

    Object load(String objectId);
}
