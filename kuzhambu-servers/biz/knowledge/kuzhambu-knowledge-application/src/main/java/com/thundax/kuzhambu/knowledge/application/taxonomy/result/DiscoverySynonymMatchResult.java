package com.thundax.kuzhambu.knowledge.application.taxonomy.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiscoverySynonymMatchResult {
    private String sourceTerm;
    private String targetTerm;
    private String matchedTerm;
    private String expandedTerm;
    private String direction;
}
