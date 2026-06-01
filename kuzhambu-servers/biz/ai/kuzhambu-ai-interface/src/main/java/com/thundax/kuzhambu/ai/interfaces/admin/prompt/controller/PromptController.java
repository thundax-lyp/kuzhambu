package com.thundax.kuzhambu.ai.interfaces.admin.prompt.controller;

import com.thundax.kuzhambu.ai.application.prompt.service.PromptApplicationService;
import com.thundax.kuzhambu.ai.interfaces.admin.prompt.assembler.PromptInterfaceAssembler;
import com.thundax.kuzhambu.ai.interfaces.admin.prompt.controller.request.PromptRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.prompt.controller.response.PromptResponses.TemplateResponse;
import com.thundax.kuzhambu.ai.interfaces.admin.prompt.controller.response.PromptResponses.VariableResponse;
import com.thundax.kuzhambu.ai.interfaces.admin.prompt.controller.response.PromptResponses.VersionResponse;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "AI模块-提示词", description = "AI提示词模板和版本")
@SysLogger(module = {"AI", "提示词"})
@RequestMapping(value = "/api/ai/prompt")
@WrappedApiController
public class PromptController {

    private final PromptApplicationService promptService;

    public PromptController(PromptApplicationService promptService) {
        this.promptService = promptService;
    }

    @Operation(summary = "获取提示词模板", description = "ai:prompt:view")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:prompt:view")
    @SysLogger(value = "模板读取")
    @PostMapping(value = "template/get")
    public TemplateResponse getTemplate(@Valid @RequestBody PromptRequests.TemplateIdRequest request) {
        return PromptInterfaceAssembler.toResponse(promptService.getTemplate(request.getTemplateId()));
    }

    @Operation(summary = "按范围和能力获取提示词模板", description = "ai:prompt:view")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:prompt:view")
    @SysLogger(value = "模板读取")
    @PostMapping(value = "template/get-by-scope")
    public TemplateResponse getTemplateByScope(@Valid @RequestBody PromptRequests.TemplateQueryRequest request) {
        return PromptInterfaceAssembler.toResponse(
                promptService.getTemplate(request.getScope(), request.getCapability()));
    }

    @Operation(summary = "保存提示词模板", description = "ai:prompt:edit")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:prompt:edit")
    @SysLogger(value = "模板保存")
    @PostMapping(value = "template/save")
    public TemplateResponse saveTemplate(@Valid @RequestBody PromptRequests.TemplateSaveRequest request) {
        Long templateId = promptService.saveTemplate(PromptInterfaceAssembler.toSaveCommand(request));
        return PromptInterfaceAssembler.toResponse(promptService.getTemplate(templateId));
    }

    @Operation(summary = "获取当前提示词版本", description = "ai:prompt:view")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:prompt:view")
    @SysLogger(value = "当前版本读取")
    @PostMapping(value = "version/current")
    public VersionResponse getCurrentVersion(@Valid @RequestBody PromptRequests.TemplateIdRequest request) {
        return PromptInterfaceAssembler.toResponse(promptService.getCurrentVersion(request.getTemplateId()));
    }

    @Operation(summary = "获取提示词版本列表", description = "ai:prompt:view")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:prompt:view")
    @SysLogger(value = "版本列表")
    @PostMapping(value = "version/list")
    public List<VersionResponse> listVersions(@Valid @RequestBody PromptRequests.TemplateIdRequest request) {
        return promptService.listVersions(request.getTemplateId()).stream()
                .map(PromptInterfaceAssembler::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "对比提示词版本", description = "ai:prompt:view")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:prompt:view")
    @SysLogger(value = "版本对比")
    @PostMapping(value = "version/compare")
    public List<VersionResponse> compareVersions(@Valid @RequestBody PromptRequests.VersionCompareRequest request) {
        return promptService.compareVersions(PromptInterfaceAssembler.toCompareQuery(request)).stream()
                .map(PromptInterfaceAssembler::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "回滚提示词版本", description = "ai:prompt:edit")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:prompt:edit")
    @SysLogger(value = "版本回滚")
    @PostMapping(value = "version/rollback")
    public VersionResponse rollbackVersion(@Valid @RequestBody PromptRequests.VersionRollbackRequest request) {
        return PromptInterfaceAssembler.toResponse(
                promptService.rollback(request.getTemplateId(), request.getVersionNo()));
    }

    @Operation(summary = "获取提示词变量列表", description = "ai:prompt:view")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:prompt:view")
    @SysLogger(value = "变量列表")
    @PostMapping(value = "variable/list")
    public List<VariableResponse> listVariables(@Valid @RequestBody PromptRequests.TemplateIdRequest request) {
        return promptService.listVariables(request.getTemplateId()).stream()
                .map(PromptInterfaceAssembler::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "校验提示词必填变量", description = "ai:prompt:view")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:prompt:view")
    @SysLogger(value = "变量校验")
    @PostMapping(value = "variable/validate")
    public Boolean validateVariables(@Valid @RequestBody PromptRequests.VariableValidateRequest request) {
        promptService.validateRequiredVariables(request.getTemplateId(), request.getProvidedNames());
        return true;
    }

    @Operation(summary = "构建提示词优化建议", description = "ai:prompt:edit")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:prompt:edit")
    @SysLogger(value = "优化建议")
    @PostMapping(value = "optimization/suggest")
    public VersionResponse buildOptimizationSuggestion(@Valid @RequestBody PromptRequests.OptimizationRequest request) {
        return PromptInterfaceAssembler.toResponse(
                promptService.buildOptimizationSuggestion(request.getTemplateId(), request.getChangeSummary()));
    }
}
