package com.thundax.kuzhambu.discovery.domain.qa.repository;

import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaRetrievalTrace;

public interface QaRetrievalTraceRepository {

    QaRetrievalTrace getByTraceId(Long traceId);

    QaRetrievalTrace getByMessageId(Long messageId);

    Long save(QaRetrievalTrace entity);

    int update(QaRetrievalTrace entity);
}
