package com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "古籍发布操作请求")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassicsPublicationActionRequest {
    @Schema(description = "内容ID")
    @NotNull
    private Long id;

    public ClassicsPublicationActionRequest() {}

    public ClassicsPublicationActionRequest(Long id) {
        this.id = id;
    }
}
