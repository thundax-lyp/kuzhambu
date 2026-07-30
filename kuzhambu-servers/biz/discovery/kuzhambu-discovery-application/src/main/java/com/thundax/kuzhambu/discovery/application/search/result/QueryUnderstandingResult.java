package com.thundax.kuzhambu.discovery.application.search.result;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QueryUnderstandingResult {
    private String normalizedQueryText;
    private String rewrittenQueryText;
    private String intent;
    private List<RecognizedEntityResult> recognizedEntities;
    private String requestId;
    private String traceId;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecognizedEntityResult {
        private String name;
        private String type;
        private String matchedText;
    }
}
