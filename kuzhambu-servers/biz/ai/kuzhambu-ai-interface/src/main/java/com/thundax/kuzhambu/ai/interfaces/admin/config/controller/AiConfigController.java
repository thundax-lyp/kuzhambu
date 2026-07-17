package com.thundax.kuzhambu.ai.interfaces.admin.config.controller;

import com.thundax.kuzhambu.ai.application.capability.service.AiCapabilityApplicationService;
import com.thundax.kuzhambu.ai.application.config.model.service.AiModelApplicationService;
import com.thundax.kuzhambu.ai.interfaces.admin.config.assembler.AiConfigInterfaceAssembler;
import com.thundax.kuzhambu.ai.interfaces.admin.config.controller.request.AiConfigRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.config.controller.response.AiConfigResponses.CapabilityResponse;
import com.thundax.kuzhambu.ai.interfaces.admin.config.controller.response.AiConfigResponses.ModelResponse;
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

@Tag(name = "AI模块-配置", description = "AI服务、模型和能力映射")
@SysLogger(module = {"AI", "配置"})
@RequestMapping(value = "/api/ai/config")
@WrappedApiController
public class AiConfigController {

    private final AiModelApplicationService modelService;
    private final AiCapabilityApplicationService capabilityService;

    public AiConfigController(
            AiModelApplicationService modelService, AiCapabilityApplicationService capabilityService) {
        this.modelService = modelService;
        this.capabilityService = capabilityService;
    }

    @Operation(summary = "获取AI模型", description = "ai:config:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:config:view")
    @SysLogger(value = "模型读取")
    @PostMapping(value = "model/get")
    public ModelResponse getModel(@Valid @RequestBody AiConfigRequests.ModelIdRequest request) {
        return AiConfigInterfaceAssembler.toResponse(modelService.get(request.getId()));
    }

    @Operation(summary = "获取AI模型列表", description = "ai:config:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:config:view")
    @SysLogger(value = "模型列表")
    @PostMapping(value = "model/list")
    public List<ModelResponse> listModels(@Valid @RequestBody AiConfigRequests.ModelListRequest request) {
        return modelService.list(request.getApiSource(), request.getEnabled()).stream()
                .map(AiConfigInterfaceAssembler::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "新增AI模型", description = "ai:config:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:config:edit")
    @SysLogger(value = "模型新增")
    @PostMapping(value = "model/create")
    public ModelResponse createModel(@Valid @RequestBody AiConfigRequests.ModelSaveRequest request) {
        Long id = modelService.save(AiConfigInterfaceAssembler.toModel(request));
        return AiConfigInterfaceAssembler.toResponse(modelService.get(id));
    }

    @Operation(summary = "更新AI模型", description = "ai:config:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:config:edit")
    @SysLogger(value = "模型更新")
    @PostMapping(value = "model/update")
    public ModelResponse updateModel(@Valid @RequestBody AiConfigRequests.ModelSaveRequest request) {
        modelService.update(AiConfigInterfaceAssembler.toModel(request));
        return AiConfigInterfaceAssembler.toResponse(modelService.get(request.getId()));
    }

    @Operation(summary = "删除AI模型", description = "ai:config:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:config:edit")
    @SysLogger(value = "模型删除")
    @PostMapping(value = "model/delete")
    public Boolean deleteModel(@Valid @RequestBody AiConfigRequests.ModelIdRequest request) {
        modelService.delete(request.getId());
        return true;
    }

    @Operation(summary = "获取AI能力列表", description = "ai:config:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:config:view")
    @SysLogger(value = "能力列表")
    @PostMapping(value = "capability/list")
    public List<CapabilityResponse> listCapabilities(
            @Valid @RequestBody AiConfigRequests.CapabilityQueryRequest request) {
        return capabilityService.listCapabilities(request.getEnabled()).stream()
                .map(AiConfigInterfaceAssembler::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "获取AI能力", description = "ai:config:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:config:view")
    @SysLogger(value = "能力读取")
    @PostMapping(value = "capability/get")
    public CapabilityResponse getCapability(@Valid @RequestBody AiConfigRequests.CapabilityQueryRequest request) {
        return AiConfigInterfaceAssembler.toResponse(capabilityService.getCapability(request.getCapability()));
    }
}
