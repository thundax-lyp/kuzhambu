package com.thundax.kuzhambu.operations.application;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import com.thundax.kuzhambu.common.test.architecture.ImplContractArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.LayerArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.PathArchitectureRuleSupport;
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

        LayerArchitectureRuleSupport.assertApplicationServiceBoundaryClean(classes, Collections.emptyList());
        ImplContractArchitectureRuleSupport.assertImplClassesImplementNamedInterface(classes, Collections.emptySet());
        ImplContractArchitectureRuleSupport.assertProductionCodeDoesNotDependOnImplTypes(
                classes, Collections.emptySet());
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesDeclareNoMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertConfigurationClassNames(classes);
        PathArchitectureRuleSupport.assertConfigurationClassPlacement(classes);
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesAreRecords(
                Path.of("src/main/java"), Collections.emptyList());
        NamingArchitectureRuleSupport.assertApplicationCommandQueryConstructionInAssemblersOrApplicationServices(
                List.of(Path.of("src/main/java"), Path.of("../kuzhambu-operations-interface/src/main/java")),
                Collections.emptyList());
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
