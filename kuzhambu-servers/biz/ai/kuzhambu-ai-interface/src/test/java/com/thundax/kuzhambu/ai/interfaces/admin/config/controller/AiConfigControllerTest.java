package com.thundax.kuzhambu.ai.interfaces.admin.config.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.ai.application.capability.result.AiActionStatusResult;
import com.thundax.kuzhambu.ai.application.capability.service.AiCapabilityApplicationService;
import com.thundax.kuzhambu.ai.application.config.service.AiServiceConfigApplicationService;
import com.thundax.kuzhambu.ai.application.model.service.AiModelApplicationService;
import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiCapabilityMapping;
import com.thundax.kuzhambu.ai.domain.model.model.entity.AiModelCheckRecord;
import com.thundax.kuzhambu.ai.interfaces.admin.config.controller.request.AiConfigRequests.ActionStatusListRequest;
import com.thundax.kuzhambu.ai.interfaces.admin.config.controller.request.AiConfigRequests.CapabilityQueryRequest;
import com.thundax.kuzhambu.ai.interfaces.admin.config.controller.request.AiConfigRequests.ModelIdRequest;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class AiConfigControllerTest {

    @Test
    void routesShouldKeepActionStatusListApiPathAndPermission() throws Exception {
        RequestMapping mapping = AiConfigController.class.getAnnotation(RequestMapping.class);
        assertEquals("/api/ai/config", mapping.value()[0]);

        Method method = AiConfigController.class.getDeclaredMethod("listActionStatuses", ActionStatusListRequest.class);
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        assertEquals("action/status/list", postMapping.value()[0]);
        HasPermission permission = method.getAnnotation(HasPermission.class);
        assertEquals(List.of("ai:config:view"), List.of(permission.value()));
    }

    @Test
    void routesShouldKeepModelCheckApiPathAndPermission() throws Exception {
        Method method = AiConfigController.class.getDeclaredMethod("checkModel", ModelIdRequest.class);
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        assertEquals("model/check", postMapping.value()[0]);
        HasPermission permission = method.getAnnotation(HasPermission.class);
        assertEquals(List.of("ai:config:edit"), List.of(permission.value()));
    }

    @Test
    void routesShouldKeepCapabilityMappingListApiPathAndPermission() throws Exception {
        Method method = AiConfigController.class.getDeclaredMethod("listMappings", CapabilityQueryRequest.class);
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        assertEquals("capability/mapping/list", postMapping.value()[0]);
        HasPermission permission = method.getAnnotation(HasPermission.class);
        assertEquals(List.of("ai:config:view"), List.of(permission.value()));
    }

    @Test
    void listActionStatusesShouldDelegateFiltersToCapabilityService() {
        AiConfigController controller =
                new AiConfigController(noServiceConfigService(), noModelService(), actionStatusListCapabilityService());
        ActionStatusListRequest request = new ActionStatusListRequest();
        request.setScope("classics");
        request.setCapability("summary");
        request.setAvailable(false);

        var response = controller.listActionStatuses(request);

        assertEquals(1, response.size());
        assertEquals("classics", response.get(0).getScope());
        assertEquals("summary", response.get(0).getCapability());
        assertEquals(false, response.get(0).getAvailable());
        assertEquals("MODEL_DISABLED", response.get(0).getUnavailableReason());
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), response.get(0).getCheckedAt());
    }

    @Test
    void checkModelShouldDelegateToModelService() {
        AiConfigController controller =
                new AiConfigController(noServiceConfigService(), modelCheckService(), noCapabilityService());
        ModelIdRequest request = new ModelIdRequest();
        request.setModelId(9001L);

        var response = controller.checkModel(request);

        assertEquals(8001L, response.getCheckId());
        assertEquals(9001L, response.getModelId());
        assertEquals("SUCCEEDED", response.getStatus());
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), response.getCheckedAt());
    }

    @Test
    void listMappingsShouldDelegateFiltersToCapabilityService() {
        AiConfigController controller =
                new AiConfigController(noServiceConfigService(), noModelService(), mappingListCapabilityService());
        CapabilityQueryRequest request = new CapabilityQueryRequest();
        request.setScope("classics");
        request.setCapability("summary");
        request.setEnabled(true);

        var response = controller.listMappings(request);

        assertEquals(1, response.size());
        assertEquals(7001L, response.get(0).getMappingId());
        assertEquals("classics", response.get(0).getScope());
        assertEquals("summary", response.get(0).getCapability());
        assertEquals(9001L, response.get(0).getModelId());
        assertEquals(true, response.get(0).getEnabled());
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), response.get(0).getConfiguredAt());
    }

    private static AiServiceConfigApplicationService noServiceConfigService() {
        return proxy(AiServiceConfigApplicationService.class, noOpInvocationHandler("service config service"));
    }

    private static AiModelApplicationService noModelService() {
        return proxy(AiModelApplicationService.class, noOpInvocationHandler("model service"));
    }

    private static AiModelApplicationService modelCheckService() {
        return proxy(AiModelApplicationService.class, (proxy, method, args) -> {
            if ("check".equals(method.getName())) {
                assertEquals(9001L, args[0]);
                return new AiModelCheckRecord(
                        null,
                        8001L,
                        9001L,
                        1001L,
                        "gpt-4o",
                        "SUCCEEDED",
                        12,
                        null,
                        null,
                        Instant.parse("2026-01-01T00:00:00Z"));
            }
            throw new UnsupportedOperationException(
                    "model service should not be called in this test: " + method.getName());
        });
    }

    private static AiCapabilityApplicationService noCapabilityService() {
        return proxy(AiCapabilityApplicationService.class, noOpInvocationHandler("capability service"));
    }

    private static AiCapabilityApplicationService actionStatusListCapabilityService() {
        return proxy(AiCapabilityApplicationService.class, (proxy, method, args) -> {
            if ("listActionStatuses".equals(method.getName())) {
                assertEquals("classics", args[0]);
                assertEquals("summary", args[1]);
                assertEquals(false, args[2]);
                return List.of(new AiActionStatusResult(
                        "classics", "summary", false, "MODEL_DISABLED", Instant.parse("2026-01-01T00:00:00Z")));
            }
            throw new UnsupportedOperationException(
                    "capability service should not be called in this test: " + method.getName());
        });
    }

    private static AiCapabilityApplicationService mappingListCapabilityService() {
        return proxy(AiCapabilityApplicationService.class, (proxy, method, args) -> {
            if ("listMappings".equals(method.getName())) {
                assertEquals("classics", args[0]);
                assertEquals("summary", args[1]);
                assertEquals(true, args[2]);
                return List.of(new AiCapabilityMapping(
                        null, 7001L, "classics", "summary", 9001L, true, Instant.parse("2026-01-01T00:00:00Z")));
            }
            throw new UnsupportedOperationException(
                    "capability service should not be called in this test: " + method.getName());
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler invocationHandler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, invocationHandler);
    }

    private static InvocationHandler noOpInvocationHandler(String name) {
        return (proxy, method, args) -> {
            throw new UnsupportedOperationException(name + " should not be called in this test: " + method.getName());
        };
    }
}
