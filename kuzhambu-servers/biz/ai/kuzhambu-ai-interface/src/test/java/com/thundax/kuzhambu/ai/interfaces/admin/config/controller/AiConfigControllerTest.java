package com.thundax.kuzhambu.ai.interfaces.admin.config.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.ai.application.config.service.AiBusinessConfigApplicationService;
import com.thundax.kuzhambu.ai.application.config.service.AiCapabilityCatalogApplicationService;
import com.thundax.kuzhambu.ai.application.config.service.AiModelApplicationService;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.interfaces.admin.config.controller.request.AiConfigRequests.CapabilityQueryRequest;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class AiConfigControllerTest {

    @Test
    void routesShouldKeepCapabilityListApiPathAndPermission() throws Exception {
        RequestMapping mapping = AiConfigController.class.getAnnotation(RequestMapping.class);
        assertEquals("/api/ai/config", mapping.value()[0]);

        Method method = AiConfigController.class.getDeclaredMethod("listCapabilities", CapabilityQueryRequest.class);
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        assertEquals("capability/list", postMapping.value()[0]);
        HasPermission permission = method.getAnnotation(HasPermission.class);
        assertEquals(List.of("ai:config:view"), List.of(permission.value()));
    }

    @Test
    void listCapabilitiesShouldDelegateEnabledFilterToCapabilityService() {
        AiConfigController controller =
                new AiConfigController(noModelService(), capabilityListService(), noBusinessConfigService());
        CapabilityQueryRequest request = new CapabilityQueryRequest();
        request.setEnabled(true);

        var response = controller.listCapabilities(request);

        assertEquals(1, response.size());
        assertEquals(
                AiBusinessCapability.CLASSICS_TRANSLATE.value(), response.get(0).getCapability());
    }

    private static AiModelApplicationService noModelService() {
        return proxy(AiModelApplicationService.class, noOpInvocationHandler("model service"));
    }

    private static AiCapabilityCatalogApplicationService capabilityListService() {
        return proxy(AiCapabilityCatalogApplicationService.class, (proxy, method, args) -> {
            if ("listCapabilities".equals(method.getName())) {
                assertEquals(true, args[0]);
                return List.of(AiBusinessCapability.CLASSICS_TRANSLATE);
            }
            throw new UnsupportedOperationException(
                    "capability service should not be called in this test: " + method.getName());
        });
    }

    private static AiBusinessConfigApplicationService noBusinessConfigService() {
        return proxy(AiBusinessConfigApplicationService.class, noOpInvocationHandler("business config service"));
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
