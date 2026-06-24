package com.thundax.kuzhambu.discovery.domain.search.model.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchKeyword {
    private String rawText;
    private String normalizedText;
    private String displayText;
}
