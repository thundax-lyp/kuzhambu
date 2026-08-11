package com.thundax.kuzhambu.operations.interfaces.admin.cleanup.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OperationsCleanupDetailResponse", description = "Operations 清理明细响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsCleanupDetailResponse {
    private Long cleanupId;
    private String cleanupType;
    private String cleanupStatus;
    private Integer totalCount;
    private Integer successCount;
    private Integer failedCount;
    private String failureReason;
    private Long requesterUserId;
    private Instant startedAt;
    private Instant completedAt;
    private List<Item> items;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "Item", description = "清理明细项")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private Long cleanupItemId;
        private String targetType;
        private Long targetId;
        private String itemStatus;
        private String failureReason;
        private Instant processedAt;
    }
}
