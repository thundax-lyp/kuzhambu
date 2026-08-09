package com.thundax.kuzhambu.common.test.architecture;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class RepositoryApiHardRulesArchitectureTest {

    private static final Path BUSINESS_SOURCE_ROOT = Path.of("../../biz");
    private static final Pattern ADMIN_WEB_SERVICE_VERBS_PATTERN =
            Pattern.compile("(?s)const SERVICE_METHOD_VERBS = \\[(.*?)\\];");
    private static final Pattern QUOTED_VALUE_PATTERN = Pattern.compile("\\\"([^\\\"]+)\\\"");

    @Test
    void apiSourceShouldKeepHardRuleContract() throws Exception {
        ApiAnnotationArchitectureRuleSupport.assertOperationDeclaresAccessAnnotation(BUSINESS_SOURCE_ROOT);
        ApiAnnotationArchitectureRuleSupport.assertRestControllersDeclareRequestMapping(BUSINESS_SOURCE_ROOT);
        ApiAnnotationArchitectureRuleSupport.assertRestControllerRequestMappingsUseApiResourcePath(
                BUSINESS_SOURCE_ROOT);
        ApiAnnotationArchitectureRuleSupport.assertRestControllersDeclareApi(BUSINESS_SOURCE_ROOT);
        ApiAnnotationArchitectureRuleSupport.assertApiTagsDoNotUseNumericPrefix(BUSINESS_SOURCE_ROOT);
        ApiAnnotationArchitectureRuleSupport.assertMappedMethodsDeclareOperation(BUSINESS_SOURCE_ROOT);
        ApiAnnotationArchitectureRuleSupport.assertMappedMethodsDeclareSingleHttpMapping(BUSINESS_SOURCE_ROOT);
        ApiAnnotationArchitectureRuleSupport.assertMappedMethodsUsePostOrGetMapping(BUSINESS_SOURCE_ROOT);
        ApiAnnotationArchitectureRuleSupport.assertJsonRequestMethodsUsePostMapping(BUSINESS_SOURCE_ROOT);
        ApiAnnotationArchitectureRuleSupport.assertGetMappingMethodsReturnVoid(BUSINESS_SOURCE_ROOT);
        ApiAnnotationArchitectureRuleSupport.assertControllerActionsUseVerbWhitelist(
                BUSINESS_SOURCE_ROOT, legacyActionVerbAllowances());
        ApiAnnotationArchitectureRuleSupport.assertRequestBodyRequestParametersDeclareValid(BUSINESS_SOURCE_ROOT);
        ApiAnnotationArchitectureRuleSupport.assertControllersDoNotCreateResponses(BUSINESS_SOURCE_ROOT);
    }

    @Test
    void controllerActionVerbsShouldMatchAdminWebServiceVerbs() throws Exception {
        String eslintConfig = Files.readString(
                ArchitectureSourceSupport.repositoryRoot().resolve("kuzhambu-apps/admin-web/eslint.config.js"));
        Matcher verbBlock = ADMIN_WEB_SERVICE_VERBS_PATTERN.matcher(eslintConfig);
        Assertions.assertThat(verbBlock.find()).isTrue();

        List<String> frontendVerbs = new ArrayList<String>();
        Matcher valueMatcher = QUOTED_VALUE_PATTERN.matcher(verbBlock.group(1));
        while (valueMatcher.find()) {
            frontendVerbs.add(valueMatcher.group(1));
        }

        Assertions.assertThat(frontendVerbs)
                .containsExactlyElementsOf(ApiAnnotationArchitectureRuleSupport.controllerActionVerbs());
    }

    private static List<ArchitectureRuleAllowance> legacyActionVerbAllowances() {
        return List.of(
                actionVerbAllowance("AiInvocationController", "AI"),
                actionVerbAllowance("PromptController", "AI"),
                actionVerbAllowance("PlatformAiController", "AI"),
                actionVerbAllowance("AiRefinementTaskController", "AI"),
                actionVerbAllowance("AiRefinementController", "AI"),
                actionVerbAllowance("DiscoveryQaPortalStreamController", "Discovery"),
                actionVerbAllowance("DiscoveryQaPortalController", "Discovery"),
                actionVerbAllowance("DiscoveryQaConversationStreamController", "Discovery"),
                actionVerbAllowance("DiscoveryQaAdminController", "Discovery"),
                actionVerbAllowance("DiscoveryQaConversationController", "Discovery"),
                actionVerbAllowance("DiscoverySearchStatisticsController", "Discovery"),
                actionVerbAllowance("OperationsBackupAdminController", "Operations"),
                actionVerbAllowance("OperationsCleanupAdminController", "Operations"),
                actionVerbAllowance("OperationsDashboardAdminController", "Operations"),
                actionVerbAllowance("OperationsHealthAdminController", "Operations"),
                actionVerbAllowance("OperationsHealthAlertAdminController", "Operations"),
                actionVerbAllowance("OperationsReportAdminController", "Operations"),
                actionVerbAllowance("OperationsRestoreAdminController", "Operations"),
                actionVerbAllowance("OperationsTaskAdminController", "Operations"),
                actionVerbAllowance("StorageObjectController", "Storage"),
                actionVerbAllowance("AuditController", "System"),
                actionVerbAllowance("AuthController", "System"),
                actionVerbAllowance("CaptchaController", "System"),
                actionVerbAllowance("CurrentUserController", "System"),
                actionVerbAllowance("DepartmentController", "System"),
                actionVerbAllowance("MenuController", "System"),
                actionVerbAllowance("RoleController", "System"),
                actionVerbAllowance("UserController", "System"),
                actionVerbAllowance("KnowledgeGraphExtractionController", "Knowledge"),
                actionVerbAllowance("KnowledgeGraphRefinementController", "Knowledge"),
                actionVerbAllowance("KnowledgeGraphWorkbenchController", "Knowledge"),
                actionVerbAllowance("KnowledgeLineageController", "Knowledge"),
                actionVerbAllowance("KnowledgeQualityReportController", "Knowledge"),
                actionVerbAllowance("KnowledgeTaxonomyController", "Knowledge"));
    }

    private static ArchitectureRuleAllowance actionVerbAllowance(String controller, String domain) {
        return ArchitectureRuleAllowance.of(
                "CONTROLLER_ACTION_VERB:*" + controller + ".java*",
                domain + " controller retains legacy action names or paths outside the shared verb whitelist.",
                "Rename the controller method and action path with a shared verb, update callers, then remove this allowance.");
    }
}
