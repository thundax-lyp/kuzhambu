package com.thundax.kuzhambu.knowledge.interfaces.admin.graph;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.exception.ApiException;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphBatchWithdrawalCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialNodeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphBatchWithdrawalResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphWithdrawalResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphExtractionApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphMaterialApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphPublicationApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphPublishedApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphWorkbenchApplicationService;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphMaterialRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphPublicationRequests;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GraphControllerTest {

    @Test
    void shouldKeepReadAndWritePermissionsOnAdminEndpoints() throws Exception {
        assertThat(permission("materialPage")).isEqualTo("knowledge:graph:view");
        assertThat(permission("materialGet")).isEqualTo("knowledge:graph:view");
        assertThat(permission("materialNodeCreate")).isEqualTo("knowledge:graph:edit");
        assertThat(permission("taskPage")).isEqualTo("knowledge:graph:view");
        assertThat(permission("taskGet")).isEqualTo("knowledge:graph:view");
        assertThat(permission("extractionCreate")).isEqualTo("knowledge:graph:edit");
        assertThat(permission("candidateApply")).isEqualTo("knowledge:graph:edit");
        assertThat(permission("publicationPublish")).isEqualTo("knowledge:graph:edit");
        assertThat(permission("withdrawalBatchPreview")).isEqualTo("knowledge:graph:view");
        assertThat(permission("withdrawalBatch")).isEqualTo("knowledge:graph:edit");
        assertThat(permission("publishedNodeDelete")).isEqualTo("knowledge:graph:edit");
    }

    @Test
    void shouldMapMaterialNodeCreateThroughAssemblerAndApplicationService() {
        GraphMaterialApplicationService materialService = mock(GraphMaterialApplicationService.class);
        GraphController controller = controller(materialService);
        GraphMaterialRequests.MaterialObjectRequest request = materialNodeRequest();
        when(materialService.getMaterialGraph(any()))
                .thenReturn(new GraphMaterialResult(
                        null,
                        new GraphMaterial(
                                new ContentRef("SANCAI_ENTRY", 1001L), "三才图会", GraphMaterialStatus.DRAFT, null, 7),
                        null,
                        List.of(),
                        List.of(),
                        null));

        var response = controller.materialNodeCreate(request);

        ArgumentCaptor<GraphMaterialNodeCommand> captor = ArgumentCaptor.forClass(GraphMaterialNodeCommand.class);
        verify(materialService).createNode(captor.capture());
        GraphMaterialNodeCommand command = captor.getValue();
        assertThat(command.materialLockVersion()).isEqualTo(7);
        assertThat(command.node().getMaterialRef()).isEqualTo(new ContentRef("SANCAI_ENTRY", 1001L));
        assertThat(command.node().getName()).isEqualTo("张三");
        assertThat(command.node().getPropertiesJson()).contains("identityQualifier");
        assertThat(response.material().contentRef().contentRefId()).isEqualTo("1001");
    }

    @Test
    void shouldPropagateBusinessErrorForUnifiedApiExceptionMapping() {
        GraphMaterialApplicationService materialService = mock(GraphMaterialApplicationService.class);
        GraphController controller = controller(materialService);
        GraphMaterialRequests.MaterialObjectRequest request = materialNodeRequest();
        when(materialService.createNode(any())).thenThrow(new ApiException("GRAPH_LOCK_CONFLICT"));

        assertThrows(ApiException.class, () -> controller.materialNodeCreate(request));
    }

    @Test
    void shouldMapBatchWithdrawalThroughAssemblerAndApplicationService() {
        GraphPublicationApplicationService publicationService = mock(GraphPublicationApplicationService.class);
        GraphController controller = controller(publicationService);
        GraphPublicationRequests.BatchWithdrawalRequest request = batchWithdrawalRequest();
        when(publicationService.withdrawBatch(any()))
                .thenReturn(new GraphBatchWithdrawalResult(
                        "batch-001",
                        List.of(new GraphWithdrawalResult(
                                new ContentRef("SANCAI_ENTRY", 1001L),
                                true,
                                new GraphMaterial(
                                        new ContentRef("SANCAI_ENTRY", 1001L),
                                        "三才图会",
                                        GraphMaterialStatus.DRAFT,
                                        null,
                                        8L),
                                null,
                                null))));

        var response = controller.withdrawalBatch(request);

        ArgumentCaptor<GraphBatchWithdrawalCommand> captor = ArgumentCaptor.forClass(GraphBatchWithdrawalCommand.class);
        verify(publicationService).withdrawBatch(captor.capture());
        assertThat(captor.getValue().idempotencyKey()).isEqualTo("batch-001");
        assertThat(captor.getValue().materials()).hasSize(2);
        assertThat(captor.getValue().materials().get(1).materialRef()).isEqualTo(new ContentRef("SANCAI_ENTRY", 1002L));
        assertThat(captor.getValue().materials().get(1).materialLockVersion()).isEqualTo(9L);
        assertThat(response.materials())
                .extracting(item -> item.contentRef().contentRefId())
                .containsExactly("1001");
    }

    private static String permission(String methodName) {
        for (Method method : GraphController.class.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method.getAnnotation(HasPermission.class).value()[0];
            }
        }
        throw new AssertionError("missing method " + methodName);
    }

    private static GraphController controller(GraphMaterialApplicationService materialService) {
        return new GraphController(
                mock(GraphWorkbenchApplicationService.class),
                materialService,
                mock(GraphExtractionApplicationService.class),
                mock(GraphPublicationApplicationService.class),
                mock(GraphPublishedApplicationService.class));
    }

    private static GraphController controller(GraphPublicationApplicationService publicationService) {
        return new GraphController(
                mock(GraphWorkbenchApplicationService.class),
                mock(GraphMaterialApplicationService.class),
                mock(GraphExtractionApplicationService.class),
                publicationService,
                mock(GraphPublishedApplicationService.class));
    }

    private static GraphMaterialRequests.MaterialObjectRequest materialNodeRequest() {
        GraphMaterialRequests.MaterialObjectRequest request = new GraphMaterialRequests.MaterialObjectRequest();
        request.setContentType("SANCAI_ENTRY");
        request.setContentRefId("1001");
        request.setMaterialLockVersion("7");
        GraphMaterialRequests.MaterialObjectRequestData node = new GraphMaterialRequests.MaterialObjectRequestData();
        node.setNodeType("PERSON");
        node.setName("张三");
        node.setSource("MANUAL");
        node.setProperties(Map.of("identityQualifier", "明代"));
        request.setNode(node);
        return request;
    }

    private static GraphPublicationRequests.BatchWithdrawalRequest batchWithdrawalRequest() {
        GraphPublicationRequests.BatchWithdrawalRequest request = new GraphPublicationRequests.BatchWithdrawalRequest();
        request.setIdempotencyKey("batch-001");
        GraphPublicationRequests.WithdrawalRequest first = withdrawalRequest("1001", "8");
        GraphPublicationRequests.WithdrawalRequest second = withdrawalRequest("1002", "9");
        request.setMaterials(List.of(first, second));
        return request;
    }

    private static GraphPublicationRequests.WithdrawalRequest withdrawalRequest(
            String contentRefId, String lockVersion) {
        GraphPublicationRequests.WithdrawalRequest request = new GraphPublicationRequests.WithdrawalRequest();
        request.setContentType("SANCAI_ENTRY");
        request.setContentRefId(contentRefId);
        request.setMaterialLockVersion(lockVersion);
        return request;
    }
}
