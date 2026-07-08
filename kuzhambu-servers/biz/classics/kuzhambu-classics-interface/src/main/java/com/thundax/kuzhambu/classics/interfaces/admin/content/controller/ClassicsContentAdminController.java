package com.thundax.kuzhambu.classics.interfaces.admin.content.controller;

import com.thundax.kuzhambu.classics.application.content.command.ContentExportCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairSortCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagSortCommand;
import com.thundax.kuzhambu.classics.application.content.result.AiCandidateApplyContentResult;
import com.thundax.kuzhambu.classics.application.content.result.ClassicsExportJobResult;
import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.mingcustoms.service.MingCustomsApplicationService;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationResult;
import com.thundax.kuzhambu.classics.application.result.ClassicsStoredContentResult;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiApplicationService;
import com.thundax.kuzhambu.classics.application.wangqi.service.WangqiDocumentApplicationService;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentExportJobIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentQaPairIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentTagIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentQaPairId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentTagId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.codec.MingCustomsEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.wangqi.codec.WangqiDocumentIdCodec;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility;
import com.thundax.kuzhambu.classics.interfaces.admin.common.response.ClassicsBatchOperationResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.content.assembler.ClassicsContentInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.request.ClassicsBatchVisibilityRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.request.ClassicsContentQaPairSortRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.request.ClassicsContentRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.request.ClassicsContentTagSortRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.response.ClassicsContentResponse;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.common.web.request.RequestListHelper;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "古籍模块-通用内容", description = "通用内容")
@SysLogger(module = {"古籍", "通用内容"})
@RequestMapping("/api/classics/content")
@WrappedApiController
public class ClassicsContentAdminController {
    private static final Set<String> BATCH_VISIBILITY_CONTENT_TYPES =
            Set.of("SANCAI_ENTRY", "WANGQI_DOCUMENT", "MING_CUSTOMS");
    private static final Set<String> BATCH_VISIBILITIES = Set.of("PUBLIC", "PRIVATE");
    private static final Set<String> CONTENT_TAG_CONTENT_TYPES =
            Set.of("SANCAI_ENTRY", "WANGQI_DOCUMENT", "MING_CUSTOMS");

    private final ClassicsContentApplicationService service;
    private final SancaiApplicationService sancaiService;
    private final WangqiDocumentApplicationService wangqiDocumentService;
    private final MingCustomsApplicationService mingCustomsService;

    public ClassicsContentAdminController(
            ClassicsContentApplicationService service,
            SancaiApplicationService sancaiService,
            WangqiDocumentApplicationService wangqiDocumentService,
            MingCustomsApplicationService mingCustomsService) {
        this.service = service;
        this.sancaiService = sancaiService;
        this.wangqiDocumentService = wangqiDocumentService;
        this.mingCustomsService = mingCustomsService;
    }

    @Operation(summary = "查询古籍内容标签", description = "classics:content:view")
    @ApiImplicitParams({})
    @HasPermission("classics:content:view")
    @SysLogger(value = "标签列表")
    @GetMapping("tags")
    public List<ClassicsContentResponse> listTags(@RequestParam String contentType, @RequestParam Long contentId) {
        String validContentType = validContentTagType(contentType);
        ClassicsContentId contentIdValue = ClassicsContentIdCodec.toDomain(requireParameter(contentId, "contentId"));
        return service.listTags(validContentType, contentIdValue).stream()
                .map(ClassicsContentInterfaceAssembler::toTagResponse)
                .toList();
    }

    @Operation(summary = "新增古籍内容标签", description = "classics:content:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:content:edit")
    @SysLogger(value = "新增标签")
    @PostMapping("tags/add")
    public ClassicsContentResponse addTag(@Valid @RequestBody ClassicsContentRequest request) {
        validateTagMutationRequest(request, false);
        ClassicsContentTagId id = service.addTag(ClassicsContentInterfaceAssembler.toTagCommand(request));
        return ClassicsContentResponse.builder()
                .id(id == null ? null : id.value())
                .build();
    }

    @Operation(summary = "更新古籍内容标签", description = "classics:content:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:content:edit")
    @SysLogger(value = "更新标签")
    @PostMapping("tags/update")
    public ClassicsContentResponse updateTag(@Valid @RequestBody ClassicsContentRequest request) {
        validateTagMutationRequest(request, true);
        ClassicsContentTagId id = service.updateTag(ClassicsContentInterfaceAssembler.toTagCommand(request));
        return ClassicsContentResponse.builder()
                .id(id == null ? null : id.value())
                .build();
    }

