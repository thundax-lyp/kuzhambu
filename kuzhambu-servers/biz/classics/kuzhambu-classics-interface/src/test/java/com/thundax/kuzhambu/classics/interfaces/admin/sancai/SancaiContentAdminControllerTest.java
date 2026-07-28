package com.thundax.kuzhambu.classics.interfaces.admin.sancai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairSortCommand;
import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentQaPairIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.SancaiContentAdminController;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiContentRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiContentSortRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class SancaiContentAdminControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void routesShouldKeepAdminApiPaths() throws Exception {
        assertRequestMapping(SancaiContentAdminController.class, "/api/classics/sancai/contents");
        assertPostMapping(SancaiContentAdminController.class, "listContents", "list", SancaiContentRequest.class);
        assertPostMapping(SancaiContentAdminController.class, "addContent", "add", SancaiContentRequest.class);
        assertPostMapping(SancaiContentAdminController.class, "updateContent", "update", SancaiContentRequest.class);
        assertPostMapping(SancaiContentAdminController.class, "deleteContent", "delete", SancaiContentRequest.class);
        assertPostMapping(SancaiContentAdminController.class, "sortContents", "sort", SancaiContentSortRequest.class);
    }

    @Test
    void requestAndResponseJsonFieldsShouldRemainStable() throws Exception {
        SancaiContentRequest request = OBJECT_MAPPER.readValue(
                """
                {
                  "id": 9001,
                  "entryId": 3001,
                  "question": "问",
                  "answer": "答",
                  "source": "MANUAL"
                }
                """,
                SancaiContentRequest.class);
        assertEquals(9001L, request.getId());
        assertEquals(3001L, request.getEntryId());
        assertJsonFields(request, "id", "entryId", "question", "answer", "source");

        SancaiContentSortRequest sortRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "orderedIds": [9001, 9002]
                }
                """,
                SancaiContentSortRequest.class);
        assertEquals(List.of(9001L, 9002L), sortRequest.getOrderedIds());
        assertJsonFields(sortRequest, "orderedIds");

        JsonNode response = OBJECT_MAPPER.valueToTree(SancaiContentAdminControllerTest.contentController()
                .listContents(request)
                .get(0));
        assertEquals(9001L, response.get("id").asLong());
        assertEquals(3001L, response.get("entryId").asLong());
        assertEquals("问", response.get("question").asText());
        assertEquals("答", response.get("answer").asText());
    }

    @Test
    void controllerShouldProxyContentServiceWithSancaiEntryType() {
        SancaiContentAdminController controller = contentController();
        SancaiContentRequest request = new SancaiContentRequest();
        request.setId(9001L);
        request.setEntryId(3001L);
        request.setQuestion("问");
        request.setAnswer("答");
        request.setSource("MANUAL");

        assertEquals(1, controller.listContents(request).size());
        assertEquals(9001L, controller.addContent(request).getId());
        assertEquals(9001L, controller.updateContent(request).getId());
        controller.deleteContent(request);

        SancaiContentSortRequest sortRequest = new SancaiContentSortRequest();
        sortRequest.setOrderedIds(List.of(9001L, 9002L));
        assertEquals(true, controller.sortContents(sortRequest));
    }

    private static SancaiContentAdminController contentController() {
        return new SancaiContentAdminController(contentService());
    }

    private static ClassicsContentApplicationService contentService() {
        return (ClassicsContentApplicationService) Proxy.newProxyInstance(
                ClassicsContentApplicationService.class.getClassLoader(),
                new Class<?>[] {ClassicsContentApplicationService.class},
                (proxy, method, args) -> {
                    if ("listQaPairs".equals(method.getName())) {
                        assertEquals("SANCAI_ENTRY", args[0]);
                        assertEquals(ClassicsContentIdCodec.toDomain(3001L), args[1]);
                        return List.of(content());
                    }
                    if ("addQaPair".equals(method.getName()) || "updateQaPair".equals(method.getName())) {
                        ContentQaPairCommand command = (ContentQaPairCommand) args[0];
                        assertEquals(9001L, command.getId());
                        assertEquals(ClassicsContentType.SANCAI_ENTRY, command.getContentType());
                        assertEquals(3001L, command.getContentId());
                        assertEquals("问", command.getQuestion());
                        assertEquals("答", command.getAnswer());
                        assertEquals(ClassicsContentSource.MANUAL, command.getSource());
                        return ClassicsContentQaPairIdCodec.toDomain(9001L);
                    }
                    if ("deleteQaPair".equals(method.getName())) {
                        assertEquals(ClassicsContentQaPairIdCodec.toDomain(9001L), args[0]);
                        return null;
                    }
                    if ("sortQaPairs".equals(method.getName())) {
                        ContentQaPairSortCommand command = (ContentQaPairSortCommand) args[0];
                        assertEquals(
                                List.of(
                                        ClassicsContentQaPairIdCodec.toDomain(9001L),
                                        ClassicsContentQaPairIdCodec.toDomain(9002L)),
                                command.getOrderedIds());
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static ClassicsContentQaPair content() {
        return new ClassicsContentQaPair(
                ClassicsContentQaPairIdCodec.toDomain(9001L),
                ClassicsContentType.SANCAI_ENTRY,
                ClassicsContentIdCodec.toDomain(3001L),
                "问",
                "答",
                ClassicsContentSource.MANUAL,
                1);
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
        JsonNode node = OBJECT_MAPPER.valueToTree(value);
        assertEquals(
                List.of(expectedFields),
                node.properties().stream().map(entry -> entry.getKey()).toList());
    }
}
