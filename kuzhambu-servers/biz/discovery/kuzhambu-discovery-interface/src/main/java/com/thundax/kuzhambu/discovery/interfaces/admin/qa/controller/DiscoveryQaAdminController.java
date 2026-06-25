package com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.discovery.application.qa.service.QaApplicationService;
import com.thundax.kuzhambu.discovery.interfaces.admin.qa.assembler.DiscoveryQaAdminInterfaceAssembler;
import com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller.request.DiscoveryQaAdminRequests;
import com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller.response.DiscoveryQaAdminResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "发现模块-Admin 问答", description = "Discovery Admin 问答调试接口")
@SysLogger(module = {"发现", "问答调试"})
@RequestMapping("/api/discovery/qa-admin")
@WrappedApiController
public class DiscoveryQaAdminController {

    private final QaApplicationService qaApplicationService;

    public DiscoveryQaAdminController(QaApplicationService qaApplicationService) {
        this.qaApplicationService = qaApplicationService;
    }

    @Operation(summary = "获取会话详情", description = "Discovery QA 会话详情")
    @HasPermission("discovery:qa:view")
    @IgnoreSysLogger
    @PostMapping("session/get")
    public DiscoveryQaAdminResponses.QaSessionDetailResponse getSession(
            @Valid @RequestBody DiscoveryQaAdminRequests.QaSessionGetRequest request) {
        return DiscoveryQaAdminInterfaceAssembler.toSessionDetailResponse(
                qaApplicationService.getSessionDetail(request.getSessionId()));
    }

    @Operation(summary = "获取来源列表", description = "Discovery QA 来源列表")
    @HasPermission("discovery:qa:view")
    @IgnoreSysLogger
    @PostMapping("source/list")
    public List<DiscoveryQaAdminResponses.QaSourceResponse> listSources(
            @Valid @RequestBody DiscoveryQaAdminRequests.QaSourceListRequest request) {
        return DiscoveryQaAdminInterfaceAssembler.toSourceResponses(
                qaApplicationService.listSourcesByMessageId(request.getMessageId()));
    }

    @Operation(summary = "获取检索轨迹", description = "Discovery QA 检索轨迹")
    @HasPermission("discovery:qa:view")
    @IgnoreSysLogger
    @PostMapping("trace/get")
    public DiscoveryQaAdminResponses.QaTraceResponse getTrace(
            @Valid @RequestBody DiscoveryQaAdminRequests.QaTraceGetRequest request) {
        return DiscoveryQaAdminInterfaceAssembler.toTraceResponse(
                qaApplicationService.getTraceByTraceId(request.getTraceId()));
    }
}
