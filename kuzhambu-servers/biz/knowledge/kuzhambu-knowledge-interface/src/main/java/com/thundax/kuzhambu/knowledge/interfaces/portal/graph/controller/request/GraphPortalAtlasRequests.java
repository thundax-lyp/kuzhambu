package com.thundax.kuzhambu.knowledge.interfaces.portal.graph.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public final class GraphPortalAtlasRequests {
    private GraphPortalAtlasRequests() {}

    @Getter
    @Setter
    @Schema(description = "门户图谱一跳关系渐进查询请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OneHopEdgesListRequest {
        @NotEmpty
        @Size(max = 400)
        private List<@Pattern(regexp = "^\\d+$") String> nodeIds;

        @Pattern(regexp = "^\\d+$")
        private String afterEdgeId;
    }
}
