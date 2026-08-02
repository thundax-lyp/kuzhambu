package com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.result.ClassicsStoredContentResult;
import com.thundax.kuzhambu.classics.application.sancai.result.SancaiEntryImageContent;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiApplicationService;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiAssetApplicationService;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiCategoryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryImageIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiVisualAssetIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategoryOverview;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.interfaces.portal.sancai.assembler.SancaiPortalInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.request.SancaiPortalEntrySearchRequest;
import com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.response.SancaiPortalCategoryResponse;
import com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.response.SancaiPortalEntryResponse;
import com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.response.SancaiPortalVolumeResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import com.thundax.kuzhambu.common.web.annotation.PostJsonApiExempt;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "古籍门户-三才图会", description = "三才图会门户展示")
@PublicApi
@RequestMapping("/api/portal/classics/sancai")
@WrappedApiController
public class SancaiPortalController {
    private final SancaiApplicationService service;
    private final SancaiAssetApplicationService assetService;
    private final ClassicsContentApplicationService contentService;

    public SancaiPortalController(
            SancaiApplicationService service,
            SancaiAssetApplicationService assetService,
            ClassicsContentApplicationService contentService) {
        this.service = service;
        this.assetService = assetService;
        this.contentService = contentService;
    }

    @Operation(summary = "查询三才图会门户门类", description = "公开访问")
    @PostMapping("categories/list")
    public List<SancaiPortalCategoryResponse> listCategories() {
        Map<Long, SancaiCategoryOverview> overviewByCategoryId = service.listPortalReadyCategoryOverviews().stream()
                .filter(overview -> overview.getCategoryId() != null)
                .collect(Collectors.toMap(
                        overview -> overview.getCategoryId().value(), Function.identity(), (left, right) -> left));
        return service.listCategories().stream()
                .map(category -> SancaiPortalInterfaceAssembler.toResponse(
                        category,
                        category.getId() == null
                                ? null
                                : overviewByCategoryId.get(category.getId().value())))
                .toList();
    }

    @Operation(summary = "查询三才图会门户卷目", description = "公开访问")
    @PostMapping("volumes/list")
    public List<SancaiPortalVolumeResponse> listVolumes(@Valid @RequestBody SancaiPortalEntrySearchRequest request) {
        return service
                .listVolumes(SancaiCategoryIdCodec.toDomain(request == null ? null : request.getCategoryId()))
                .stream()
                .map(SancaiPortalInterfaceAssembler::toResponse)
                .toList();
    }

    @Operation(summary = "分页查询三才图会门户公开条目", description = "公开访问")
    @PostMapping("entries/page")
    public PageResponse<SancaiPortalEntryResponse> pageEntries(
            @Valid @RequestBody SancaiPortalEntrySearchRequest request) {
        SancaiPortalEntrySearchRequest effectiveRequest =
                request == null ? new SancaiPortalEntrySearchRequest() : request;
        effectiveRequest.setKeyword(SancaiPortalInterfaceAssembler.normalizeKeyword(effectiveRequest.getKeyword()));
        return PageResponseHelper.fromPageResult(
                service.pagePortalReadyEntries(
                        SancaiPortalInterfaceAssembler.toPublicQuery(effectiveRequest),
                        new PageQuery(
                                SancaiPortalInterfaceAssembler.pageNo(effectiveRequest.getPageNo()),
                                SancaiPortalInterfaceAssembler.pageSize(effectiveRequest.getPageSize()))),
                SancaiPortalInterfaceAssembler::toResponse);
    }

    @Operation(summary = "查看三才图会门户公开条目详情", description = "公开访问")
    @PostMapping("entries/get")
    public SancaiPortalEntryResponse getEntry(@Valid @RequestBody SancaiPortalEntrySearchRequest request) {
        Long entryIdValue = request == null ? null : request.getId();
        SancaiEntryId entryId = SancaiEntryIdCodec.toDomain(entryIdValue);
        SancaiEntry entry = requirePublicEntry(entryId);
        return SancaiPortalInterfaceAssembler.toResponse(
                entry,
                contentService.listTags("SANCAI_ENTRY", ClassicsContentIdCodec.toDomain(entryIdValue)),
                assetService.listImages(entryId),
                assetService.listVisualAssets(entryId));
    }

