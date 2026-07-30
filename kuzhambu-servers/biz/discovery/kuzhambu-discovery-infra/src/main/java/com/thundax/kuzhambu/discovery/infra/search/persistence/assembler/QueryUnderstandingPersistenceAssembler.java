package com.thundax.kuzhambu.discovery.infra.search.persistence.assembler;

import com.thundax.kuzhambu.discovery.domain.search.codec.QueryUnderstandingIdCodec;
import com.thundax.kuzhambu.discovery.domain.search.codec.SearchEventIdCodec;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.QueryUnderstanding;
import com.thundax.kuzhambu.discovery.domain.search.model.enums.SearchIntentType;
import com.thundax.kuzhambu.discovery.infra.search.persistence.dataobject.QueryUnderstandingDO;
import java.util.ArrayList;
import java.util.List;

public final class QueryUnderstandingPersistenceAssembler {

    private QueryUnderstandingPersistenceAssembler() {}

    public static QueryUnderstandingDO toObject(QueryUnderstanding entity) {
        if (entity == null) {
            return null;
        }
        QueryUnderstandingDO dataObject = new QueryUnderstandingDO();
        dataObject.setId(QueryUnderstandingIdCodec.toValue(entity.getId()));
        dataObject.setQueryUnderstandingId(entity.getQueryUnderstandingId());
        dataObject.setSearchEventId(SearchEventIdCodec.toValue(entity.getSearchEventId()));
        dataObject.setQueryText(entity.getQueryText());
        dataObject.setNormalizedQueryText(entity.getNormalizedQueryText());
        dataObject.setRewrittenQueryText(entity.getRewrittenQueryText());
        dataObject.setIntentType(intentTypeValue(entity.getIntentType()));
        dataObject.setRecognizedEntitiesJson(entity.getRecognizedEntitiesJson());
        dataObject.setUnderstandingStatus(entity.getUnderstandingStatus());
        dataObject.setFailureCode(entity.getFailureCode());
        dataObject.setFailureMessage(entity.getFailureMessage());
        dataObject.setRequestId(entity.getRequestId());
        dataObject.setTraceId(entity.getTraceId());
        dataObject.setCreatedAt(entity.getCreatedAt());
        return dataObject;
    }

    public static QueryUnderstanding toDomain(QueryUnderstandingDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        QueryUnderstanding entity = new QueryUnderstanding();
        entity.setId(QueryUnderstandingIdCodec.toDomain(dataObject.getId()));
        entity.setSearchEventId(SearchEventIdCodec.toDomain(dataObject.getSearchEventId()));
        entity.setQueryText(dataObject.getQueryText());
        entity.setNormalizedQueryText(dataObject.getNormalizedQueryText());
        entity.setRewrittenQueryText(dataObject.getRewrittenQueryText());
        entity.setIntentType(intentTypeFrom(dataObject.getIntentType()));
        entity.setRecognizedEntitiesJson(dataObject.getRecognizedEntitiesJson());
        entity.setUnderstandingStatus(dataObject.getUnderstandingStatus());
        entity.setFailureCode(dataObject.getFailureCode());
        entity.setFailureMessage(dataObject.getFailureMessage());
        entity.setRequestId(dataObject.getRequestId());
        entity.setTraceId(dataObject.getTraceId());
        entity.setCreatedAt(dataObject.getCreatedAt());
        return entity;
    }

    public static List<QueryUnderstanding> toDomainList(List<QueryUnderstandingDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<QueryUnderstanding> entities = new ArrayList<>();
        for (QueryUnderstandingDO dataObject : dataObjects) {
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
}
