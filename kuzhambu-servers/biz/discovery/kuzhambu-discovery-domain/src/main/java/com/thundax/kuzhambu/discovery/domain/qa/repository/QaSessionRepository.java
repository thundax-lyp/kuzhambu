package com.thundax.kuzhambu.discovery.domain.qa.repository;

import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import java.util.List;

public interface QaSessionRepository {

    QaSession getBySessionId(Long sessionId);

    List<QaSession> listByOpenedAtRange(java.util.Date openedAtStart, java.util.Date openedAtEnd);

    List<QaSession> listByOwnerUserId(Long ownerUserId, Integer limit);

    Long save(QaSession entity);

    int update(QaSession entity);
}
