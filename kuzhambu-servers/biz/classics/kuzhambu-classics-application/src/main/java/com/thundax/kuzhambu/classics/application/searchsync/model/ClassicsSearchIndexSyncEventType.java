package com.thundax.kuzhambu.classics.application.searchsync.model;

public enum ClassicsSearchIndexSyncEventType {
    UPSERT,
    DELETE;

    public String value() {
        return name();
    }
}
