package com.thundax.kuzhambu.knowledge.application.taxonomy.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryEntityHintResult {
    private String term;
    private String normalizedTerm;
    private String entityName;
    private String entityType;
    private Long contentRefCount;
}
