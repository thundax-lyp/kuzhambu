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
}
