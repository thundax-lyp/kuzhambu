package com.thundax.kuzhambu.discovery.interfaces;

import com.thundax.kuzhambu.common.test.architecture.ApiAnnotationArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ApiSurfaceArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import com.thundax.kuzhambu.common.test.architecture.BoundaryAssemblerNullnessAllowances;
import com.thundax.kuzhambu.common.test.architecture.ConcurrencyArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class DiscoveryInterfaceArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.discovery";

    @Test
    void interfaceApiSurfaceShouldKeepContractShape() throws Exception {
        JavaClasses classes = ConcurrencyArchitectureRuleSupport.importMainClasses(BASE_PACKAGE + ".interfaces");
        ConcurrencyArchitectureRuleSupport.shouldNotUseCompletableFutureAsyncWithoutExecutor(BASE_PACKAGE)
                .check(classes);
        ApiAnnotationArchitectureRuleSupport.assertControllerActionsUseVerbWhitelist(
                Path.of("src/main/java"), legacyActionVerbAllowances());
        ApiSurfaceArchitectureRuleSupport.assertApiModelsDoNotExposePriority(Path.of("src/main/java"));
        ApiSurfaceArchitectureRuleSupport.assertSortRequestsUseOrderedIdsOnly(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertBoundaryAssemblerPublicMethodsUseNonNullContracts(
                Collections.singletonList(Path.of("src/main/java")),
                BoundaryAssemblerNullnessAllowances.legacyClasses(
                        "com.thundax.kuzhambu.discovery.interfaces.admin.qa.assembler.DiscoveryQaAdminInterfaceAssembler",
                        "com.thundax.kuzhambu.discovery.interfaces.admin.search.assembler.DiscoverySearchStatisticsInterfaceAssembler",
                        "com.thundax.kuzhambu.discovery.interfaces.portal.qa.assembler.DiscoveryQaPortalInterfaceAssembler",
                        "com.thundax.kuzhambu.discovery.interfaces.portal.search.assembler.DiscoverySearchPortalInterfaceAssembler"));
    }

    private static List<ArchitectureRuleAllowance> legacyActionVerbAllowances() {
        return List.of(
                actionVerbAllowance("DiscoveryQaPortalStreamController"),
                actionVerbAllowance("DiscoveryQaPortalController"),
                actionVerbAllowance("DiscoveryQaConversationStreamController"),
                actionVerbAllowance("DiscoveryQaAdminController"),
                actionVerbAllowance("DiscoveryQaConversationController"),
                actionVerbAllowance("DiscoverySearchStatisticsController"));
    }

    private static ArchitectureRuleAllowance actionVerbAllowance(String controller) {
        return ArchitectureRuleAllowance.of(
                "CONTROLLER_ACTION_VERB:*" + controller + ".java*",
                "Discovery controller retains legacy action names or paths outside the shared verb whitelist.",
                "Rename the controller method and action path with a shared verb, update callers, then remove this allowance.");
    }
}
