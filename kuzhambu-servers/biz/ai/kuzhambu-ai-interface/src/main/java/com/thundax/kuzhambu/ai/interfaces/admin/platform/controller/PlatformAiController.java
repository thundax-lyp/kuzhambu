package com.thundax.kuzhambu.ai.interfaces.admin.platform.controller;

import com.thundax.kuzhambu.ai.application.platform.service.PlatformAiApplicationService;
import com.thundax.kuzhambu.ai.interfaces.admin.platform.assembler.PlatformAiInterfaceAssembler;
import com.thundax.kuzhambu.ai.interfaces.admin.platform.controller.request.PlatformAiRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.platform.controller.response.PlatformAiResponses.InvokeResponse;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "AI模块-平台能力", description = "AI平台提示词建议和版本摘要")
@SysLogger(module = {"AI", "平台能力"})
@RequestMapping(value = "/api/ai/platform")
@WrappedApiController
public class PlatformAiController {

    private final PlatformAiApplicationService platformAiApplicationService;

    public PlatformAiController(PlatformAiApplicationService platformAiApplicationService) {
        this.platformAiApplicationService = platformAiApplicationService;
    }

    @Operation(summary = "生成提示词优化建议", description = "ai:prompt:edit")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:prompt:edit")
    @SysLogger(value = "提示词建议")
    @PostMapping(value = "prompt-suggestion")
    public InvokeResponse buildPromptSuggestion(@Valid @RequestBody PlatformAiRequests.InvokeRequest request) {
        return PlatformAiInterfaceAssembler.toResponse(
                platformAiApplicationService.buildPromptSuggestion(PlatformAiInterfaceAssembler.toCommand(request)));
    }

    @Operation(summary = "生成版本摘要", description = "ai:prompt:view")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:prompt:view")
    @SysLogger(value = "版本摘要")
    @PostMapping(value = "version-summary")
    public InvokeResponse summarizeVersion(@Valid @RequestBody PlatformAiRequests.InvokeRequest request) {
        return PlatformAiInterfaceAssembler.toResponse(
                platformAiApplicationService.summarizeVersion(PlatformAiInterfaceAssembler.toCommand(request)));
    }
}
