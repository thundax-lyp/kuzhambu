package com.thundax.kuzhambu.discovery.domain.search.model.valueobject;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchScope {
    private List<String> knowledgeBases;
    private List<String> categoryCodes;
    private List<String> tagNames;
    private List<String> privateKnowledgeBases;
    private Instant dateFrom;
    private Instant dateTo;
}
