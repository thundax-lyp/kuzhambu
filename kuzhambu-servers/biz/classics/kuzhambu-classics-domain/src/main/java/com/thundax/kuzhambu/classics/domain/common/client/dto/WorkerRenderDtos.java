package com.thundax.kuzhambu.classics.domain.common.client.dto;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public final class WorkerRenderDtos {

    private WorkerRenderDtos() {}

    @Getter
    @Setter
    @NoArgsConstructor
    public static class WorkerRenderRequest {

        private String requestId;
        private String traceId;
        private String callerDomain;
        private String operation;
        private String renderType;
        private Template template;
        private Output output;
        private Input input;
        private Options options;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Template {

        private String templateId;
        private String templateVersion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Output {

        private String format;
        private String filenameHint;
        private String locale;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Input {

        private String snapshotId;
        private String contentType;
        private String payloadJson;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Options {

        private boolean stream;
        private boolean includeMetadata;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class WorkerRenderResponse {

        private String requestId;
        private String traceId;
        private String status;
        private String renderType;
        private Artifact artifact;
        private Summary summary;
        private Usage usage;
        private WorkerRenderError error;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Artifact {

        private String format;
        private String filename;
        private String contentType;
        private String encoding;
        private String content;
        private Long sizeBytes;
        private String sha256;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Summary {

        private Integer itemCount;
        private List<String> warnings;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Usage {

        private Integer latencyMs;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class WorkerRenderError {

        private String type;
        private String code;
        private String message;
        private Boolean retryable;
        private Map<String, Object> detail;
    }
}
