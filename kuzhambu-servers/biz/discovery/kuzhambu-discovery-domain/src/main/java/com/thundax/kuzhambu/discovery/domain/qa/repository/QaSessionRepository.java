package com.thundax.kuzhambu.discovery.domain.qa.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaOwnerRef;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaSessionId;
import java.time.Instant;
import java.util.List;

public interface QaSessionRepository {

    QaSession getById(QaSessionId id);

    default QaSession getBySessionId(QaSessionId sessionId) {
        return getById(sessionId);
    }

    List<QaSession> listByOpenedAtRange(Instant openedAtStart, Instant openedAtEnd);

    PageResult<QaSession> page(String title, Instant openedAtStart, Instant openedAtEnd, int pageNo, int pageSize);

    List<QaSession> listByOwnerUserId(QaOwnerRef owner, Integer limit);

    QaSessionId save(QaSession entity);

    int update(QaSession entity);

    int markRemoved(QaSessionId id, Instant removedAt);
}
