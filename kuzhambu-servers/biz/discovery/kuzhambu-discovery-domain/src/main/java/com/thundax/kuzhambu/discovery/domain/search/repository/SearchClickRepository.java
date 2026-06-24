package com.thundax.kuzhambu.discovery.domain.search.repository;

import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchClick;

public interface SearchClickRepository {

    SearchClick getBySearchClickId(String searchClickId);

    Long save(SearchClick entity);
}
