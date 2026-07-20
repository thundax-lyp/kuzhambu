package com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "DiscoverySearchClickEventRequest", description = "Discovery Portal 检索点击事件请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscoverySearchClickEventRequest {

    @Schema(name = "searchEventId", description = "检索统计事件号")
    @JsonProperty(value = "searchEventId")
    @NotBlank(message = "\"检索统计事件号\"不能为空")
    private String searchEventId;

    @Schema(name = "contentDomain", description = "内容域")
    @JsonProperty(value = "contentDomain")
    @NotBlank(message = "\"内容域\"不能为空")
    private String contentDomain;

    @Schema(name = "contentType", description = "内容类型")
    @JsonProperty(value = "contentType")
    @NotBlank(message = "\"内容类型\"不能为空")
    private String contentType;

    @Schema(name = "contentId", description = "内容业务标识")
    @JsonProperty(value = "contentId")
    @NotBlank(message = "\"内容业务标识\"不能为空")
    private String contentId;

    @Schema(name = "contentTitle", description = "点击时标题快照")
    @JsonProperty(value = "contentTitle")
    private String contentTitle;

    @Schema(name = "resultGroupKey", description = "结果分组键")
    @JsonProperty(value = "resultGroupKey")
    @NotBlank(message = "\"结果分组键\"不能为空")
    private String resultGroupKey;

    @Schema(name = "resultRank", description = "全结果位置")
    @JsonProperty(value = "resultRank")
    @NotNull(message = "\"全结果位置\"不能为空")
    private Integer resultRank;

    @Schema(name = "groupRank", description = "组内位置")
    @JsonProperty(value = "groupRank")
    @NotNull(message = "\"组内位置\"不能为空")
    private Integer groupRank;

    @Schema(name = "targetPath", description = "跳转路径")
    @JsonProperty(value = "targetPath")
    private String targetPath;
}
