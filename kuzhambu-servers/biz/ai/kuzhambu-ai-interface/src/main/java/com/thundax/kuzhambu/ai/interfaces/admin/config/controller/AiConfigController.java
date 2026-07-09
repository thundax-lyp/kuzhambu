package com.thundax.kuzhambu.ai.interfaces.admin.config.controller;

import com.thundax.kuzhambu.ai.application.capability.service.AiCapabilityApplicationService;
import com.thundax.kuzhambu.ai.application.config.service.AiServiceConfigApplicationService;
import com.thundax.kuzhambu.ai.application.model.service.AiModelApplicationService;
import com.thundax.kuzhambu.ai.interfaces.admin.config.assembler.AiConfigInterfaceAssembler;
import com.thundax.kuzhambu.ai.interfaces.admin.config.controller.request.AiConfigRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.config.controller.response.AiConfigResponses.ActionStatusResponse;
import com.thundax.kuzhambu.ai.interfaces.admin.config.controller.response.AiConfigResponses.CapabilityMappingResponse;
import com.thundax.kuzhambu.ai.interfaces.admin.config.controller.response.AiConfigResponses.CapabilityResponse;
import com.thundax.kuzhambu.ai.interfaces.admin.config.controller.response.AiConfigResponses.IdResponse;
import com.thundax.kuzhambu.ai.interfaces.admin.config.controller.response.AiConfigResponses.ModelCheckRecordResponse;
import com.thundax.kuzhambu.ai.interfaces.admin.config.controller.response.AiConfigResponses.ModelResponse;
import com.thundax.kuzhambu.ai.interfaces.admin.config.controller.response.AiConfigResponses.ServiceConfigResponse;
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

@Tag(name = "AI模块-配置", description = "AI服务、模型和能力映射")
@SysLogger(module = {"AI", "配置"})
@RequestMapping(value = "/api/ai/config")
@WrappedApiController
public class AiConfigController {

    private final AiServiceConfigApplicationService serviceConfigService;
    private final AiModelApplicationService modelService;
    private final AiCapabilityApplicationService capabilityService;

    public AiConfigController(
            AiServiceConfigApplicationService serviceConfigService,
            AiModelApplicationService modelService,
            AiCapabilityApplicationService capabilityService) {
        this.serviceConfigService = serviceConfigService;
        this.modelService = modelService;
        this.capabilityService = capabilityService;
    }

    @Operation(summary = "按服务ID获取AI服务配置", description = "ai:config:view")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:config:view")
    @SysLogger(value = "服务配置读取")
    @PostMapping(value = "service/get")
    public ServiceConfigResponse getService(@Valid @RequestBody AiConfigRequests.ServiceIdRequest request) {
        return AiConfigInterfaceAssembler.toResponse(serviceConfigService.getByServiceId(request.getServiceId()));
    }

    @Operation(summary = "按角色获取AI服务配置", description = "ai:config:view")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:config:view")
    @SysLogger(value = "服务配置读取")
    @PostMapping(value = "service/get-by-role")
    public ServiceConfigResponse getServiceByRole(@Valid @RequestBody AiConfigRequests.ServiceRoleRequest request) {
        return AiConfigInterfaceAssembler.toResponse(serviceConfigService.getByRole(request.getServiceRole()));
    }

    @Operation(summary = "保存AI服务配置", description = "ai:config:edit")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:config:edit")
    @SysLogger(value = "服务配置保存")
    @PostMapping(value = "service/save")
    public ServiceConfigResponse saveService(@Valid @RequestBody AiConfigRequests.ServiceConfigSaveRequest request) {
        Long serviceId = serviceConfigService.save(AiConfigInterfaceAssembler.toServiceConfig(request));
        return AiConfigInterfaceAssembler.toResponse(serviceConfigService.getByServiceId(serviceId));
    }

    @Operation(summary = "获取AI模型", description = "ai:config:view")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:config:view")
    @SysLogger(value = "模型读取")
    @PostMapping(value = "model/get")
    public ModelResponse getModel(@Valid @RequestBody AiConfigRequests.ModelIdRequest request) {
        return AiConfigInterfaceAssembler.toResponse(modelService.get(request.getModelId()));
    }

