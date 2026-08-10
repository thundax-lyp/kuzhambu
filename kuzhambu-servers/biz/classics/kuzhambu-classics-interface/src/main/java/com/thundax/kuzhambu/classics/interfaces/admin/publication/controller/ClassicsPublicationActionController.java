package com.thundax.kuzhambu.classics.interfaces.admin.publication.controller;

import com.thundax.kuzhambu.classics.application.publication.command.ClassicsPublicationBatchCreateCommand;
import com.thundax.kuzhambu.classics.application.publication.service.ClassicsPublicationApplicationService;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobType;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.assembler.ClassicsPublicationInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.request.ClassicsPublicationActionRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.request.ClassicsPublicationBatchActionRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.response.ClassicsPublicationBatchResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.response.ClassicsPublicationCreateResponse;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "古籍模块-发布动作", description = "三才图会、王圻与明代习俗发布动作")
@SysLogger(module = {"古籍", "发布"})
@RequestMapping("/api/classics/publication")
@WrappedApiController
public class ClassicsPublicationActionController {
    private final ClassicsPublicationApplicationService publicationService;

    public ClassicsPublicationActionController(ClassicsPublicationApplicationService publicationService) {
        this.publicationService = publicationService;
    }

    @HasPermission("classics:sancai:edit")
    @Operation(summary = "发布三才图会条目")
    @ApiImplicitParams(@ApiImplicitParam(name = AccessTokenNames.HEADER_TOKEN, paramType = "header"))
    @SysLogger("三才图会条目发布")
    @PostMapping("sancai/entries/publish")
    public ClassicsPublicationCreateResponse publishSancai(
            @Valid @RequestBody ClassicsPublicationActionRequest request) {
        return action(request, ClassicsContentType.SANCAI_ENTRY, ClassicsPublicationJobType.PUBLISH);
    }

    @HasPermission("classics:sancai:edit")
    @Operation(summary = "下线三才图会条目")
    @ApiImplicitParams(@ApiImplicitParam(name = AccessTokenNames.HEADER_TOKEN, paramType = "header"))
    @SysLogger("三才图会条目下线")
    @PostMapping("sancai/entries/offline")
    public ClassicsPublicationCreateResponse offlineSancai(
            @Valid @RequestBody ClassicsPublicationActionRequest request) {
        return action(request, ClassicsContentType.SANCAI_ENTRY, ClassicsPublicationJobType.OFFLINE);
    }

    @HasPermission("classics:sancai:edit")
    @Operation(summary = "批量发布三才图会条目")
    @ApiImplicitParams(@ApiImplicitParam(name = AccessTokenNames.HEADER_TOKEN, paramType = "header"))
    @SysLogger("三才图会条目批量发布")
    @PostMapping("sancai/entries/batch/publish")
    public ClassicsPublicationBatchResponse publishSancaiBatch(
            @Valid @RequestBody ClassicsPublicationBatchActionRequest request) {
        return batchAction(request, ClassicsContentType.SANCAI_ENTRY, ClassicsPublicationJobType.PUBLISH);
    }

    @HasPermission("classics:sancai:edit")
    @Operation(summary = "批量下线三才图会条目")
    @ApiImplicitParams(@ApiImplicitParam(name = AccessTokenNames.HEADER_TOKEN, paramType = "header"))
    @SysLogger("三才图会条目批量下线")
    @PostMapping("sancai/entries/batch/offline")
    public ClassicsPublicationBatchResponse offlineSancaiBatch(
            @Valid @RequestBody ClassicsPublicationBatchActionRequest request) {
        return batchAction(request, ClassicsContentType.SANCAI_ENTRY, ClassicsPublicationJobType.OFFLINE);
    }

    @HasPermission("classics:wangqi:edit")
    @Operation(summary = "发布王圻文档")
    @ApiImplicitParams(@ApiImplicitParam(name = AccessTokenNames.HEADER_TOKEN, paramType = "header"))
    @SysLogger("王圻文档发布")
    @PostMapping("wangqi/documents/publish")
    public ClassicsPublicationCreateResponse publishWangqi(
            @Valid @RequestBody ClassicsPublicationActionRequest request) {
        return action(request, ClassicsContentType.WANGQI_DOCUMENT, ClassicsPublicationJobType.PUBLISH);
    }

    @HasPermission("classics:wangqi:edit")
    @Operation(summary = "下线王圻文档")
    @ApiImplicitParams(@ApiImplicitParam(name = AccessTokenNames.HEADER_TOKEN, paramType = "header"))
    @SysLogger("王圻文档下线")
    @PostMapping("wangqi/documents/offline")
    public ClassicsPublicationCreateResponse offlineWangqi(
            @Valid @RequestBody ClassicsPublicationActionRequest request) {
        return action(request, ClassicsContentType.WANGQI_DOCUMENT, ClassicsPublicationJobType.OFFLINE);
    }

