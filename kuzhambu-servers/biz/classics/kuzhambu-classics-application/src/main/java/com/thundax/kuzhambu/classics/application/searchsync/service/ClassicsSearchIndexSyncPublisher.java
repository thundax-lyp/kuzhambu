package com.thundax.kuzhambu.classics.application.searchsync.service;

import com.thundax.kuzhambu.classics.facade.dto.ClassicsSearchIndexSyncMessageFacadeDto;

public interface ClassicsSearchIndexSyncPublisher {
    void publish(ClassicsSearchIndexSyncMessageFacadeDto message);
}
