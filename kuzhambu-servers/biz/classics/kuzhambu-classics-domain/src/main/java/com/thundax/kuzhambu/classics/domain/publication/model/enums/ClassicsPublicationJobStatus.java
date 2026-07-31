package com.thundax.kuzhambu.classics.domain.publication.model.enums;

public enum ClassicsPublicationJobStatus {
    QUEUED,
    SNAPSHOT_READY,
    ES_PREPARED,
    FASTGPT_PREPARED,
    ES_READY,
    FASTGPT_READY,
    ES_DISABLED,
    FASTGPT_DISABLED,
    CONTENT_COMMITTED
}