    @Operation(summary = "读取三才图会门户图片内容", description = "公开文件内容")
    @PostJsonApiExempt(reason = "文件内容需要浏览器直链预览或下载")
    @GetMapping("images/{entryId}/{imageId}/content")
    public void downloadImage(
            @PathVariable("entryId") Long entryId,
            @PathVariable("imageId") Long imageId,
            @RequestParam(value = "download", required = false) Boolean download,
            HttpServletResponse response)
            throws IOException {
        requirePublicEntry(SancaiEntryIdCodec.toDomain(entryId));
        SancaiEntryImageContent imageContent;
        try {
            imageContent = assetService.getImageContent(
                    SancaiEntryIdCodec.toDomain(entryId), SancaiEntryImageIdCodec.toDomain(imageId));
        } catch (BizException exception) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        writeStoredContent(imageContent.getContent(), Boolean.TRUE.equals(download), response);
    }

    @Operation(summary = "读取三才图会门户视觉资产原图", description = "公开文件内容")
    @PostJsonApiExempt(reason = "文件内容需要浏览器直链预览或下载")
    @GetMapping("visual-assets/{entryId}/{visualAssetId}/source-content")
    public void downloadVisualAssetSourceContent(
            @PathVariable("entryId") Long entryId,
            @PathVariable("visualAssetId") Long visualAssetId,
            @RequestParam(value = "download", required = false) Boolean download,
            HttpServletResponse response)
            throws IOException {
        downloadVisualAssetContent(entryId, visualAssetId, true, download, response);
    }

    @Operation(summary = "读取三才图会门户视觉资产生成图", description = "公开文件内容")
    @PostJsonApiExempt(reason = "文件内容需要浏览器直链预览或下载")
    @GetMapping("visual-assets/{entryId}/{visualAssetId}/generated-content")
    public void downloadVisualAssetGeneratedContent(
            @PathVariable("entryId") Long entryId,
            @PathVariable("visualAssetId") Long visualAssetId,
            @RequestParam(value = "download", required = false) Boolean download,
            HttpServletResponse response)
            throws IOException {
        downloadVisualAssetContent(entryId, visualAssetId, false, download, response);
    }

    private SancaiEntry requirePublicEntry(SancaiEntryId entryId) {
        if (!service.isPortalReadyEntry(entryId)) {
            throw new BizException("三才图会条目不存在或不可公开访问");
        }
        SancaiEntry entry = service.getEntry(entryId);
        if (entry == null) {
            throw new BizException("三才图会条目不存在或不可公开访问");
        }
        return entry;
    }

    private void downloadVisualAssetContent(
            Long entryId, Long visualAssetId, boolean sourceContent, Boolean download, HttpServletResponse response)
            throws IOException {
        requirePublicEntry(SancaiEntryIdCodec.toDomain(entryId));
        ClassicsStoredContentResult content;
        try {
            content = sourceContent
                    ? assetService.getVisualAssetSourceContent(
                            SancaiEntryIdCodec.toDomain(entryId), SancaiVisualAssetIdCodec.toDomain(visualAssetId))
                    : assetService.getVisualAssetGeneratedContent(
                            SancaiEntryIdCodec.toDomain(entryId), SancaiVisualAssetIdCodec.toDomain(visualAssetId));
        } catch (BizException exception) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        writeStoredContent(content, Boolean.TRUE.equals(download), response);
    }

    private static void writeStoredContent(
            ClassicsStoredContentResult content, boolean download, HttpServletResponse response) throws IOException {
        response.setContentType(
                StringUtils.defaultIfBlank(content.getContentType(), MediaType.APPLICATION_OCTET_STREAM_VALUE));
        if (content.getSize() != null) {
            response.setContentLengthLong(content.getSize());
        }
        response.setHeader("Content-Disposition", contentDisposition(content.getOriginalFilename(), download));
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

    private static String fileName(String path) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        String normalized = path.replace('\\', '/');
        int index = normalized.lastIndexOf('/');
        return index >= 0 ? normalized.substring(index + 1) : normalized;
    }
}
