package com.thundax.kuzhambu.discovery.application.qa.result;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeHealthResult {
    private boolean available;
    private String provider;
    private String message;
    private Map<String, Object> raw;
}
