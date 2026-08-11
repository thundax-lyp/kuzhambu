package com.thundax.kuzhambu.discovery.application;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import com.thundax.kuzhambu.common.test.architecture.ImplContractArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.LayerArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.PathArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.TransactionArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class DiscoveryApplicationArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.discovery";

    @Test
    void applicationCommandAndQuerySourcesShouldDeclareFieldsOnly() {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".application");

        TransactionArchitectureRuleSupport.assertTransactionalOnlyOnApplicationServiceUseCases(classes, BASE_PACKAGE);
        LayerArchitectureRuleSupport.assertApplicationServiceBoundaryClean(classes, Collections.emptyList());
        NamingArchitectureRuleSupport.assertConfigurationClassNames(classes);
        PathArchitectureRuleSupport.assertConfigurationClassPlacement(classes);
        ImplContractArchitectureRuleSupport.assertImplClassesImplementNamedInterface(classes, Collections.emptySet());
        ImplContractArchitectureRuleSupport.assertProductionCodeDoesNotDependOnImplTypes(
                classes, Collections.emptySet());
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesDeclareNoMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesAreRecords(
                Path.of("src/main/java"), Collections.emptyList());
        NamingArchitectureRuleSupport.assertApplicationCommandQueryConstructionInAssemblersOrApplicationServices(
                List.of(Path.of("src/main/java"), Path.of("../kuzhambu-discovery-interface/src/main/java")),
                legacyCommandQueryConstructionAllowances());
        NamingArchitectureRuleSupport.assertAssemblersDoNotReturnNullApplicationCommandOrQuery(
                List.of(Path.of("src/main/java"), Path.of("../kuzhambu-discovery-interface/src/main/java")),
                legacyAssemblerNullReturnAllowances());
        NamingArchitectureRuleSupport.assertApplicationQueriesDoNotOwnPageState(
                Path.of("src/main/java"), Collections.emptyList());
        NamingArchitectureRuleSupport.assertApplicationContractSourcesUnderDedicatedPackages(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertBoundaryAssemblerPublicMethodsUseNonNullContracts(
                Collections.singletonList(Path.of("src/main/java")), Collections.emptyList());
    }

    private static List<ArchitectureRuleAllowance> legacyCommandQueryConstructionAllowances() {
        return List.of();
    }

    private static ArchitectureRuleAllowance constructionViolation(String ownerAndType) {
        return ArchitectureRuleAllowance.of(
                "COMMAND_QUERY_CONSTRUCTION:" + ownerAndType,
                "Discovery QA admin controller directly constructs an application Command/Query.",
                "Move request conversion into DiscoveryQaInterfaceAssembler, then remove this allowance.");
    }

    private static List<ArchitectureRuleAllowance> legacyAssemblerNullReturnAllowances() {
        return List.of();
    }

    private static ArchitectureRuleAllowance nullReturnViolation(String ownerMethodAndType) {
        return ArchitectureRuleAllowance.of(
                "COMMAND_QUERY_ASSEMBLER_NULL_RETURN:" + ownerMethodAndType,
                "Discovery interface assembler returns null for an application Command/Query on null input.",
                "Validate inputs in the caller or model the absence explicitly, then return a concrete application contract.");
    }

    private static ArchitectureRuleAllowance rawParameters(String key) {
        return ArchitectureRuleAllowance.of(
                key,
                "Discovery ApplicationService method uses naked Long/String/Integer or scattered query parameters.",
                "Introduce a dedicated Query/Command or strong domain value object for the method input, then remove this allowance.");
    }
}
