package com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "古籍发布批量操作请求")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassicsPublicationBatchActionRequest {
    @Schema(description = "内容ID列表")
    @NotEmpty
    private List<@Valid @NotNull Long> ids;

    public ClassicsPublicationBatchActionRequest() {}

    public ClassicsPublicationBatchActionRequest(List<@Valid @NotNull Long> ids) {
        this.ids = ids;
    }
}
