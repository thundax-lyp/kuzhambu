package com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobResultStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobType;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "古籍发布任务分页请求")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassicsPublicationJobPageRequest extends PageRequest {
    @Schema(description = "发布任务类型")
    private ClassicsPublicationJobType jobType;

    @Schema(description = "发布任务结果状态")
    private ClassicsPublicationJobResultStatus jobResultStatus;

    @Schema(description = "发布任务执行状态")
    private ClassicsPublicationJobStatus jobStatus;

    @Schema(description = "内容类型")
    private ClassicsContentType contentType;

    @Schema(description = "标题关键词")
    private String keyword;
}
