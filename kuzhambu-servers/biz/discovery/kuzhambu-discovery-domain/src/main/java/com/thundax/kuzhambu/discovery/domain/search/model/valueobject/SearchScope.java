package com.thundax.kuzhambu.discovery.domain.search.model.valueobject;

import java.util.Date;
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
    private List<String> contentStatuses;
    private List<String> visibilityScopes;
    private List<String> privateKnowledgeBases;
    private Date dateFrom;
    private Date dateTo;
}
