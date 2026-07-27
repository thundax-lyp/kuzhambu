package com.thundax.kuzhambu.ai.application.batch.command;

import com.thundax.kuzhambu.ai.domain.batch.model.entity.AiBatchJob;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiBatchJobCreateCommand {

    private String scope;
    private String capability;
    private String contentType;
    private int totalCount;
    private String failureSummaryJson;

    public AiBatchJob toEntity() {
        AiBatchJob job = new AiBatchJob();
        job.setScope(scope);
        job.setCapability(AiBusinessCapability.from(normalizeCapability(capability)));
        job.setContentType(contentType);
        job.setTotalCount(totalCount);
        job.setFailureSummaryJson(failureSummaryJson);
        job.setRequestedAt(Instant.now());
        return job;
    }

    private String normalizeCapability(String value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case "translate" -> "classics_translate";
            case "summary" -> "classics_summary";
            case "tags" -> "classics_tags";
            case "qa" -> "classics_qa";
            case "image_analysis" -> "classics_image_describe";
            case "visual" -> "classics_visual_describe";
            case "image_gen" -> "classics_image_generate";
            case "split" -> "classics_split";
            default -> value;
        };
    }
}
