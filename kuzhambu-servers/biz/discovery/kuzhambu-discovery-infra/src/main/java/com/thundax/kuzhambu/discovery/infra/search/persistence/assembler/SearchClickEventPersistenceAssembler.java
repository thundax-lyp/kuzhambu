package com.thundax.kuzhambu.discovery.infra.search.persistence.assembler;

import com.thundax.kuzhambu.discovery.domain.search.codec.SearchClickEventIdCodec;
import com.thundax.kuzhambu.discovery.domain.search.codec.SearchEventIdCodec;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchClickEvent;
import com.thundax.kuzhambu.discovery.infra.search.persistence.dataobject.SearchClickEventDO;
import java.util.ArrayList;
import java.util.List;

public final class SearchClickEventPersistenceAssembler {

    private SearchClickEventPersistenceAssembler() {}

    public static SearchClickEventDO toObject(SearchClickEvent entity) {
        if (entity == null) {
            return null;
        }
        SearchClickEventDO dataObject = new SearchClickEventDO();
        dataObject.setId(SearchClickEventIdCodec.toValue(entity.getId()));
        dataObject.setSearchClickEventId(entity.getSearchClickEventId());
        dataObject.setSearchEventId(SearchEventIdCodec.toValue(entity.getSearchEventId()));
        dataObject.setContentDomain(entity.getContentDomain());
        dataObject.setContentType(entity.getContentType());
        dataObject.setContentId(entity.getContentId());
        dataObject.setContentTitle(entity.getContentTitle());
        dataObject.setResultGroupKey(entity.getResultGroupKey());
        dataObject.setResultRank(entity.getResultRank());
        dataObject.setGroupRank(entity.getGroupRank());
        dataObject.setTargetPath(entity.getTargetPath());
        dataObject.setOperatorType(entity.getOperatorType());
        dataObject.setOperatorId(entity.getOperatorId());
        dataObject.setRequestId(entity.getRequestId());
        dataObject.setTraceId(entity.getTraceId());
        dataObject.setCreatedAt(entity.getCreatedAt());
        return dataObject;
    }

    public static SearchClickEvent toDomain(SearchClickEventDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        SearchClickEvent entity = new SearchClickEvent();
        entity.setId(SearchClickEventIdCodec.toDomain(dataObject.getId()));
        entity.setSearchEventId(SearchEventIdCodec.toDomain(dataObject.getSearchEventId()));
        entity.setContentDomain(dataObject.getContentDomain());
        entity.setContentType(dataObject.getContentType());
        entity.setContentId(dataObject.getContentId());
        entity.setContentTitle(dataObject.getContentTitle());
        entity.setResultGroupKey(dataObject.getResultGroupKey());
        entity.setResultRank(dataObject.getResultRank());
        entity.setGroupRank(dataObject.getGroupRank());
        entity.setTargetPath(dataObject.getTargetPath());
        entity.setOperatorType(dataObject.getOperatorType());
        entity.setOperatorId(dataObject.getOperatorId());
        entity.setRequestId(dataObject.getRequestId());
        entity.setTraceId(dataObject.getTraceId());
        entity.setCreatedAt(dataObject.getCreatedAt());
        return entity;
    }

    public static List<SearchClickEvent> toDomainList(List<SearchClickEventDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<SearchClickEvent> entities = new ArrayList<>();
        for (SearchClickEventDO dataObject : dataObjects) {
            entities.add(toDomain(dataObject));
        }
        return entities;
    }
}
