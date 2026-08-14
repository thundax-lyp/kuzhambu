package com.thundax.kuzhambu.knowledge.interfaces.portal.graph.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "门户图谱素材响应")
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphPortalMaterialResponse implements Serializable {
    @JsonProperty("visible")
    private boolean visible;

    @JsonProperty("contentRef")
    private ContentRefData contentRef;

    @JsonProperty("nodes")
    private List<NodeData> nodes;

    @JsonProperty("edges")
    private List<EdgeData> edges;

    @Getter
    @Builder
    @Schema(description = "门户图谱素材内容引用")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContentRefData implements Serializable {
        @JsonProperty("contentType")
        private String contentType;

        @JsonProperty("contentRefId")
        private String contentRefId;
    }

    @Getter
    @Builder
    @Schema(description = "门户图谱节点")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NodeData implements Serializable {
        @JsonProperty("id")
        private String id;

        @JsonProperty("nodeType")
        private String nodeType;

        @JsonProperty("name")
        private String name;

        @JsonProperty("status")
        private String status;

        @JsonProperty("lockVersion")
        private String lockVersion;
    }

    @Getter
    @Builder
    @Schema(description = "门户图谱关系")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EdgeData implements Serializable {
        @JsonProperty("id")
        private String id;

        @JsonProperty("sourceNodeId")
        private String sourceNodeId;

        @JsonProperty("targetNodeId")
        private String targetNodeId;

        @JsonProperty("relationType")
        private String relationType;

        @JsonProperty("qualifiers")
        private Map<String, Object> qualifiers;

        @JsonProperty("status")
        private String status;

        @JsonProperty("lockVersion")
        private String lockVersion;
    }
}