    @Operation(summary = "删除古籍内容标签", description = "classics:content:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:content:edit")
    @SysLogger(value = "删除标签")
    @PostMapping("tags/delete")
    public Boolean deleteTag(@Valid @RequestBody ClassicsContentRequest request) {
        service.deleteTag(
                ClassicsContentTagIdCodec.toDomain(requireParameter(request == null ? null : request.getId(), "id")));
        return true;
    }

    @Operation(summary = "排序古籍内容标签", description = "classics:content:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:content:edit")
    @SysLogger(value = "标签排序")
    @PostMapping("tags/sort")
    public Boolean sortTags(@Valid @RequestBody ClassicsContentTagSortRequest request) {
        String contentType = validContentTagType(request == null ? null : request.getContentType());
        Long contentId = requireParameter(request == null ? null : request.getContentId(), "contentId");
        service.sortTags(new ContentTagSortCommand(
                contentType,
                ClassicsContentIdCodec.toDomain(contentId),
                RequestListHelper.map(
                        RequestListHelper.presentUnique(
                                request == null ? null : request.getOrderedIds(),
                                "orderedIds",
                                AdminResponseExceptions::invalidParameter),
                        ClassicsContentTagIdCodec::toDomain),
                request == null ? null : request.getSortDirection()));
        return true;
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

    @Operation(summary = "新增古籍内容问答", description = "classics:content:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:content:edit")
    @SysLogger(value = "新增问答")
    @PostMapping("qa-pairs/add")
    public ClassicsContentResponse addQaPair(@Valid @RequestBody ClassicsContentRequest request) {
        ClassicsContentQaPairId id = service.addQaPair(ClassicsContentInterfaceAssembler.toQaCommand(request));
        return ClassicsContentResponse.builder()
                .id(id == null ? null : id.value())
                .build();
    }

    @Operation(summary = "应用AI候选到内容", description = "classics:content:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:content:edit")
    @SysLogger(value = "AI候选应用")
    @PostMapping("ai-candidates/change")
    public ClassicsContentResponse.AiCandidateApplyResponse changeAiCandidate(
            @Valid @RequestBody ClassicsContentRequest.AiCandidateApplyRequest request) {
        AiCandidateApplyContentResult result =
                service.applyAiCandidate(ClassicsContentInterfaceAssembler.toAiCandidateApplyCommand(request));
        return ClassicsContentInterfaceAssembler.toAiCandidateApplyResponse(result);
    }

    @Operation(summary = "批量应用AI候选到内容", description = "classics:content:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:content:edit")
    @SysLogger(value = "AI候选批量应用")
    @PostMapping("ai-candidates/batch/apply")
    public ClassicsBatchOperationResponse changeAiCandidates(
            @Valid @RequestBody ClassicsContentRequest.AiCandidateBatchApplyRequest request) {
        validateCandidateIdUnique(request == null ? null : request.getItems());
        return ClassicsBatchOperationResponse.from(
                service.applyAiCandidates(ClassicsContentInterfaceAssembler.toAiCandidateBatchApplyCommand(request)));
    }

    @Operation(summary = "批量拒绝AI候选", description = "classics:content:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:content:edit")
    @SysLogger(value = "AI候选批量拒绝")
    @PostMapping("ai-candidates/batch/reject")
    public ClassicsBatchOperationResponse removeAiCandidates(
            @Valid @RequestBody ClassicsContentRequest.AiCandidateBatchRejectRequest request) {
        validateCandidateIdUnique(request == null ? null : request.getItems());
        return ClassicsBatchOperationResponse.from(
                service.rejectAiCandidates(ClassicsContentInterfaceAssembler.toAiCandidateBatchRejectCommand(request)));
    }

    @Operation(summary = "批量修改古籍内容可见性", description = "classics:content:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:content:edit")
    @SysLogger(value = "批量可见性")
    @PostMapping("visibility/change")
    public ClassicsBatchOperationResponse changeBatchVisibility(
            @Valid @RequestBody ClassicsBatchVisibilityRequest request) {
        String contentType = validBatchContentType(request == null ? null : request.getContentType());
        String visibility = validBatchVisibility(request == null ? null : request.getVisibility());
        List<Long> contentIds = RequestListHelper.presentUnique(
                request == null ? null : request.getContentIds(),
                "contentIds",
                AdminResponseExceptions::invalidParameter);
        return ClassicsBatchOperationResponse.from(changeBatchVisibility(contentType, contentIds, visibility));
    }

    @Operation(summary = "更新古籍内容问答", description = "classics:content:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:content:edit")
    @SysLogger(value = "更新问答")
    @PostMapping("qa-pairs/update")
    public ClassicsContentResponse updateQaPair(@Valid @RequestBody ClassicsContentRequest request) {
        ClassicsContentQaPairId id = service.updateQaPair(ClassicsContentInterfaceAssembler.toQaCommand(request));
        return ClassicsContentResponse.builder()
                .id(id == null ? null : id.value())
                .build();
    }

    @Operation(summary = "排序古籍内容问答", description = "classics:content:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:content:edit")
    @SysLogger(value = "问答排序")
    @PostMapping("qa-pairs/sort")
    public Boolean sortQaPairs(@Valid @RequestBody ClassicsContentQaPairSortRequest request) {
        service.sortQaPairs(new ContentQaPairSortCommand(
                RequestListHelper.map(
                        RequestListHelper.presentUnique(
                                request == null ? null : request.getOrderedIds(),
                                "orderedIds",
                                AdminResponseExceptions::invalidParameter),
                        ClassicsContentQaPairIdCodec::toDomain),
                request == null ? null : request.getSortDirection()));
        return true;
    }

    @Operation(summary = "创建古籍内容导出任务", description = "classics:content:export")
    @ApiImplicitParams({})
    @HasPermission("classics:content:export")
    @SysLogger(value = "创建导出任务")
    @PostMapping("exports/create")
    public ClassicsContentResponse createExport(@Valid @RequestBody ClassicsContentRequest request) {
        ContentExportCommand command = ClassicsContentInterfaceAssembler.toExportCommand(request);
        command.setOperatorPermissions(KuzhambuContextHolder.currentAuthorities());
        ClassicsExportJobResult result = service.createExportJob(command);
        return ClassicsContentResponse.builder()
                .id(
                        result == null || result.getJobId() == null
                                ? null
                                : result.getJobId().value())
                .status(
                        result == null || result.getStatus() == null
                                ? null
                                : result.getStatus().name())
                .build();
    }

    @Operation(summary = "分页查询古籍内容导出任务", description = "classics:content:view")
    @ApiImplicitParams({})
    @HasPermission("classics:content:view")
    @SysLogger(value = "导出任务列表")
    @PostMapping("exports/page")
    public PageResponse<ClassicsContentResponse> pageExports(@Valid @RequestBody ClassicsContentRequest request) {
        PageQuery pageQuery = PageInterfaceAssembler.toPageQuery(request);
        return PageResponseHelper.fromPageResult(
                service.pageExportJobs(
                        request.getContentType(), request.getExportKind(), request.getStatus(), pageQuery),
                ClassicsContentInterfaceAssembler::toExportResponse);
    }

    @Operation(summary = "删除古籍内容导出任务", description = "classics:content:export")
    @ApiImplicitParams({})
    @HasPermission("classics:content:export")
    @SysLogger(value = "删除导出任务")
    @PostMapping("exports/delete")
    public Boolean deleteExport(@Valid @RequestBody ClassicsContentRequest request) {
        service.deleteExportJob(ClassicsContentExportJobIdCodec.toDomain(
                requireParameter(request == null ? null : request.getId(), "id")));
        return true;
    }

    @Operation(summary = "下载古籍内容导出文件", description = "classics:content:view")
    @ApiImplicitParams({})
    @HasPermission("classics:content:view")
    @SysLogger(value = "导出文件下载")
    @GetMapping("exports/{jobId}/content")
    public void downloadExportContent(
            @PathVariable("jobId") Long jobId,
            @RequestParam(value = "download", required = false) Boolean download,
            HttpServletResponse response)
            throws IOException {
        ClassicsStoredContentResult content =
                service.getExportJobContent(ClassicsContentExportJobIdCodec.toDomain(jobId));
        if (content == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentType(
                StringUtils.defaultIfBlank(content.getContentType(), MediaType.APPLICATION_OCTET_STREAM_VALUE));
        if (content.getSize() != null) {
            response.setContentLengthLong(content.getSize());
        }
        response.setHeader(
                "Content-Disposition",
                contentDisposition(content.getOriginalFilename(), Boolean.TRUE.equals(download)));
        try (InputStream inputStream = content.getInputStream()) {
            inputStream.transferTo(response.getOutputStream());
        }
    }

    private static String contentDisposition(String originalFilename, boolean download) {
        String disposition = download ? "attachment" : "inline";
        String filename = StringUtils.defaultIfBlank(fileName(originalFilename), "file");
        String asciiFilename = filename.replace("\\", "").replace("\"", "");
        String encodedFilename =
                URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return disposition + "; filename=\"" + asciiFilename + "\"; filename*=UTF-8''" + encodedFilename;
    }

    private static String validBatchContentType(String contentType) {
        String value = StringUtils.trimToNull(contentType);
        if (!BATCH_VISIBILITY_CONTENT_TYPES.contains(value)) {
            throw AdminResponseExceptions.invalidParameter("contentType");
        }
        return value;
    }

    private static String validBatchVisibility(String visibility) {
        String value = StringUtils.trimToNull(visibility);
        if (!BATCH_VISIBILITIES.contains(value)) {
            throw AdminResponseExceptions.invalidParameter("visibility");
        }
        return value;
    }

    private static void validateTagMutationRequest(ClassicsContentRequest request, boolean requireId) {
        if (request == null) {
            throw AdminResponseExceptions.invalidParameter("request");
        }
        if (requireId) {
            requireParameter(request.getId(), "id");
        }
        request.setContentType(validContentTagType(request.getContentType()));
        requireParameter(request.getContentId(), "contentId");
        String tagName = StringUtils.trimToNull(request.getTagNameSnapshot());
        if (tagName == null) {
            throw AdminResponseExceptions.invalidParameter("tagNameSnapshot");
        }
        request.setTagNameSnapshot(tagName);
    }

    private static String validContentTagType(String contentType) {
        String value = StringUtils.trimToNull(contentType);
        if (!CONTENT_TAG_CONTENT_TYPES.contains(value)) {
            throw AdminResponseExceptions.invalidParameter("contentType");
        }
        return value;
    }

    private static Long requireParameter(Long value, String name) {
        if (value == null) {
            throw AdminResponseExceptions.invalidParameter(name);
        }
        return value;
    }

    private static void validateCandidateIdUnique(List<?> items) {
        if (items == null || items.isEmpty()) {
            throw AdminResponseExceptions.invalidParameter("items");
        }
        List<Long> ids = items.stream()
                .map(item -> {
                    if (item instanceof ClassicsContentRequest.AiCandidateApplyRequest applyRequest) {
                        return applyRequest.getCandidateId();
                    }
                    if (item instanceof ClassicsContentRequest.AiCandidateRejectItemRequest rejectRequest) {
                        return rejectRequest.getCandidateId();
                    }
                    return null;
                })
                .collect(Collectors.toList());
        if (ids.isEmpty() || ids.contains(null)) {
            throw AdminResponseExceptions.invalidParameter("candidateId");
        }
        if (ids.size()
                != RequestListHelper.presentUnique(ids, "candidateId", AdminResponseExceptions::invalidParameter)
                        .size()) {
            throw AdminResponseExceptions.invalidParameter("candidateId");
        }
    }

    private ClassicsBatchOperationResult changeBatchVisibility(
            String contentType, List<Long> contentIds, String visibility) {
        return switch (contentType) {
            case "SANCAI_ENTRY" ->
                sancaiService.batchChangeEntryVisibility(
                        RequestListHelper.map(contentIds, SancaiEntryIdCodec::toDomain),
                        visibility,
                        KuzhambuContextHolder.currentAuthorities());
            case "WANGQI_DOCUMENT" ->
                wangqiDocumentService.batchChangeVisibility(
                        RequestListHelper.map(contentIds, WangqiDocumentIdCodec::toDomain),
                        WangqiDocumentVisibility.from(visibility),
                        KuzhambuContextHolder.currentAuthorities());
            case "MING_CUSTOMS" ->
                mingCustomsService.batchChangeVisibility(
                        RequestListHelper.map(contentIds, MingCustomsEntryIdCodec::toDomain),
                        visibility,
                        KuzhambuContextHolder.currentAuthorities());
            default -> throw AdminResponseExceptions.invalidParameter("contentType");
        };
    }

    private static String fileName(String path) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        String normalized = path.replace('\\', '/');
        int index = normalized.lastIndexOf('/');
        return index >= 0 ? normalized.substring(index + 1) : normalized;
    }
}
