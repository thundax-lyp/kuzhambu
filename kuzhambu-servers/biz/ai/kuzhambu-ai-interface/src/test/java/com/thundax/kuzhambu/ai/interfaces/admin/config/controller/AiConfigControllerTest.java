package com.thundax.kuzhambu.ai.interfaces.admin.config.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.ai.application.capability.result.AiActionStatusResult;
import com.thundax.kuzhambu.ai.application.capability.service.AiCapabilityApplicationService;
import com.thundax.kuzhambu.ai.application.config.service.AiServiceConfigApplicationService;
import com.thundax.kuzhambu.ai.application.model.service.AiModelApplicationService;
import com.thundax.kuzhambu.ai.interfaces.admin.config.controller.request.AiConfigRequests.ActionStatusListRequest;
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

    private static AiServiceConfigApplicationService noServiceConfigService() {
        return proxy(AiServiceConfigApplicationService.class, noOpInvocationHandler("service config service"));
    }

    private static AiModelApplicationService noModelService() {
        return proxy(AiModelApplicationService.class, noOpInvocationHandler("model service"));
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
