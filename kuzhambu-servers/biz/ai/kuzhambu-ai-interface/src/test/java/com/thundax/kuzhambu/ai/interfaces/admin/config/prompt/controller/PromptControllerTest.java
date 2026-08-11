package com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.controller;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.ai.application.config.service.AiCapabilityCatalogApplicationService;
import com.thundax.kuzhambu.ai.application.config.service.PromptApplicationService;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.controller.request.PromptRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.controller.response.PromptResponses;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Test;

public class PromptControllerTest {

    @Test
    public void variableApiModelsShouldNotExposeInternalPriority() {
        assertThrows(
                NoSuchFieldException.class,
                () -> PromptRequests.VariableItemRequest.class.getDeclaredField("priority"));
        assertThrows(
                NoSuchFieldException.class, () -> PromptResponses.VariableResponse.class.getDeclaredField("priority"));
    }

    @Test
    public void getTemplateShouldReturnEmptyResponseWhenTemplateIsMissing() {
        PromptController controller = new PromptController(missingPromptService(), noCapabilityService());
        PromptRequests.TemplateIdRequest request = new PromptRequests.TemplateIdRequest();
        request.setId(99L);

        var response = controller.getTemplate(request);

        assertNull(response.getId());
    }

    @Test
    public void getTemplateByCapabilityShouldReturnEmptyResponseWhenTemplateIsMissing() {
        PromptController controller = new PromptController(missingPromptService(), noCapabilityService());
        PromptRequests.TemplateQueryRequest request = new PromptRequests.TemplateQueryRequest();
        request.setCapability(AiBusinessCapability.CLASSICS_SUMMARY.value());

        var response = controller.getTemplateByCapability(request);

        assertNull(response.getId());
    }

    @Test
    public void getCurrentVersionShouldReturnEmptyResponseWhenVersionIsMissing() {
        PromptController controller = new PromptController(missingPromptService(), noCapabilityService());
        PromptRequests.TemplateIdRequest request = new PromptRequests.TemplateIdRequest();
        request.setId(99L);

        var response = controller.getLatestVersion(request);

        assertNull(response.getId());
    }

    @Test
    public void buildOptimizationSuggestionShouldReturnEmptyResponseWhenVersionIsMissing() {
        PromptController controller = new PromptController(missingPromptService(), noCapabilityService());
        PromptRequests.OptimizationRequest request = new PromptRequests.OptimizationRequest();
        request.setId(99L);
        request.setChangeSummary("优化");

        var response = controller.createOptimizationSuggestion(request);

        assertNull(response.getId());
    }

    private static PromptApplicationService missingPromptService() {
        return proxy(PromptApplicationService.class, (proxy, method, args) -> {
            if ("get".equals(method.getName())
                    || "getByCapability".equals(method.getName())
                    || "getCurrentVersion".equals(method.getName())
                    || "buildOptimizationSuggestion".equals(method.getName())) {
                return null;
            }
            throw new UnsupportedOperationException(
                    "prompt service should not be called in this test: " + method.getName());
        });
    }

    private static AiCapabilityCatalogApplicationService noCapabilityService() {
        return proxy(AiCapabilityCatalogApplicationService.class, noOpInvocationHandler("capability service"));
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
