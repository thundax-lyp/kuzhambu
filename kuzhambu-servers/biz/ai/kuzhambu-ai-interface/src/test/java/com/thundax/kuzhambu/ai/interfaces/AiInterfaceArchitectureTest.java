package com.thundax.kuzhambu.ai.interfaces;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.ApiAnnotationArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ApiSurfaceArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import com.thundax.kuzhambu.common.test.architecture.ConcurrencyArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.InterfaceBoundaryArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ModuleAndDependencyArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.SpringBeanArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class AiInterfaceArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.ai";

    @Test
    void interfaceLayerShouldKeepArchitectureBoundary() throws Exception {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".interfaces");

        ApiAnnotationArchitectureRuleSupport.requestClassAnnotationsRequired(BASE_PACKAGE)
                .check(classes);
        ApiAnnotationArchitectureRuleSupport.responseClassAnnotationsRequired(BASE_PACKAGE)
                .check(classes);
        ModuleAndDependencyArchitectureRuleSupport.assertInterfaceLayerBoundary(classes, BASE_PACKAGE);
        ModuleAndDependencyArchitectureRuleSupport.assertCrossDomainDependencyBoundary(classes, "ai");
        InterfaceBoundaryArchitectureRuleSupport.assertInterfaceNoPersistenceDependency(classes, BASE_PACKAGE);
        InterfaceBoundaryArchitectureRuleSupport.assertInterfaceOnlyCallsApplicationServices(classes, BASE_PACKAGE);
        InterfaceBoundaryArchitectureRuleSupport.assertInterfaceProtocolModelsStayInSameSubdomain(
                Path.of("src/main/java"));
        InterfaceBoundaryArchitectureRuleSupport.assertInterfaceProtocolsDoNotExposeDomainModels(
                Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertValueObjectPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertValueObjectIdSourcesDeclareNoStaticMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertEntityPlacement(classes, BASE_PACKAGE);
        SpringBeanArchitectureRuleSupport.assertDirectSpringBeansHaveSingleConstructor(classes);
        ConcurrencyArchitectureRuleSupport.shouldNotUseCompletableFutureAsyncWithoutExecutor(BASE_PACKAGE)
                .check(classes);
        ApiAnnotationArchitectureRuleSupport.assertAdminControllersDeclareRequiredClassAnnotations(
                Path.of("src/main/java"));
        ApiAnnotationArchitectureRuleSupport.assertAdminControllerMethodsDeclareRequiredAnnotations(
                Path.of("src/main/java"));
        ApiAnnotationArchitectureRuleSupport.assertControllerActionsUseVerbWhitelist(
                Path.of("src/main/java"), legacyActionVerbAllowances());
        ApiAnnotationArchitectureRuleSupport.assertPostMappingMethodsUseRequestResponseShape(Path.of("src/main/java"));
        ApiAnnotationArchitectureRuleSupport.assertPostMappingMethodsDoNotUsePathOrQueryParameters(
                Path.of("src/main/java"));
        ApiSurfaceArchitectureRuleSupport.assertApiModelsDoNotExposePriority(Path.of("src/main/java"));
        ApiSurfaceArchitectureRuleSupport.assertSortRequestsUseOrderedIdsOnly(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertBoundaryAssemblerPublicMethodsUseNonNullContracts(
                Collections.singletonList(Path.of("src/main/java")), Collections.emptyList());
    }

    private static List<ArchitectureRuleAllowance> legacyActionVerbAllowances() {
        return List.of(
                aiInvocationActionVerbAllowance("method=summarizeInvocationLogs"),
                aiInvocationActionVerbAllowance("method=summarizeInvocationLogs path=invocation-log/summary"),
                aiInvocationActionVerbAllowance("method=markCandidateApplied"),
                aiInvocationActionVerbAllowance("method=markCandidateApplied path=candidate/mark-applied"),
                aiInvocationActionVerbAllowance("method=recordBatchSuccess"),
                aiInvocationActionVerbAllowance("method=recordBatchSuccess path=batch/record-success"),
                aiInvocationActionVerbAllowance("method=recordBatchFailure"),
                aiInvocationActionVerbAllowance("method=recordBatchFailure path=batch/record-failure"),
                aiInvocationActionVerbAllowance("method=canDispatchBatch"),
                aiInvocationActionVerbAllowance("method=canDispatchBatch path=batch/can-dispatch"),
                promptActionVerbAllowance("method=getTemplateByCapability path=template/get-by-capability"),
                promptActionVerbAllowance("method=saveTemplate"),
                promptActionVerbAllowance("method=saveTemplate path=template/save"),
                promptActionVerbAllowance("method=getCurrentVersion path=version/current"),
                promptActionVerbAllowance("method=compareVersions"),
                promptActionVerbAllowance("method=compareVersions path=version/compare"),
                promptActionVerbAllowance("method=rollbackVersion"),
                promptActionVerbAllowance("method=rollbackVersion path=version/rollback"),
                promptActionVerbAllowance("method=validateVariables"),
                promptActionVerbAllowance("method=validateVariables path=variable/validate"),
                promptActionVerbAllowance("method=buildOptimizationSuggestion"),
                promptActionVerbAllowance("method=buildOptimizationSuggestion path=optimization/suggest"),
                platformAiActionVerbAllowance("method=buildPromptSuggestion"),
                platformAiActionVerbAllowance("method=buildPromptSuggestion path=prompt-suggestion"),
                platformAiActionVerbAllowance("method=summarizeVersion"),
                platformAiActionVerbAllowance("method=summarizeVersion path=version-summary"),
                legacyControllerActionVerbAllowance("AiRefinementTaskController"),
                legacyControllerActionVerbAllowance("AiRefinementController"));
    }

    private static ArchitectureRuleAllowance aiInvocationActionVerbAllowance(String violation) {
        return ArchitectureRuleAllowance.of(
                "CONTROLLER_ACTION_VERB:kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/"
                        + "kuzhambu/ai/interfaces/admin/invocation/controller/AiInvocationController.java "
                        + violation,
                "AI invocation endpoints retain legacy action names outside the shared verb whitelist.",
                "Rename the method and action path with shared verbs, update callers, then remove this allowance.");
    }

    private static ArchitectureRuleAllowance promptActionVerbAllowance(String violation) {
        return ArchitectureRuleAllowance.of(
                "CONTROLLER_ACTION_VERB:kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/"
                        + "kuzhambu/ai/interfaces/admin/config/prompt/controller/PromptController.java "
                        + violation,
                "Prompt endpoints retain legacy action names outside the shared verb whitelist.",
                "Rename the method and action path with shared verbs, update callers, then remove this allowance.");
    }

    private static ArchitectureRuleAllowance platformAiActionVerbAllowance(String violation) {
        return ArchitectureRuleAllowance.of(
                "CONTROLLER_ACTION_VERB:kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/"
                        + "kuzhambu/ai/interfaces/admin/platform/controller/PlatformAiController.java "
                        + violation,
                "AI platform endpoints retain legacy action names outside the shared verb whitelist.",
                "Rename the method and action path with shared verbs, update callers, then remove this allowance.");
    }

    private static ArchitectureRuleAllowance legacyControllerActionVerbAllowance(String controller) {
        return ArchitectureRuleAllowance.of(
                "CONTROLLER_ACTION_VERB:*" + controller + ".java*",
                "AI controller retains legacy action names or paths outside the shared verb whitelist.",
                "Rename the controller method and action path with a shared verb, update callers, then remove this allowance.");
    }
}
