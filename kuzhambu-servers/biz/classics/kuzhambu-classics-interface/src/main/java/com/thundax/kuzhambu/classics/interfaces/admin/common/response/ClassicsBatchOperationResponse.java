package com.thundax.kuzhambu.classics.interfaces.admin.common.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationItemResult;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationResult;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassicsBatchOperationResponse implements Serializable {
    @JsonProperty("successCount")
    private Integer successCount;

    @JsonProperty("failureCount")
    private Integer failureCount;

    @JsonProperty("successes")
    private List<Item> successes;

    @JsonProperty("failures")
    private List<Item> failures;

    public static ClassicsBatchOperationResponse from(ClassicsBatchOperationResult result) {
        if (result == null) {
            return ClassicsBatchOperationResponse.builder()
                    .successCount(0)
                    .failureCount(0)
                    .successes(List.of())
                    .failures(List.of())
                    .build();
        }
        return ClassicsBatchOperationResponse.builder()
                .successCount(result.getSuccessCount())
                .failureCount(result.getFailureCount())
                .successes(toItems(result.getSuccesses()))
                .failures(toItems(result.getFailures()))
                .build();
    }

    private static List<Item> toItems(List<ClassicsBatchOperationItemResult> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream().map(ClassicsBatchOperationResponse::toItem).toList();
    }

    private static Item toItem(ClassicsBatchOperationItemResult item) {
        return Item.builder()
                .contentType(item.getContentType())
                .contentId(item.getContentId())
                .resultId(item.getResultId())
                .status(item.getStatus())
                .failureCode(item.getFailureCode())
                .failureReason(item.getFailureReason())
                .build();
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item implements Serializable {
        @JsonProperty("contentType")
        private String contentType;

        @JsonProperty("contentId")
        private Long contentId;

        @JsonProperty("resultId")
        private Long resultId;

        @JsonProperty("status")
        private String status;

        @JsonProperty("failureCode")
        private String failureCode;

        @JsonProperty("failureReason")
        private String failureReason;
    }
}
