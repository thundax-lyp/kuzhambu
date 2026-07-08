package com.thundax.kuzhambu.knowledge.application.taxonomy.result;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiscoverySynonymQueryResult {
    private String term;
    private String normalizedTerm;
    private String direction;
    private int limit;
    private List<DiscoverySynonymMatchResult> matches;
}
