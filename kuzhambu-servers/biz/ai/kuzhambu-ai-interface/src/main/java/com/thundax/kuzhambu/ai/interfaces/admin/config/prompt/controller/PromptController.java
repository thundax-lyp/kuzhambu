package com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.controller;

import com.thundax.kuzhambu.ai.application.config.service.AiCapabilityCatalogApplicationService;
import com.thundax.kuzhambu.ai.application.config.service.PromptApplicationService;
import com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.assembler.PromptInterfaceAssembler;
import com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.controller.request.PromptRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.controller.response.PromptResponses.CapabilityVariableResponse;
import com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.controller.response.PromptResponses.TemplateResponse;
import com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.controller.response.PromptResponses.VariableResponse;
import com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.controller.response.PromptResponses.VersionResponse;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import io.swagger.annotations.ApiImplicitParam;
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
@RequestMapping(value = "/api/ai/config/prompt")
@WrappedApiController
public class PromptController {

    private final PromptApplicationService promptService;
    private final AiCapabilityCatalogApplicationService capabilityCatalogService;

    public PromptController(
            PromptApplicationService promptService, AiCapabilityCatalogApplicationService capabilityCatalogService) {
        this.promptService = promptService;
        this.capabilityCatalogService = capabilityCatalogService;
    }

    @Operation(summary = "获取提示词模板", description = "ai:prompt:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:prompt:view")
    @SysLogger(value = "模板读取")
    @PostMapping(value = "template/get")
    public TemplateResponse getTemplate(@Valid @RequestBody PromptRequests.TemplateIdRequest request) {
        return PromptInterfaceAssembler.toResponse(
                promptService.get(PromptInterfaceAssembler.toGetPromptQuery(request)));
    }

    @Operation(summary = "按能力获取提示词模板", description = "ai:prompt:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:prompt:view")
    @SysLogger(value = "模板读取")
    @PostMapping(value = "template/get-by-capability")
    public TemplateResponse getTemplateByCapability(@Valid @RequestBody PromptRequests.TemplateQueryRequest request) {
        return PromptInterfaceAssembler.toResponse(
                promptService.getByCapability(PromptInterfaceAssembler.toGetPromptByCapabilityQuery(request)));
    }

    @Operation(summary = "获取提示词模板列表", description = "ai:prompt:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:prompt:view")
    @SysLogger(value = "模板列表")
    @PostMapping(value = "template/list")
    public List<TemplateResponse> listTemplates(@Valid @RequestBody PromptRequests.TemplateQueryRequest request) {
        return promptService.list(PromptInterfaceAssembler.toListPromptsQuery(request)).stream()
                .map(PromptInterfaceAssembler::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "保存提示词模板", description = "ai:prompt:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:prompt:edit")
    @SysLogger(value = "模板保存")
    @PostMapping(value = "template/save")
    public TemplateResponse saveTemplate(@Valid @RequestBody PromptRequests.TemplateSaveRequest request) {
        var templateId = promptService.save(PromptInterfaceAssembler.toSaveCommand(request));
        return PromptInterfaceAssembler.toResponse(
                promptService.get(PromptInterfaceAssembler.toGetPromptQuery(templateId)));
    }

    @Operation(summary = "更新提示词模板状态", description = "ai:prompt:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:prompt:edit")
    @SysLogger(value = "模板状态更新")
    @PostMapping(value = "template/status/update")
    public Boolean updateTemplateStatus(@Valid @RequestBody PromptRequests.TemplateStatusRequest request) {
        promptService.changeStatus(PromptInterfaceAssembler.toChangeStatusCommand(request));
        return true;
    }

    @Operation(summary = "删除提示词模板", description = "ai:prompt:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:prompt:edit")
    @SysLogger(value = "模板删除")
    @PostMapping(value = "template/delete")
    public Boolean deleteTemplate(@Valid @RequestBody PromptRequests.TemplateIdRequest request) {
        promptService.delete(PromptInterfaceAssembler.toDeleteCommand(request));
        return true;
    }

    @Operation(summary = "获取当前提示词版本", description = "ai:prompt:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:prompt:view")
    @SysLogger(value = "当前版本读取")
    @PostMapping(value = "version/current")
    public VersionResponse getCurrentVersion(@Valid @RequestBody PromptRequests.TemplateIdRequest request) {
        return PromptInterfaceAssembler.toResponse(
                promptService.getCurrentVersion(PromptInterfaceAssembler.toGetCurrentPromptVersionQuery(request)));
    }

    @Operation(summary = "获取提示词版本列表", description = "ai:prompt:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:prompt:view")
    @SysLogger(value = "版本列表")
    @PostMapping(value = "version/list")
    public List<VersionResponse> listVersions(@Valid @RequestBody PromptRequests.TemplateIdRequest request) {
        return promptService.listVersions(PromptInterfaceAssembler.toListPromptVersionsQuery(request)).stream()
                .map(PromptInterfaceAssembler::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "对比提示词版本", description = "ai:prompt:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:prompt:view")
    @SysLogger(value = "版本对比")
    @PostMapping(value = "version/compare")
    public List<VersionResponse> compareVersions(@Valid @RequestBody PromptRequests.VersionCompareRequest request) {
        return promptService.compareVersions(PromptInterfaceAssembler.toCompareQuery(request)).stream()
                .map(PromptInterfaceAssembler::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "回滚提示词版本", description = "ai:prompt:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:prompt:edit")
    @SysLogger(value = "版本回滚")
    @PostMapping(value = "version/rollback")
    public VersionResponse rollbackVersion(@Valid @RequestBody PromptRequests.VersionRollbackRequest request) {
        return PromptInterfaceAssembler.toResponse(
                promptService.rollback(PromptInterfaceAssembler.toRollbackPromptVersionCommand(request)));
    }

    @Operation(summary = "获取提示词变量列表", description = "ai:prompt:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:prompt:view")
    @SysLogger(value = "变量列表")
    @PostMapping(value = "variable/list")
    public List<VariableResponse> listVariables(@Valid @RequestBody PromptRequests.TemplateIdRequest request) {
        return promptService.listVariables(PromptInterfaceAssembler.toListPromptVariablesQuery(request)).stream()
                .map(PromptInterfaceAssembler::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "获取能力变量目录", description = "ai:prompt:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:prompt:view")
    @SysLogger(value = "能力变量目录")
    @PostMapping(value = "capability-variable/list")
    public List<CapabilityVariableResponse> listCapabilityVariables(
            @Valid @RequestBody PromptRequests.CapabilityVariableListRequest request) {
        return capabilityCatalogService
                .listPromptVariables(PromptInterfaceAssembler.toListCapabilityVariablesQuery(request))
                .stream()
                .map(PromptInterfaceAssembler::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "校验提示词必填变量", description = "ai:prompt:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:prompt:view")
    @SysLogger(value = "变量校验")
    @PostMapping(value = "variable/validate")
    public Boolean validateVariables(@Valid @RequestBody PromptRequests.VariableValidateRequest request) {
        promptService.validateRequiredVariables(PromptInterfaceAssembler.toValidatePromptVariablesCommand(request));
        return true;
    }

    @Operation(summary = "构建提示词优化建议", description = "ai:prompt:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:prompt:edit")
    @SysLogger(value = "优化建议")
    @PostMapping(value = "optimization/suggest")
    public VersionResponse buildOptimizationSuggestion(@Valid @RequestBody PromptRequests.OptimizationRequest request) {
        return PromptInterfaceAssembler.toResponse(promptService.buildOptimizationSuggestion(
                PromptInterfaceAssembler.toBuildOptimizationSuggestionCommand(request)));
    }
}
