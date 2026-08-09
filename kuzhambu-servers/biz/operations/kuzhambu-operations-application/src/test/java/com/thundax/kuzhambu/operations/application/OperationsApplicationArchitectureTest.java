package com.thundax.kuzhambu.operations.application;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import com.thundax.kuzhambu.common.test.architecture.ImplContractArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.LayerArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.SourceHardRuleArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class OperationsApplicationArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.operations";

    @Test
    void applicationContractsShouldStayInDedicatedPackages() throws Exception {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".application");

        LayerArchitectureRuleSupport.assertApplicationServiceBoundaryClean(
                classes, legacyApplicationServiceBoundaryAllowances());
        ImplContractArchitectureRuleSupport.assertImplClassesImplementNamedInterface(classes, Collections.emptySet());
        ImplContractArchitectureRuleSupport.assertProductionCodeDoesNotDependOnImplTypes(
                classes, Collections.emptySet());
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesDeclareNoMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesAreRecords(
                Path.of("src/main/java"), OperationsApplicationCommandQueryRecordAllowances.legacyAllowances());
        NamingArchitectureRuleSupport.assertApplicationCommandQueryConstructionInAssemblersOrApplicationServices(
                List.of(Path.of("src/main/java"), Path.of("../kuzhambu-operations-interface/src/main/java")),
                legacyCommandQueryConstructionAllowances());
        NamingArchitectureRuleSupport.assertAssemblersDoNotReturnNullApplicationCommandOrQuery(
                List.of(Path.of("src/main/java"), Path.of("../kuzhambu-operations-interface/src/main/java")),
                legacyAssemblerNullReturnAllowances());
        NamingArchitectureRuleSupport.assertApplicationQueriesDoNotOwnPageState(
                Path.of("src/main/java"), Collections.emptyList());
        NamingArchitectureRuleSupport.assertApplicationContractSourcesUnderDedicatedPackages(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertBoundaryAssemblerPublicMethodsUseNonNullContracts(
                Collections.singletonList(Path.of("src/main/java")), Collections.emptyList());
        SourceHardRuleArchitectureRuleSupport.assertConfigurationPropertiesDoNotDeclareBusinessControlFlow(
                Path.of("src/main/java"));
    }

    private static List<ArchitectureRuleAllowance> legacyApplicationServiceBoundaryAllowances() {
        return List.of(
                ArchitectureRuleAllowance.of(
                        "METHOD_SHAPE:com.thundax.kuzhambu.operations.application.backup.service."
                                + "BackupApplicationService.executeAutoBackup()",
                        "BackupApplicationService.executeAutoBackup is a legacy no-argument write/maintenance operation.",
                        "Introduce an explicit OperationsBackupExecuteCommand or scheduler-only support service boundary, then remove this allowance."));
    }

    private static List<ArchitectureRuleAllowance> legacyCommandQueryConstructionAllowances() {
        return List.of(
                constructionViolation(
                        "com.thundax.kuzhambu.operations.application.cleanup.support.OperationsCleanupScheduler#OperationsCleanupExecuteCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.operations.interfaces.admin.report.controller.OperationsReportAdminController#OperationsReportDetailQuery:1"));
    }

    private static ArchitectureRuleAllowance constructionViolation(String ownerAndType) {
        return ArchitectureRuleAllowance.of(
                "COMMAND_QUERY_CONSTRUCTION:" + ownerAndType,
                "Operations legacy scheduler or controller constructs an application Command/Query directly.",
                "Move conversion into the corresponding InterfaceAssembler or application orchestration boundary, then remove this allowance.");
    }

    private static List<ArchitectureRuleAllowance> legacyAssemblerNullReturnAllowances() {
        return List.of(
                nullReturn(
                        "com.thundax.kuzhambu.operations.interfaces.admin.task.assembler.OperationsTaskInterfaceAssembler#toQuery:OperationsTaskQuery:1"),
                nullReturn(
                        "com.thundax.kuzhambu.operations.interfaces.admin.task.assembler.OperationsTaskInterfaceAssembler#toQuery:OperationsTaskDetailQuery:1"),
                nullReturn(
                        "com.thundax.kuzhambu.operations.interfaces.admin.report.assembler.OperationsReportInterfaceAssembler#toCommand:OperationsReportGenerateCommand:1"),
                nullReturn(
                        "com.thundax.kuzhambu.operations.interfaces.admin.report.assembler.OperationsReportInterfaceAssembler#toQuery:OperationsReportQuery:1"),
                nullReturn(
                        "com.thundax.kuzhambu.operations.interfaces.admin.report.assembler.OperationsReportInterfaceAssembler#toQuery:OperationsReportDetailQuery:1"),
                nullReturn(
                        "com.thundax.kuzhambu.operations.interfaces.admin.restore.assembler.OperationsRestoreInterfaceAssembler#toCommand:OperationsRestoreExecuteCommand:1"),
                nullReturn(
                        "com.thundax.kuzhambu.operations.interfaces.admin.restore.assembler.OperationsRestoreInterfaceAssembler#toQuery:OperationsRestoreQuery:1"),
                nullReturn(
                        "com.thundax.kuzhambu.operations.interfaces.admin.restore.assembler.OperationsRestoreInterfaceAssembler#toQuery:OperationsRestoreDetailQuery:1"));
    }

    private static ArchitectureRuleAllowance nullReturn(String method) {
        return ArchitectureRuleAllowance.of(
                "COMMAND_QUERY_ASSEMBLER_NULL_RETURN:" + method,
                "Operations legacy interface assembler returns null for an application Command/Query when its request is absent.",
                "Move optional request handling to the controller and make the assembler return a concrete contract, then remove this allowance.");
    }
}
