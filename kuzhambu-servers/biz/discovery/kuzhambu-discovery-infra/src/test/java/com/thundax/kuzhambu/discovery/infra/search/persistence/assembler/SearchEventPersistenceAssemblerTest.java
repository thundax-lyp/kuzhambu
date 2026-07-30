package com.thundax.kuzhambu.discovery.infra.search.persistence.assembler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchEvent;
import com.thundax.kuzhambu.discovery.domain.search.model.enums.SearchIntentType;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchScope;
import com.thundax.kuzhambu.discovery.infra.search.persistence.dataobject.SearchEventDO;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class SearchEventPersistenceAssemblerTest {

    @Test
    void toObjectAndToDomainShouldKeepSearchLatencyMs() {
        SearchEvent entity = new SearchEvent(
                1L,
                "1",
                "黄帝",
                "huangdi",
                "黄帝",
                SearchIntentType.KEYWORD_SEARCH,
                new SearchScope(
                        List.of("SANCAI_ENTRY"),
                        List.of("11"),
                        List.of("上古"),
                        List.of(),
                        List.of(),
                        List.of(),
                        null,
                        null),
                2,
                1,
                123L,
                "SUCCEEDED",
                null,
                null,
                "ADMIN",
                "admin-1",
                "req-1",
                "trace-1",
                new Date(1_718_000_000_000L));

        SearchEventDO dataObject = SearchEventPersistenceAssembler.toObject(entity);
        SearchEvent restored = SearchEventPersistenceAssembler.toDomain(dataObject);

        assertEquals(123L, dataObject.getSearchLatencyMs());
        assertEquals(123L, restored.getSearchLatencyMs());
        assertEquals(SearchIntentType.KEYWORD_SEARCH, restored.getIntentType());
        assertEquals(List.of("SANCAI_ENTRY"), restored.getSearchScope().getKnowledgeBases());
    }

    @Test
    void toDomainShouldAllowEmptySearchLatencyMs() {
        SearchEventDO dataObject = new SearchEventDO();
        dataObject.setId(2L);

        SearchEvent restored = SearchEventPersistenceAssembler.toDomain(dataObject);

        assertNull(restored.getSearchLatencyMs());
    }
}
