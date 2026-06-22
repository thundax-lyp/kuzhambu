package com.thundax.kuzhambu.knowledge.application.taxonomy.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SynonymResult {
    private String id;
    private String term;
    private String synonym;
    private String status;
}
