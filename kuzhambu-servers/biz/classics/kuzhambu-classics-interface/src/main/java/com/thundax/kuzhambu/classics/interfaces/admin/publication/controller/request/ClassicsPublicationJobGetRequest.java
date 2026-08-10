package com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "古籍发布任务详情请求")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassicsPublicationJobGetRequest {
    @Schema(description = "发布任务ID")
    @NotNull
    private Long id;

    public ClassicsPublicationJobGetRequest() {}

    public ClassicsPublicationJobGetRequest(Long id) {
        this.id = id;
    }
}
