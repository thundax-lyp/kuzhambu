package com.thundax.kuzhambu.operations.interfaces;

import com.thundax.kuzhambu.common.test.architecture.ApiSurfaceArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.BoundaryAssemblerNullnessAllowances;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import java.nio.file.Path;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class OperationsInterfaceArchitectureTest {

    @Test
    void interfaceApiSurfaceShouldKeepContractShape() throws Exception {
        ApiSurfaceArchitectureRuleSupport.assertApiModelsDoNotExposePriority(Path.of("src/main/java"));
        ApiSurfaceArchitectureRuleSupport.assertSortRequestsUseOrderedIdsOnly(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertBoundaryAssemblerPublicMethodsUseNonNullContracts(
                Collections.singletonList(Path.of("src/main/java")),
                BoundaryAssemblerNullnessAllowances.legacyClasses(
                        "com.thundax.kuzhambu.operations.interfaces.admin.backup.assembler.OperationsBackupInterfaceAssembler",
                        "com.thundax.kuzhambu.operations.interfaces.admin.cleanup.assembler.OperationsCleanupInterfaceAssembler",
                        "com.thundax.kuzhambu.operations.interfaces.admin.dashboard.assembler.OperationsDashboardInterfaceAssembler",
                        "com.thundax.kuzhambu.operations.interfaces.admin.health.assembler.OperationsHealthAlertInterfaceAssembler",
                        "com.thundax.kuzhambu.operations.interfaces.admin.health.assembler.OperationsHealthInterfaceAssembler",
                        "com.thundax.kuzhambu.operations.interfaces.admin.report.assembler.OperationsReportInterfaceAssembler",
                        "com.thundax.kuzhambu.operations.interfaces.admin.restore.assembler.OperationsRestoreInterfaceAssembler",
                        "com.thundax.kuzhambu.operations.interfaces.admin.task.assembler.OperationsTaskInterfaceAssembler"));
    }
}