    @Operation(summary = "获取AI模型列表", description = "ai:config:view")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:config:view")
    @SysLogger(value = "模型列表")
    @PostMapping(value = "model/list")
    public List<ModelResponse> listModels(@Valid @RequestBody AiConfigRequests.ModelListRequest request) {
        return modelService.list(request.getServiceId(), request.getEnabled()).stream()
                .map(AiConfigInterfaceAssembler::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "新增AI模型", description = "ai:config:edit")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:config:edit")
    @SysLogger(value = "模型新增")
    @PostMapping(value = "model/create")
    public ModelResponse createModel(@Valid @RequestBody AiConfigRequests.ModelSaveRequest request) {
        Long modelId = modelService.save(AiConfigInterfaceAssembler.toModel(request));
        return AiConfigInterfaceAssembler.toResponse(modelService.get(modelId));
    }

    @Operation(summary = "更新AI模型", description = "ai:config:edit")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:config:edit")
    @SysLogger(value = "模型更新")
    @PostMapping(value = "model/update")
    public ModelResponse updateModel(@Valid @RequestBody AiConfigRequests.ModelSaveRequest request) {
        modelService.update(AiConfigInterfaceAssembler.toModel(request));
        return AiConfigInterfaceAssembler.toResponse(modelService.get(request.getModelId()));
    }

    @Operation(summary = "删除AI模型", description = "ai:config:edit")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:config:edit")
    @SysLogger(value = "模型删除")
    @PostMapping(value = "model/delete")
    public Boolean deleteModel(@Valid @RequestBody AiConfigRequests.ModelIdRequest request) {
        capabilityService.assertModelCanBeDeleted(request.getModelId());
        modelService.delete(request.getModelId());
        return true;
    }

    @Operation(summary = "检测AI模型", description = "ai:config:edit")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:config:edit")
    @SysLogger(value = "模型检测")
    @PostMapping(value = "model/check")
    public ModelCheckRecordResponse checkModel(@Valid @RequestBody AiConfigRequests.ModelIdRequest request) {
        return AiConfigInterfaceAssembler.toResponse(modelService.check(request.getModelId()));
    }

    @Operation(summary = "记录AI模型检测结果", description = "ai:config:edit")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:config:edit")
    @SysLogger(value = "模型检测记录")
    @PostMapping(value = "model/check-record")
    public IdResponse recordModelCheck(@Valid @RequestBody AiConfigRequests.ModelCheckRecordRequest request) {
        return IdResponse.builder()
                .id(modelService.recordCheck(AiConfigInterfaceAssembler.toCheckCommand(request)))
                .build();
    }

    @Operation(summary = "获取AI模型检测历史", description = "ai:config:view")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:config:view")
    @SysLogger(value = "模型检测历史")
    @PostMapping(value = "model/check-records")
    public List<ModelCheckRecordResponse> listModelCheckRecords(
            @Valid @RequestBody AiConfigRequests.ModelIdRequest request) {
        return modelService.listCheckRecords(request.getModelId()).stream()
                .map(AiConfigInterfaceAssembler::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "获取AI能力列表", description = "ai:config:view")
    @ApiImplicitParams({})
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
    @ApiImplicitParams({})
    @HasPermission(value = "ai:config:view")
    @SysLogger(value = "能力读取")
    @PostMapping(value = "capability/get")
    public CapabilityResponse getCapability(@Valid @RequestBody AiConfigRequests.CapabilityQueryRequest request) {
        return AiConfigInterfaceAssembler.toResponse(capabilityService.getCapability(request.getCapability()));
    }

    @Operation(summary = "获取AI能力映射", description = "ai:config:view")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:config:view")
    @SysLogger(value = "能力映射读取")
    @PostMapping(value = "capability/mapping/get")
    public CapabilityMappingResponse getMapping(@Valid @RequestBody AiConfigRequests.CapabilityQueryRequest request) {
        return AiConfigInterfaceAssembler.toResponse(
                capabilityService.getMapping(request.getScope(), request.getCapability()));
    }

    @Operation(summary = "保存AI能力映射", description = "ai:config:edit")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:config:edit")
    @SysLogger(value = "能力映射保存")
    @PostMapping(value = "capability/mapping/save")
    public IdResponse saveMapping(@Valid @RequestBody AiConfigRequests.CapabilityMappingSaveRequest request) {
        return IdResponse.builder()
                .id(capabilityService.saveMapping(AiConfigInterfaceAssembler.toMappingCommand(request)))
                .build();
    }

    @Operation(summary = "获取AI动作状态", description = "ai:config:view")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:config:view")
    @SysLogger(value = "动作状态读取")
    @PostMapping(value = "action/status")
    public ActionStatusResponse getActionStatus(@Valid @RequestBody AiConfigRequests.CapabilityQueryRequest request) {
        return AiConfigInterfaceAssembler.toResponse(
                capabilityService.getActionStatus(request.getScope(), request.getCapability()));
    }

    @Operation(summary = "获取AI动作状态列表", description = "ai:config:view")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:config:view")
    @SysLogger(value = "动作状态列表")
    @PostMapping(value = "action/status/list")
    public List<ActionStatusResponse> listActionStatuses(
            @Valid @RequestBody AiConfigRequests.ActionStatusListRequest request) {
        return capabilityService
                .listActionStatuses(request.getScope(), request.getCapability(), request.getAvailable())
                .stream()
                .map(AiConfigInterfaceAssembler::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "刷新AI动作状态", description = "ai:config:edit")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:config:edit")
    @SysLogger(value = "动作状态刷新")
    @PostMapping(value = "action/status/refresh")
    public ActionStatusResponse refreshActionStatus(
            @Valid @RequestBody AiConfigRequests.CapabilityQueryRequest request) {
        return AiConfigInterfaceAssembler.toResponse(
                capabilityService.refreshActionStatus(request.getScope(), request.getCapability()));
    }
}
