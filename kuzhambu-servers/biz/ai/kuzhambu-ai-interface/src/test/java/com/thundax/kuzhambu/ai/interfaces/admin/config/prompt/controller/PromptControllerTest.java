package com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.controller.request.PromptRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.controller.response.PromptResponses;
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
}
