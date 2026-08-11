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
        ApiAnnotationArchitectureRuleSupport.assertAdminControllersDeclareRequiredClassAnnotations(
                BUSINESS_SOURCE_ROOT);
        ApiAnnotationArchitectureRuleSupport.assertAdminControllerMethodsDeclareRequiredAnnotations(
                BUSINESS_SOURCE_ROOT);
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
        ApiAnnotationArchitectureRuleSupport.assertPostMappingMethodsUseRequestResponseShape(BUSINESS_SOURCE_ROOT);
        ApiAnnotationArchitectureRuleSupport.assertPostMappingMethodsDoNotUsePathOrQueryParameters(
                BUSINESS_SOURCE_ROOT);
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
        List<ArchitectureRuleAllowance> allowances = new ArrayList<ArchitectureRuleAllowance>();
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/DiscoveryQaPortalStreamController.java",
                "method=chatCompletionsStream",
                "method=chatCompletionsStream path=chat/completions/stream");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/DiscoveryQaPortalController.java",
                "method=openSession",
                "method=openSession path=session/open",
                "method=exportSession",
                "method=exportSession path=session/export",
                "method=chatCompletions",
                "method=chatCompletions path=chat/completions");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaConversationStreamController.java",
                "method=chatCompletionsStream",
                "method=chatCompletionsStream path=chat/completions/stream");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaAdminController.java",
                "method=getKnowledgeHealth path=knowledge/health",
                "method=syncKnowledge",
                "method=syncKnowledge path=knowledge/sync",
                "method=exportSession",
                "method=exportSession path=session/export");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaConversationController.java",
                "method=openSession",
                "method=openSession path=session/open",
                "method=exportSession",
                "method=exportSession path=session/export",
                "method=chatCompletions",
                "method=chatCompletions path=chat/completions");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchStatisticsController.java",
                "method=getStatisticsSummary path=summary");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/StorageObjectController.java",
                "method=initiate",
                "method=initiate path=multipart/initiate",
                "method=uploadPart path=multipart/uploadPart");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/workbench/controller/KnowledgeGraphWorkbenchController.java",
                "method=listManuscriptTree path=manuscript-tree");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/AiInvocationController.java",
                "method=summarizeInvocationLogs",
                "method=summarizeInvocationLogs path=invocation-log/summary",
                "method=markCandidateApplied",
                "method=markCandidateApplied path=candidate/mark-applied",
                "method=recordBatchSuccess",
                "method=recordBatchSuccess path=batch/record-success",
                "method=recordBatchFailure",
                "method=recordBatchFailure path=batch/record-failure",
                "method=canDispatchBatch",
                "method=canDispatchBatch path=batch/can-dispatch");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/config/prompt/controller/PromptController.java",
                "method=getTemplateByCapability path=template/get-by-capability",
                "method=saveTemplate",
                "method=saveTemplate path=template/save",
                "method=getCurrentVersion path=version/current",
                "method=compareVersions",
                "method=compareVersions path=version/compare",
                "method=rollbackVersion",
                "method=rollbackVersion path=version/rollback",
                "method=validateVariables",
                "method=validateVariables path=variable/validate",
                "method=buildOptimizationSuggestion",
                "method=buildOptimizationSuggestion path=optimization/suggest");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/platform/controller/PlatformAiController.java",
                "method=buildPromptSuggestion",
                "method=buildPromptSuggestion path=prompt-suggestion",
                "method=summarizeVersion",
                "method=summarizeVersion path=version-summary");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementTaskController.java",
                "method=streamTask");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementController.java",
                "method=translate",
                "method=translate path=translate",
                "method=summarize",
                "method=summarize path=summary",
                "method=generateTags",
                "method=generateTags path=tags",
                "method=generateQa",
                "method=generateQa path=qa",
                "method=analyzeImage",
                "method=analyzeImage path=image-analysis",
                "method=fuseVisualContext",
                "method=fuseVisualContext path=fusion",
                "method=describeVisual",
                "method=describeVisual path=visual",
                "method=generateImage",
                "method=generateImage path=image-gen",
                "method=splitEntry",
                "method=splitEntry path=split");
        return allowances;
    }

    private static void addLegacyActionVerbAllowances(
            List<ArchitectureRuleAllowance> allowances, String sourcePath, String... violations) {
        for (String violation : violations) {
            allowances.add(
                    ArchitectureRuleAllowance.of(
                            "CONTROLLER_ACTION_VERB:" + sourcePath + " " + violation,
                            "Controller retains a legacy action name or path outside the shared verb whitelist.",
                            "Rename the controller method and action path with a shared verb, update callers, then remove this allowance."));
        }
    }
}
