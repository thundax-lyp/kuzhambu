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
                "kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/OperationsHealthAdminController.java",
                "method=summary",
                "method=summary path=summary",
                "method=trend",
                "method=trend path=trend");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/OperationsHealthAlertAdminController.java",
                "method=ack",
                "method=ack path=ack");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/OperationsDashboardAdminController.java",
                "method=overview",
                "method=overview path=overview");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/controller/OperationsCleanupAdminController.java",
                "method=execute",
                "method=execute path=execute",
                "method=detail",
                "method=detail path=detail");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/task/controller/OperationsTaskAdminController.java",
                "method=detail",
                "method=detail path=detail");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/OperationsReportAdminController.java",
                "method=generate",
                "method=generate path=generate",
                "method=detail",
                "method=detail path=detail");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/backup/controller/OperationsBackupAdminController.java",
                "method=execute",
                "method=execute path=execute",
                "method=detail",
                "method=detail path=detail");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/OperationsRestoreAdminController.java",
                "method=execute",
                "method=execute path=execute",
                "method=detail",
                "method=detail path=detail");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/StorageObjectController.java",
                "method=initiate",
                "method=initiate path=multipart/initiate",
                "method=uploadPart path=multipart/uploadPart");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/controller/KnowledgeGraphExtractionController.java",
                "method=cancelBatchTask path=task/cancel-batch");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeGraphRefinementController.java",
                "method=openTask",
                "method=openTask path=task/open",
                "method=getTaskDetail path=task/detail",
                "method=qualitySummary",
                "method=qualitySummary path=quality/summary",
                "method=upsertAnnotation");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeQualityReportController.java",
                "method=generate",
                "method=generate path=generate",
                "method=detail",
                "method=detail path=detail",
                "method=reextractLowQualityCategory",
                "method=reextractLowQualityCategory path=reextract-low-quality-category");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/DepartmentController.java",
                "method=tree",
                "method=tree path=tree");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/MenuController.java",
                "method=updateVisibility path=display",
                "method=tree",
                "method=tree path=tree");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/RoleController.java",
                "method=options",
                "method=options path=options",
                "method=updateStatus path=enable",
                "method=menuTree",
                "method=menuTree path=menu/tree",
                "method=userTree",
                "method=userTree path=user/tree",
                "method=userList",
                "method=assignUser",
                "method=assignUser path=user/assign");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/CurrentUserController.java",
                "method=info",
                "method=info path=info",
                "method=menus",
                "method=menus path=menus",
                "method=perms",
                "method=perms path=perms");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/UserController.java",
                "method=options",
                "method=options path=options",
                "method=avatar",
                "method=avatar path=avatar",
                "method=updateStatus path=enable",
                "method=check",
                "method=check path=check",
                "method=departmentTree",
                "method=departmentTree path=department/tree",
                "method=roleList",
                "method=avatarImage");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/workbench/controller/KnowledgeGraphWorkbenchController.java",
                "method=listManuscriptTree path=manuscript-tree");
        addLegacyActionVerbAllowances(
                allowances,
                "kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/KnowledgeTaxonomyController.java",
                "method=changeCategoryStatus path=category/status",
                "method=getTagDetail path=tag/detail",
                "method=changeTagStatus path=tag/status",
                "method=batchReviewTags",
                "method=batchReviewTags path=tag/review/batch",
                "method=previewTagBatchMergeImpact path=tag/merge/batch-preview",
                "method=applyTagBatchMerge path=tag/merge/batch-apply",
                "method=batchDeprecateTags",
                "method=batchDeprecateTags path=tag/deprecate/batch",
                "method=getTagGovernanceMetrics path=tag/metrics");
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
