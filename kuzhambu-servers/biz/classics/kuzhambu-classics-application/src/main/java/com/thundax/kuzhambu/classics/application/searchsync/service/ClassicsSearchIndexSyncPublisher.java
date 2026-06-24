package com.thundax.kuzhambu.classics.application.searchsync.service;

import com.thundax.kuzhambu.classics.application.searchsync.model.ClassicsSearchIndexSyncMessage;

public interface ClassicsSearchIndexSyncPublisher {
    void publish(ClassicsSearchIndexSyncMessage message);
}
