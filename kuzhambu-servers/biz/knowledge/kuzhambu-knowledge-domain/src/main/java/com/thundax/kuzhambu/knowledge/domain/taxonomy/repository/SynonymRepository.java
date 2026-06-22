package com.thundax.kuzhambu.knowledge.domain.taxonomy.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Synonym;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.SynonymStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.SynonymId;

public interface SynonymRepository {

    Synonym getById(SynonymId id);

    PageResult<Synonym> page(String term, String synonym, SynonymStatus status, int pageNo, int pageSize);

    int countByPair(String term, String synonym, SynonymId excludedId);

    SynonymId insert(Synonym entity);

    int update(Synonym entity);

    int updateStatus(Synonym entity);

    int deleteById(SynonymId id);
}
