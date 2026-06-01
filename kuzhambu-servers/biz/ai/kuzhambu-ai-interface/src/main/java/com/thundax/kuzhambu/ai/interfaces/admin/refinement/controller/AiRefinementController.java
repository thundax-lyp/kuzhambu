package com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller;

import com.thundax.kuzhambu.ai.application.refinement.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.application.refinement.result.AiCandidateResult;
import com.thundax.kuzhambu.ai.application.refinement.service.AiRefinementApplicationService;
import com.thundax.kuzhambu.ai.interfaces.admin.refinement.assembler.AiRefinementInterfaceAssembler;
import com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.request.AiRefinementRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.response.AiRefinementResponses;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.function.Function;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "AI模块-精修", description = "Classics内容AI精修")
@SysLogger(module = {"AI", "精修"})
@RequestMapping(value = "/api/ai/refinement")
@WrappedApiController
public class AiRefinementController {

    private final AiRefinementApplicationService refinementService;

    public AiRefinementController(AiRefinementApplicationService refinementService) {
        this.refinementService = refinementService;
    }

    @Operation(summary = "AI翻译", description = "ai:refinement:edit")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "翻译")
    @PostMapping(value = "translate")
    public AiRefinementResponses.CandidateResultResponse translate(
            @Valid @RequestBody AiRefinementRequests.RefinementRequest request) {
        return invoke(request, refinementService::translate);
    }

    @Operation(summary = "AI摘要", description = "ai:refinement:edit")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "摘要")
    @PostMapping(value = "summary")
    public AiRefinementResponses.CandidateResultResponse summarize(
            @Valid @RequestBody AiRefinementRequests.RefinementRequest request) {
        return invoke(request, refinementService::summarize);
    }

    @Operation(summary = "AI标签", description = "ai:refinement:edit")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "标签")
    @PostMapping(value = "tags")
    public AiRefinementResponses.CandidateResultResponse generateTags(
            @Valid @RequestBody AiRefinementRequests.RefinementRequest request) {
        return invoke(request, refinementService::generateTags);
    }

    @Operation(summary = "AI问答对", description = "ai:refinement:edit")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "问答")
    @PostMapping(value = "qa")
    public AiRefinementResponses.CandidateResultResponse generateQa(
            @Valid @RequestBody AiRefinementRequests.RefinementRequest request) {
        return invoke(request, refinementService::generateQa);
    }

    @Operation(summary = "AI图片理解", description = "ai:refinement:edit")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "图片理解")
    @PostMapping(value = "image-analysis")
    public AiRefinementResponses.CandidateResultResponse analyzeImage(
            @Valid @RequestBody AiRefinementRequests.RefinementRequest request) {
        return invoke(request, refinementService::analyzeImage);
    }

    @Operation(summary = "AI视觉描述", description = "ai:refinement:edit")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "视觉描述")
    @PostMapping(value = "visual")
    public AiRefinementResponses.CandidateResultResponse describeVisual(
            @Valid @RequestBody AiRefinementRequests.RefinementRequest request) {
        return invoke(request, refinementService::describeVisual);
    }

    @Operation(summary = "AI条目拆分", description = "ai:refinement:edit")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "条目拆分")
    @PostMapping(value = "split")
    public AiRefinementResponses.CandidateResultResponse splitEntry(
            @Valid @RequestBody AiRefinementRequests.RefinementRequest request) {
        return invoke(request, refinementService::splitEntry);
    }

    private AiRefinementResponses.CandidateResultResponse invoke(
            AiRefinementRequests.RefinementRequest request,
            Function<AiRefinementRequestCommand, AiCandidateResult> invocation) {
        return AiRefinementInterfaceAssembler.toResponse(
                invocation.apply(AiRefinementInterfaceAssembler.toCommand(request)));
    }
}