    @HasPermission("classics:wangqi:edit")
    @Operation(summary = "批量发布王圻文档")
    @ApiImplicitParams(@ApiImplicitParam(name = AccessTokenNames.HEADER_TOKEN, paramType = "header"))
    @SysLogger("王圻文档批量发布")
    @PostMapping("wangqi/documents/batch/publish")
    public ClassicsPublicationBatchResponse publishWangqiBatch(
            @Valid @RequestBody ClassicsPublicationBatchActionRequest request) {
        return batchAction(request, ClassicsContentType.WANGQI_DOCUMENT, ClassicsPublicationJobType.PUBLISH);
    }

    @HasPermission("classics:wangqi:edit")
    @Operation(summary = "批量下线王圻文档")
    @ApiImplicitParams(@ApiImplicitParam(name = AccessTokenNames.HEADER_TOKEN, paramType = "header"))
    @SysLogger("王圻文档批量下线")
    @PostMapping("wangqi/documents/batch/offline")
    public ClassicsPublicationBatchResponse offlineWangqiBatch(
            @Valid @RequestBody ClassicsPublicationBatchActionRequest request) {
        return batchAction(request, ClassicsContentType.WANGQI_DOCUMENT, ClassicsPublicationJobType.OFFLINE);
    }

    @HasPermission("classics:mingcustoms:edit")
    @Operation(summary = "发布明代习俗")
    @ApiImplicitParams(@ApiImplicitParam(name = AccessTokenNames.HEADER_TOKEN, paramType = "header"))
    @SysLogger("明代习俗发布")
    @PostMapping("ming-customs/publish")
    public ClassicsPublicationCreateResponse publishMingCustoms(
            @Valid @RequestBody ClassicsPublicationActionRequest request) {
        return action(request, ClassicsContentType.MING_CUSTOMS, ClassicsPublicationJobType.PUBLISH);
    }

    @HasPermission("classics:mingcustoms:edit")
    @Operation(summary = "下线明代习俗")
    @ApiImplicitParams(@ApiImplicitParam(name = AccessTokenNames.HEADER_TOKEN, paramType = "header"))
    @SysLogger("明代习俗下线")
    @PostMapping("ming-customs/offline")
    public ClassicsPublicationCreateResponse offlineMingCustoms(
            @Valid @RequestBody ClassicsPublicationActionRequest request) {
        return action(request, ClassicsContentType.MING_CUSTOMS, ClassicsPublicationJobType.OFFLINE);
    }

    @HasPermission("classics:mingcustoms:edit")
    @Operation(summary = "批量发布明代习俗")
    @ApiImplicitParams(@ApiImplicitParam(name = AccessTokenNames.HEADER_TOKEN, paramType = "header"))
    @SysLogger("明代习俗批量发布")
    @PostMapping("ming-customs/batch/publish")
    public ClassicsPublicationBatchResponse publishMingCustomsBatch(
            @Valid @RequestBody ClassicsPublicationBatchActionRequest request) {
        return batchAction(request, ClassicsContentType.MING_CUSTOMS, ClassicsPublicationJobType.PUBLISH);
    }

    @HasPermission("classics:mingcustoms:edit")
    @Operation(summary = "批量下线明代习俗")
    @ApiImplicitParams(@ApiImplicitParam(name = AccessTokenNames.HEADER_TOKEN, paramType = "header"))
    @SysLogger("明代习俗批量下线")
    @PostMapping("ming-customs/batch/offline")
    public ClassicsPublicationBatchResponse offlineMingCustomsBatch(
            @Valid @RequestBody ClassicsPublicationBatchActionRequest request) {
        return batchAction(request, ClassicsContentType.MING_CUSTOMS, ClassicsPublicationJobType.OFFLINE);
    }

    private ClassicsPublicationCreateResponse action(
            ClassicsPublicationActionRequest request,
            ClassicsContentType contentType,
            ClassicsPublicationJobType jobType) {
        return ClassicsPublicationInterfaceAssembler.toResponse(publicationService.create(
                ClassicsPublicationInterfaceAssembler.toCommand(request, contentType, jobType)));
    }

    private ClassicsPublicationBatchResponse batchAction(
            ClassicsPublicationBatchActionRequest request,
            ClassicsContentType contentType,
            ClassicsPublicationJobType jobType) {
        return ClassicsPublicationInterfaceAssembler.toBatchResponse(
                publicationService.createBatch(new ClassicsPublicationBatchCreateCommand(
                        ClassicsPublicationInterfaceAssembler.toCommands(request, contentType, jobType))));
    }
}
