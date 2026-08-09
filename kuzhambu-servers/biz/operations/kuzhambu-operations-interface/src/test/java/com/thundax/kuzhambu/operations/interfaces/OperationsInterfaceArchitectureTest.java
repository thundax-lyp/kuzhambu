package com.thundax.kuzhambu.operations.interfaces;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.ApiAnnotationArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ApiSurfaceArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import com.thundax.kuzhambu.common.test.architecture.BoundaryAssemblerNullnessAllowances;
import com.thundax.kuzhambu.common.test.architecture.ModelAnnotationArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class OperationsInterfaceArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.operations";

    @Test
    void interfaceApiSurfaceShouldKeepContractShape() throws Exception {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".interfaces");

        ApiAnnotationArchitectureRuleSupport.requestClassAnnotationsRequired(BASE_PACKAGE)
                .check(classes);
        ModelAnnotationArchitectureRuleSupport.assertResponseClassAnnotationsRequired(
                classes, BASE_PACKAGE, legacyResponseAnnotationAllowances());
        ApiAnnotationArchitectureRuleSupport.assertControllerActionsUseVerbWhitelist(
                Path.of("src/main/java"), legacyActionVerbAllowances());
        ApiAnnotationArchitectureRuleSupport.assertPostMappingMethodsUseRequestResponseShape(Path.of("src/main/java"));
        ApiSurfaceArchitectureRuleSupport.assertApiModelsDoNotExposePriority(Path.of("src/main/java"));
        ApiSurfaceArchitectureRuleSupport.assertSortRequestsUseOrderedIdsOnly(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertBoundaryAssemblerPublicMethodsUseNonNullContracts(
                Collections.singletonList(Path.of("src/main/java")),
                BoundaryAssemblerNullnessAllowances.legacyClasses(
                        "com.thundax.kuzhambu.operations.interfaces.admin.report.assembler.OperationsReportInterfaceAssembler",
                        "com.thundax.kuzhambu.operations.interfaces.admin.restore.assembler.OperationsRestoreInterfaceAssembler",
                        "com.thundax.kuzhambu.operations.interfaces.admin.task.assembler.OperationsTaskInterfaceAssembler"));
    }

    private static List<ArchitectureRuleAllowance> legacyActionVerbAllowances() {
        return List.of(
                actionVerbAllowance("OperationsBackupAdminController"),
                actionVerbAllowance("OperationsCleanupAdminController"),
                actionVerbAllowance("OperationsDashboardAdminController"),
                actionVerbAllowance("OperationsHealthAdminController"),
                actionVerbAllowance("OperationsHealthAlertAdminController"),
                actionVerbAllowance("OperationsReportAdminController"),
                actionVerbAllowance("OperationsRestoreAdminController"),
                actionVerbAllowance("OperationsTaskAdminController"));
    }

    private static List<ArchitectureRuleAllowance> legacyResponseAnnotationAllowances() {
        return java.util.Arrays.stream(new String[] {
                    "admin.backup.controller.response.OperationsBackupDetailResponse",
                    "admin.backup.controller.response.OperationsBackupExecuteResponse",
                    "admin.backup.controller.response.OperationsBackupPageResponse",
                    "admin.cleanup.controller.response.OperationsCleanupDetailResponse",
                    "admin.cleanup.controller.response.OperationsCleanupExecuteResponse",
                    "admin.cleanup.controller.response.OperationsCleanupPageResponse",
                    "admin.dashboard.controller.response.OperationsDashboardOverviewResponse",
                    "admin.dashboard.controller.response.OperationsDashboardOverviewResponse$BucketCountResponse",
                    "admin.dashboard.controller.response.OperationsDashboardOverviewResponse$TaskStatusSummaryResponse",
                    "admin.dashboard.controller.response.OperationsDashboardOverviewResponse$TopAiCapabilityResponse",
                    "admin.dashboard.controller.response.OperationsDashboardOverviewResponse$TopContentResponse",
                    "admin.dashboard.controller.response.OperationsDashboardOverviewResponse$TopQueryResponse",
                    "admin.dashboard.controller.response.OperationsDashboardOverviewResponse$TopTagResponse",
                    "admin.health.controller.response.OperationsHealthAlertPageResponse",
                    "admin.health.controller.response.OperationsHealthAlertSummaryResponse",
                    "admin.health.controller.response.OperationsHealthPageResponse",
                    "admin.health.controller.response.OperationsHealthSummaryResponse",
                    "admin.health.controller.response.OperationsHealthTrendResponse",
                    "admin.report.controller.response.OperationsReportDetailResponse",
                    "admin.report.controller.response.OperationsReportGenerateResponse",
                    "admin.report.controller.response.OperationsReportPageResponse",
                    "admin.restore.controller.response.OperationsRestoreDetailResponse",
                    "admin.restore.controller.response.OperationsRestoreExecuteResponse",
                    "admin.restore.controller.response.OperationsRestorePageResponse",
                    "admin.task.controller.response.OperationsTaskDetailResponse",
                    "admin.task.controller.response.OperationsTaskPageResponse"
                })
                .map(
                        className -> ArchitectureRuleAllowance.of(
                                ModelAnnotationArchitectureRuleSupport.NAME_RESPONSE_REQUIRED_ANNOTATIONS
                                        + ":"
                                        + BASE_PACKAGE
                                        + ".interfaces."
                                        + className,
                                "Operations legacy API model is pending annotation normalization.",
                                "Add the required model annotations or migrate the protocol shape, then remove this allowance."))
                .toList();
    }

    private static ArchitectureRuleAllowance actionVerbAllowance(String controller) {
        return ArchitectureRuleAllowance.of(
                "CONTROLLER_ACTION_VERB:*" + controller + ".java*",
                "Operations controller retains legacy action names or paths outside the shared verb whitelist.",
                "Rename the controller method and action path with a shared verb, update callers, then remove this allowance.");
    }
}
