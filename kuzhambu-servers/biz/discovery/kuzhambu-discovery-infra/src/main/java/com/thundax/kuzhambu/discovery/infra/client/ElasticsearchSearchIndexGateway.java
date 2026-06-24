package com.thundax.kuzhambu.discovery.infra.client;

import com.thundax.kuzhambu.discovery.application.search.result.SearchGroupResult;
import com.thundax.kuzhambu.discovery.application.search.support.SearchIndexGateway;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchKeyword;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchScope;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ElasticsearchSearchIndexGateway implements SearchIndexGateway {

    private final DiscoverySearchIndexProperties properties;

    public ElasticsearchSearchIndexGateway() {
        this(new DiscoverySearchIndexProperties());
    }

    public ElasticsearchSearchIndexGateway(DiscoverySearchIndexProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<SearchGroupResult> search(SearchKeyword keyword, SearchScope searchScope, int pageNo, int pageSize) {
        throw new UnsupportedOperationException(
                "Discovery search backend is not implemented for index " + properties.getIndexName());
    }

    @Override
    public void rebuildIndex() {
        throw new UnsupportedOperationException(
                "Discovery index rebuild is not implemented for index " + properties.getIndexName());
    }

    @Override
    public void upsertDocuments(List<String> documentIds) {
        throw new UnsupportedOperationException(
                "Discovery document upsert is not implemented for index " + properties.getIndexName());
    }
}
