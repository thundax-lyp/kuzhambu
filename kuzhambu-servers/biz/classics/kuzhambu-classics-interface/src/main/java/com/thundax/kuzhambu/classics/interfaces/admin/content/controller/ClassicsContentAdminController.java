package com.thundax.kuzhambu.classics.interfaces.admin.content.controller;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.interfaces.admin.content.assembler.ClassicsContentInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.request.ClassicsContentRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.response.ClassicsContentResponse;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@SysLogger(module = {"古籍", "通用内容"})
@RequestMapping("/api/classics/content")
@WrappedApiController
public class ClassicsContentAdminController {
    private final ClassicsContentApplicationService service;

    public ClassicsContentAdminController(ClassicsContentApplicationService service) {
        this.service = service;
    }

    @HasPermission("classics:content:view")
    @GetMapping("tags")
    public List<ClassicsContentResponse> listTags(@RequestParam String contentType, @RequestParam Long contentId) {
        return service.listTags(contentType, contentId).stream()
                .map(ClassicsContentInterfaceAssembler::toTagResponse)
                .toList();
    }

    @HasPermission("classics:content:edit")
    @PostMapping("tags/save")
    public Long saveTag(@Valid @RequestBody ClassicsContentRequest request) {
        return service.saveTag(ClassicsContentInterfaceAssembler.toTagCommand(request));
    }

    @HasPermission("classics:content:view")
    @GetMapping("qa-pairs")
    public List<ClassicsContentResponse> listQaPairs(@RequestParam String contentType, @RequestParam Long contentId) {
        return service.listQaPairs(contentType, contentId).stream()
                .map(ClassicsContentInterfaceAssembler::toQaResponse)
                .toList();
    }

    @HasPermission("classics:content:edit")
    @PostMapping("qa-pairs/save")
    public Long saveQaPair(@Valid @RequestBody ClassicsContentRequest request) {
        return service.saveQaPair(ClassicsContentInterfaceAssembler.toQaCommand(request));
    }

    @HasPermission("classics:content:export")
    @PostMapping("exports")
    public Long createExport(@Valid @RequestBody ClassicsContentRequest request) {
        return service.createExportJob(ClassicsContentInterfaceAssembler.toExportCommand(request));
    }
}
