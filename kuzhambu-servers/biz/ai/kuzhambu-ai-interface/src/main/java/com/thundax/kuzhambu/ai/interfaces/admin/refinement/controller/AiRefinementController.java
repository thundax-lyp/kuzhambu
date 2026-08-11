package com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller;

import com.thundax.kuzhambu.ai.application.scenario.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.application.scenario.result.AiCandidateResult;
import com.thundax.kuzhambu.ai.application.scenario.service.AiRefinementApplicationService;
import com.thundax.kuzhambu.ai.interfaces.admin.refinement.assembler.AiRefinementInterfaceAssembler;
import com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.request.AiRefinementRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.response.AiRefinementResponses;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import io.swagger.annotations.ApiImplicitParam;
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

    private static final String CAPABILITY_TRANSLATE = "CLASSICS_TRANSLATE";
    private static final String CAPABILITY_SUMMARY = "CLASSICS_SUMMARY";
    private static final String CAPABILITY_TAGS = "CLASSICS_TAG_EXTRACT";
    private static final String CAPABILITY_QA = "CLASSICS_QA";
    private static final String CAPABILITY_IMAGE_ANALYSIS = "CLASSICS_IMAGE_DESCRIBE";
    private static final String CAPABILITY_FUSION = "CLASSICS_IMAGE_PROMPT_FUSION";
    private static final String CAPABILITY_VISUAL = "CLASSICS_VISUAL_DESCRIBE";
    private static final String CAPABILITY_IMAGE_GEN = "CLASSICS_IMAGE_GENERATE";
    private static final String CAPABILITY_SPLIT = "CLASSICS_SPLIT";

    private final AiRefinementApplicationService refinementService;

    public AiRefinementController(AiRefinementApplicationService refinementService) {
        this.refinementService = refinementService;
    }

    @Operation(summary = "AI翻译", description = "ai:refinement:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "翻译")
    @PostMapping(value = "translation/create")
    public AiRefinementResponses.CandidateResultResponse createTranslation(
            @Valid @RequestBody AiRefinementRequests.RefinementRequest request) {
        return invoke(request, CAPABILITY_TRANSLATE, refinementService::translate);
    }

    @Operation(summary = "AI摘要", description = "ai:refinement:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "摘要")
    @PostMapping(value = "summary/create")
    public AiRefinementResponses.CandidateResultResponse createSummary(
            @Valid @RequestBody AiRefinementRequests.RefinementRequest request) {
        return invoke(request, CAPABILITY_SUMMARY, refinementService::summarize);
    }

    @Operation(summary = "AI标签", description = "ai:refinement:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "标签")
    @PostMapping(value = "tags/create")
    public AiRefinementResponses.CandidateResultResponse createTags(
            @Valid @RequestBody AiRefinementRequests.RefinementRequest request) {
        return invoke(request, CAPABILITY_TAGS, refinementService::generateTags);
    }

    @Operation(summary = "AI问答对", description = "ai:refinement:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "问答")
    @PostMapping(value = "qa/create")
    public AiRefinementResponses.CandidateResultResponse createQa(
            @Valid @RequestBody AiRefinementRequests.RefinementRequest request) {
        return invoke(request, CAPABILITY_QA, refinementService::generateQa);
    }

    @Operation(summary = "AI图片理解", description = "ai:refinement:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "图片理解")
    @PostMapping(value = "image-analysis/create")
    public AiRefinementResponses.CandidateResultResponse createImageAnalysis(
            @Valid @RequestBody AiRefinementRequests.RefinementRequest request) {
        return invoke(request, CAPABILITY_IMAGE_ANALYSIS, refinementService::analyzeImage);
    }

    @Operation(summary = "AI信息融合", description = "ai:refinement:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "信息融合")
    @PostMapping(value = "visual-fusion/create")
    public AiRefinementResponses.CandidateResultResponse createVisualFusion(
            @Valid @RequestBody AiRefinementRequests.RefinementRequest request) {
        return invoke(request, CAPABILITY_FUSION, refinementService::fuseVisualContext);
    }

    @Operation(summary = "AI视觉描述", description = "ai:refinement:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "视觉描述")
    @PostMapping(value = "visual-description/create")
    public AiRefinementResponses.CandidateResultResponse createVisualDescription(
            @Valid @RequestBody AiRefinementRequests.RefinementRequest request) {
        return invoke(request, CAPABILITY_VISUAL, refinementService::describeVisual);
    }

    @Operation(summary = "AI生图", description = "ai:refinement:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "生图")
    @PostMapping(value = "image/create")
    public AiRefinementResponses.CandidateResultResponse createImage(
            @Valid @RequestBody AiRefinementRequests.RefinementRequest request) {
        return invoke(request, CAPABILITY_IMAGE_GEN, refinementService::generateImage);
    }

    @Operation(summary = "AI条目拆分", description = "ai:refinement:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "条目拆分")
    @PostMapping(value = "entry-split/create")
    public AiRefinementResponses.CandidateResultResponse createEntrySplit(
            @Valid @RequestBody AiRefinementRequests.RefinementRequest request) {
        return invoke(request, CAPABILITY_SPLIT, refinementService::splitEntry);
    }

    private AiRefinementResponses.CandidateResultResponse invoke(
            AiRefinementRequests.RefinementRequest request,
            String capability,
            Function<AiRefinementRequestCommand, AiCandidateResult> invocation) {
        return AiRefinementInterfaceAssembler.toResponse(
                invocation.apply(AiRefinementInterfaceAssembler.toCommand(request, capability)));
    }
}
