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
public class DiscoverySynonymExpandResult {
    private String term;
    private String normalizedTerm;
    private List<String> expandedTerms;
}
