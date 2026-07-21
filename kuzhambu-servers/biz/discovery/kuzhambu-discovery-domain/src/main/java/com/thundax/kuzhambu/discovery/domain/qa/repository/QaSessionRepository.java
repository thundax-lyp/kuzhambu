package com.thundax.kuzhambu.discovery.domain.qa.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import java.util.Date;
import java.util.List;

public interface QaSessionRepository {

    QaSession getBySessionId(Long sessionId);

    List<QaSession> listByOpenedAtRange(java.util.Date openedAtStart, java.util.Date openedAtEnd);

    PageResult<QaSession> page(String title, Date openedAtStart, Date openedAtEnd, int pageNo, int pageSize);

    List<QaSession> listByOwnerUserId(String ownerType, String ownerId, Integer limit);

    Long save(QaSession entity);

    int update(QaSession entity);

    int markRemoved(Long sessionId, Date removedAt);
}
