package com.thundax.kuzhambu.discovery.infra.search.persistence.assembler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.discovery.application.search.support.SearchTimeObjectMapperFactory;
import com.thundax.kuzhambu.discovery.domain.search.codec.SearchEventIdCodec;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchEvent;
import com.thundax.kuzhambu.discovery.domain.search.model.enums.SearchIntentType;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchScope;
import com.thundax.kuzhambu.discovery.infra.search.persistence.dataobject.SearchEventDO;
import java.util.ArrayList;
import java.util.List;

public final class SearchEventPersistenceAssembler {

    private static final ObjectMapper OBJECT_MAPPER = SearchTimeObjectMapperFactory.create();

    private SearchEventPersistenceAssembler() {}

    public static SearchEventDO toObject(SearchEvent entity) {
        if (entity == null) {
            return null;
        }
        SearchEventDO dataObject = new SearchEventDO();
        dataObject.setId(SearchEventIdCodec.toValue(entity.getId()));
        dataObject.setSearchEventId(entity.getSearchEventId());
        dataObject.setQueryText(entity.getQueryText());
        dataObject.setNormalizedQueryText(entity.getNormalizedQueryText());
        dataObject.setDisplayQueryText(entity.getDisplayQueryText());
        dataObject.setIntentType(intentTypeValue(entity.getIntentType()));
        dataObject.setSearchScopesJson(writeScope(entity.getSearchScope()));
        dataObject.setResultTotalCount(entity.getResultTotalCount());
        dataObject.setGroupTotalCount(entity.getGroupTotalCount());
        dataObject.setSearchLatencyMs(entity.getSearchLatencyMs());
        dataObject.setSearchStatus(entity.getSearchStatus());
        dataObject.setFailureCode(entity.getFailureCode());
        dataObject.setFailureMessage(entity.getFailureMessage());
        dataObject.setOperatorType(entity.getOperatorType());
        dataObject.setOperatorId(entity.getOperatorId());
        dataObject.setRequestId(entity.getRequestId());
        dataObject.setTraceId(entity.getTraceId());
        dataObject.setCreatedAt(entity.getCreatedAt());
        return dataObject;
    }

    public static SearchEvent toDomain(SearchEventDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        SearchEvent entity = new SearchEvent();
        entity.setId(SearchEventIdCodec.toDomain(dataObject.getId()));
        entity.setQueryText(dataObject.getQueryText());
        entity.setNormalizedQueryText(dataObject.getNormalizedQueryText());
        entity.setDisplayQueryText(dataObject.getDisplayQueryText());
        entity.setIntentType(intentTypeFrom(dataObject.getIntentType()));
        entity.setSearchScope(readScope(dataObject.getSearchScopesJson()));
        entity.setResultTotalCount(dataObject.getResultTotalCount());
        entity.setGroupTotalCount(dataObject.getGroupTotalCount());
        entity.setSearchLatencyMs(dataObject.getSearchLatencyMs());
        entity.setSearchStatus(dataObject.getSearchStatus());
        entity.setFailureCode(dataObject.getFailureCode());
        entity.setFailureMessage(dataObject.getFailureMessage());
        entity.setOperatorType(dataObject.getOperatorType());
        entity.setOperatorId(dataObject.getOperatorId());
        entity.setRequestId(dataObject.getRequestId());
        entity.setTraceId(dataObject.getTraceId());
        entity.setCreatedAt(dataObject.getCreatedAt());
        return entity;
    }

    public static List<SearchEvent> toDomainList(List<SearchEventDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<SearchEvent> entities = new ArrayList<>();
        for (SearchEventDO dataObject : dataObjects) {
            entities.add(toDomain(dataObject));
        }
        return entities;
    }

    private static String intentTypeValue(SearchIntentType intentType) {
        return intentType == null ? null : intentType.value();
    }

    private static SearchIntentType intentTypeFrom(String value) {
        return value == null ? null : SearchIntentType.from(value);
    }

    private static String writeScope(SearchScope searchScope) {
        if (searchScope == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(searchScope);
        } catch (JsonProcessingException exception) {
            return null;
        }
    }

    private static SearchScope readScope(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, SearchScope.class);
        } catch (JsonProcessingException exception) {
            return null;
        }
    }
}
