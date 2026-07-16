package com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller;

import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairSortCommand;
import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentQaPairIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentQaPairId;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiContentRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiContentSortRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiContentResponse;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.common.web.request.RequestListHelper;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "古籍模块-三才图会内容", description = "三才图会内容")
@SysLogger(module = {"古籍", "三才图会", "内容"})
@RequestMapping("/api/classics/sancai/contents")
@WrappedApiController
public class SancaiContentAdminController {
    private static final String CONTENT_TYPE = ClassicsContentType.SANCAI_ENTRY.value();

    private final ClassicsContentApplicationService service;

    public SancaiContentAdminController(ClassicsContentApplicationService service) {
        this.service = service;
    }

    @Operation(summary = "查询三才图会条目内容", description = "classics:sancai:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("classics:sancai:view")
    @SysLogger(value = "内容列表")
    @PostMapping("list")
    public List<SancaiContentResponse> listContents(@Valid @RequestBody SancaiContentRequest request) {
        return service.listQaPairs(CONTENT_TYPE, ClassicsContentIdCodec.toDomain(request.getEntryId())).stream()
                .map(SancaiContentAdminController::toResponse)
                .toList();
    }

    @Operation(summary = "新增三才图会条目内容", description = "classics:sancai:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "内容新增")
    @PostMapping("add")
    public SancaiContentResponse addContent(@Valid @RequestBody SancaiContentRequest request) {
        ClassicsContentQaPairId id = service.addQaPair(toCommand(request));
        return SancaiContentResponse.builder()
                .id(id == null ? null : id.value())
                .build();
    }

    @Operation(summary = "更新三才图会条目内容", description = "classics:sancai:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "内容更新")
    @PostMapping("update")
    public SancaiContentResponse updateContent(@Valid @RequestBody SancaiContentRequest request) {
        ClassicsContentQaPairId id = service.updateQaPair(toCommand(request));
        return SancaiContentResponse.builder()
                .id(id == null ? null : id.value())
                .build();
    }

    @Operation(summary = "删除三才图会条目内容", description = "classics:sancai:delete")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("classics:sancai:delete")
    @SysLogger(value = "内容删除")
    @PostMapping("delete")
    public void deleteContent(@Valid @RequestBody SancaiContentRequest request) {
        service.deleteQaPair(ClassicsContentQaPairIdCodec.toDomain(request.getId()));
    }

    @Operation(summary = "排序三才图会条目内容", description = "classics:sancai:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "内容排序")
    @PostMapping("sort")
    public Boolean sortContents(@Valid @RequestBody SancaiContentSortRequest request) {
        service.sortQaPairs(
                CONTENT_TYPE,
                ClassicsContentIdCodec.toDomain(request == null ? null : request.getEntryId()),
                new ContentQaPairSortCommand(
                        RequestListHelper.map(
                                RequestListHelper.presentUnique(
                                        request == null ? null : request.getOrderedIds(),
                                        "orderedIds",
                                        AdminResponseExceptions::invalidParameter),
                                ClassicsContentQaPairIdCodec::toDomain),
                        request == null ? null : request.getSortDirection()));
        return true;
    }

    private static ContentQaPairCommand toCommand(SancaiContentRequest request) {
        return new ContentQaPairCommand(
                request.getId(),
                ClassicsContentType.SANCAI_ENTRY,
                request.getEntryId(),
                request.getQuestion(),
                request.getAnswer(),
                StringUtils.isBlank(request.getSource())
                        ? ClassicsContentSource.MANUAL
                        : ClassicsContentSource.from(request.getSource()));
    }

    private static SancaiContentResponse toResponse(ClassicsContentQaPair content) {
        return content == null
                ? SancaiContentResponse.builder().build()
                : SancaiContentResponse.builder()
                        .id(content.getId() == null ? null : content.getId().value())
                        .entryId(
                                content.getContentId() == null
                                        ? null
                                        : content.getContentId().value())
                        .question(content.getQuestion())
                        .answer(content.getAnswer())
                        .build();
    }
}
