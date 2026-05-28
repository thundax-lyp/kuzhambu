package com.thundax.kuzhambu.classics.interfaces.admin.content.controller;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentExportJobId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentQaPairId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentTagId;
import com.thundax.kuzhambu.classics.interfaces.admin.content.assembler.ClassicsContentInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.request.ClassicsContentRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.response.ClassicsContentResponse;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "古籍模块-通用内容", description = "通用内容")
@SysLogger(module = {"古籍", "通用内容"})
@RequestMapping("/api/classics/content")
@WrappedApiController
public class ClassicsContentAdminController {
    private final ClassicsContentApplicationService service;

    public ClassicsContentAdminController(ClassicsContentApplicationService service) {
        this.service = service;
    }

    @Operation(summary = "查询古籍内容标签", description = "classics:content:view")
    @ApiImplicitParams({})
    @HasPermission("classics:content:view")
    @SysLogger(value = "标签列表")
    @GetMapping("tags")
    public List<ClassicsContentResponse> listTags(@RequestParam String contentType, @RequestParam Long contentId) {
        ClassicsContentId contentIdValue = ClassicsContentIdCodec.toDomain(contentId);
        return service.listTags(contentType, contentIdValue).stream()
                .map(ClassicsContentInterfaceAssembler::toTagResponse)
                .toList();
    }

    @Operation(summary = "保存古籍内容标签", description = "classics:content:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:content:edit")
    @SysLogger(value = "保存标签")
    @PostMapping("tags/save")
    public ClassicsContentResponse saveTag(@Valid @RequestBody ClassicsContentRequest request) {
        ClassicsContentTagId id = service.saveTag(ClassicsContentInterfaceAssembler.toTagCommand(request));
        return ClassicsContentResponse.builder()
                .id(id == null ? null : id.value())
                .build();
    }

    @Operation(summary = "查询古籍内容问答", description = "classics:content:view")
    @ApiImplicitParams({})
    @HasPermission("classics:content:view")
    @SysLogger(value = "问答列表")
    @GetMapping("qa-pairs")
    public List<ClassicsContentResponse> listQaPairs(@RequestParam String contentType, @RequestParam Long contentId) {
        ClassicsContentId contentIdValue = ClassicsContentIdCodec.toDomain(contentId);
        return service.listQaPairs(contentType, contentIdValue).stream()
                .map(ClassicsContentInterfaceAssembler::toQaResponse)
                .toList();
    }

    @Operation(summary = "保存古籍内容问答", description = "classics:content:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:content:edit")
    @SysLogger(value = "保存问答")
    @PostMapping("qa-pairs/save")
    public ClassicsContentResponse saveQaPair(@Valid @RequestBody ClassicsContentRequest request) {
        ClassicsContentQaPairId id = service.saveQaPair(ClassicsContentInterfaceAssembler.toQaCommand(request));
        return ClassicsContentResponse.builder()
                .id(id == null ? null : id.value())
                .build();
    }

    @Operation(summary = "创建古籍内容导出任务", description = "classics:content:export")
    @ApiImplicitParams({})
    @HasPermission("classics:content:export")
    @SysLogger(value = "创建导出任务")
    @PostMapping("exports")
    public ClassicsContentResponse createExport(@Valid @RequestBody ClassicsContentRequest request) {
        ClassicsContentExportJobId id =
                service.createExportJob(ClassicsContentInterfaceAssembler.toExportCommand(request));
        return ClassicsContentResponse.builder()
                .id(id == null ? null : id.value())
                .build();
    }
}
