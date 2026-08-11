package com.thundax.kuzhambu.common.test.architecture;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class RepositoryApiHardRulesArchitectureTest {

    private static final Path BUSINESS_SOURCE_ROOT = Path.of("../../biz");

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
        ApiAnnotationArchitectureRuleSupport.assertControllerActionsAvoidAmbiguousVerbs(BUSINESS_SOURCE_ROOT);
        ApiAnnotationArchitectureRuleSupport.assertPostMappingMethodsUseRequestResponseShape(BUSINESS_SOURCE_ROOT);
        ApiAnnotationArchitectureRuleSupport.assertPostMappingMethodsDoNotUsePathOrQueryParameters(
                BUSINESS_SOURCE_ROOT);
        ApiAnnotationArchitectureRuleSupport.assertRequestBodyRequestParametersDeclareValid(BUSINESS_SOURCE_ROOT);
        ApiAnnotationArchitectureRuleSupport.assertControllersDoNotCreateResponses(BUSINESS_SOURCE_ROOT);
    }
}
