package com.thundax.kuzhambu.knowledge.application.taxonomy.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryTagHintResult {
    private String term;
    private String normalizedTerm;
    private String matchedTagName;
    private String matchedAliasName;
    private Long contentRefCount;
}
