package com.thundax.kuzhambu.discovery.domain.qa.repository;

import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSource;
import java.util.List;

public interface QaSourceRepository {

    List<QaSource> listByMessageId(Long messageId);

    Long save(QaSource entity);

    int deleteByMessageId(Long messageId);
}
