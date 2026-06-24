package com.thundax.kuzhambu.discovery.infra.search.persistence.assembler;

import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchClick;
import com.thundax.kuzhambu.discovery.infra.search.persistence.dataobject.SearchClickDO;
import java.util.ArrayList;
import java.util.List;

public final class SearchClickPersistenceAssembler {

    private SearchClickPersistenceAssembler() {}

    public static SearchClickDO toObject(SearchClick entity) {
        if (entity == null) {
            return null;
        }
        SearchClickDO dataObject = new SearchClickDO();
        dataObject.setId(entity.getId());
        dataObject.setSearchClickId(entity.getSearchClickId());
        dataObject.setSearchLogId(entity.getSearchLogId());
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

    public static SearchClick toDomain(SearchClickDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        SearchClick entity = new SearchClick();
        entity.setId(dataObject.getId());
        entity.setSearchClickId(dataObject.getSearchClickId());
        entity.setSearchLogId(dataObject.getSearchLogId());
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

    public static List<SearchClick> toDomainList(List<SearchClickDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<SearchClick> entities = new ArrayList<>();
        for (SearchClickDO dataObject : dataObjects) {
            entities.add(toDomain(dataObject));
        }
        return entities;
    }
}
