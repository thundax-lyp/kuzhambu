package com.thundax.kuzhambu.system.interfaces.admin;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.AuditController;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.request.AuditLogDetailRequest;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.request.AuditLogPageRequest;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.response.AuditLogDetailResponse;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.response.AuditLogResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.LogController;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.LogPageRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.LogDepartmentResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.LogResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.LogUserResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class SystemLogContractTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void logRoutesShouldKeepAdminApiPaths() throws Exception {
        assertRequestMapping(AuditController.class, "/api/audit/log");
        assertPostMapping(AuditController.class, "page", "page", AuditLogPageRequest.class);
        assertPostMapping(AuditController.class, "detail", "detail", AuditLogDetailRequest.class);
        assertPostMapping(AuditController.class, "options", "options");

        assertRequestMapping(LogController.class, "/api/sys/log");
        assertPostMapping(LogController.class, "page", "page", LogPageRequest.class);
    }

    @Test
    void logPagePermissionShouldBeSystemLogView() throws Exception {
        Method pageMethod = LogController.class.getDeclaredMethod("page", LogPageRequest.class);
        HasPermission permission = pageMethod.getAnnotation(HasPermission.class);
        assertArrayEquals(new String[] {"system:log:view"}, permission.value(), permission::toString);
    }

    @Test
    void logPageRequestsShouldKeepJsonFieldNames() throws Exception {
        AuditLogPageRequest auditLogPageRequest = new AuditLogPageRequest();
        auditLogPageRequest.setPageNo(1);
        auditLogPageRequest.setPageSize(20);
        auditLogPageRequest.setObjectType("USER");
        auditLogPageRequest.setObjectId("user-1");
        auditLogPageRequest.setAction("CREATE");
        auditLogPageRequest.setOperatorType("USER");
        auditLogPageRequest.setOperatorId("operator-1");
        auditLogPageRequest.setSource("ADMIN_WEB");
        auditLogPageRequest.setRequestId("request-1");
        auditLogPageRequest.setBeginDate(new Date(1778513052000L));
        auditLogPageRequest.setEndDate(new Date(1778514052000L));
        assertJsonFields(
                auditLogPageRequest,
                "pageNo",
                "pageSize",
                "objectType",
                "objectId",
                "action",
                "operatorType",
                "operatorId",
                "source",
                "requestId",
                "beginDate",
                "endDate");

        AuditLogDetailRequest auditLogDetailRequest =
                OBJECT_MAPPER.readValue("{\"id\":\"audit-1\"}", AuditLogDetailRequest.class);
        assertEquals("audit-1", auditLogDetailRequest.getId());
        assertJsonFields(auditLogDetailRequest, "id");

        LogPageRequest logPageRequest = new LogPageRequest();
        logPageRequest.setPageNo(1);
        logPageRequest.setPageSize(20);
        logPageRequest.setTitle("登录");
        logPageRequest.setUserLoginName("developer");
        logPageRequest.setUserName("Developer");
        logPageRequest.setRemoteAddr("127.0.0.1");
        logPageRequest.setRequestUri("/api/auth/session/login");
        logPageRequest.setBeginDate(new Date(1778513052000L));
        logPageRequest.setEndDate(new Date(1778514052000L));
        assertJsonFields(
                logPageRequest,
                "pageNo",
                "pageSize",
                "title",
                "userLoginName",
                "userName",
                "remoteAddr",
                "requestUri",
                "beginDate",
                "endDate");
    }

    @Test
    void logResponsesShouldKeepJsonFieldNames() throws Exception {
        assertJsonFields(
                AuditLogResponse.builder()
                        .id("audit-1")
                        .objectType("USER")
                        .objectId("user-1")
                        .objectDisplayName("Developer")
                        .objectTypeLabel("用户")
                        .version(1L)
                        .action("CREATE")
                        .actionLabel("新增")
                        .operatorType("USER")
                        .operatorTypeLabel("用户")
                        .operatorId("operator-1")
                        .operatorName("Developer")
                        .source("ADMIN_WEB")
                        .requestId("request-1")
                        .traceId("trace-1")
                        .remoteAddr("127.0.0.1")
                        .summary("新增用户")
                        .occurredAt(new Date(1778513052000L))
                        .changedFields(new ArrayList<>())
                        .build(),
                "id",
                "objectType",
                "objectId",
                "objectDisplayName",
                "objectTypeLabel",
                "version",
                "action",
                "actionLabel",
                "operatorType",
                "operatorTypeLabel",
                "operatorId",
                "operatorName",
                "source",
                "requestId",
                "traceId",
                "remoteAddr",
                "summary",
                "occurredAt",
                "changedFields");

        assertJsonFields(
                AuditLogDetailResponse.builder()
                        .id("audit-1")
                        .objectType("USER")
                        .objectId("user-1")
                        .objectDisplayName("Developer")
                        .objectTypeLabel("用户")
                        .version(2L)
                        .action("UPDATE")
                        .actionLabel("更新")
                        .operatorType("USER")
                        .operatorTypeLabel("用户")
                        .operatorId("operator-1")
                        .operatorName("Developer")
                        .source("ADMIN_WEB")
                        .requestId("request-1")
                        .traceId("trace-1")
                        .remoteAddr("127.0.0.1")
                        .summary("更新用户")
                        .occurredAt(new Date(1778513052000L))
                        .changedFields(new ArrayList<>())
                        .idempotencyKey("idempotency-1")
                        .previousVersion(1L)
                        .build(),
                "id",
                "objectType",
                "objectId",
                "objectDisplayName",
                "objectTypeLabel",
                "version",
                "action",
                "actionLabel",
                "operatorType",
                "operatorTypeLabel",
                "operatorId",
                "operatorName",
                "source",
                "requestId",
                "traceId",
                "remoteAddr",
                "summary",
                "occurredAt",
                "changedFields",
                "idempotencyKey",
                "previousVersion");

        LogDepartmentResponse departmentResponse = LogDepartmentResponse.builder()
                .id("department-1")
                .name("技术中心")
                .namePath("总部/技术中心")
                .build();
        LogUserResponse userResponse = LogUserResponse.builder()
                .id("user-1")
                .loginName("developer")
                .name("Developer")
                .department(departmentResponse)
                .build();
        assertJsonFields(
                LogResponse.builder()
                        .id("log-1")
                        .remarks("系统日志")
                        .createDate(new Date(1778513052000L))
                        .type("ACCESS")
                        .title("系统-登录-成功")
                        .remoteAddr("127.0.0.1")
                        .userAgent("Playwright")
                        .method("POST")
                        .requestUri("/api/auth/session/login")
                        .requestParams("{\"loginName\":\"developer\"}")
                        .createUser(userResponse)
                        .build(),
                "id",
                "remarks",
                "createDate",
                "type",
                "title",
                "remoteAddr",
                "userAgent",
                "method",
                "requestUri",
                "requestParams",
                "createUser");
    }

    private static void assertRequestMapping(Class<?> controllerType, String expectedPath) {
        RequestMapping mapping = controllerType.getAnnotation(RequestMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
    }

    private static void assertPostMapping(
            Class<?> controllerType, String methodName, String expectedPath, Class<?>... parameterTypes)
            throws Exception {
        Method method = controllerType.getDeclaredMethod(methodName, parameterTypes);
        PostMapping mapping = method.getAnnotation(PostMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
    }

    private static void assertJsonFields(Object value, String... expectedFields) {
        JsonNode json = OBJECT_MAPPER.valueToTree(value);
        assertEquals(expectedFields.length, json.size(), json::toString);
        for (String expectedField : expectedFields) {
            assertTrue(json.has(expectedField), json::toString);
        }
    }
}
