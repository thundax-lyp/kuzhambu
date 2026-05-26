package com.thundax.kuzhambu.interfaces.portal.architecture;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.ApiAnnotationArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class ApiAnnotationArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.interfaces.portal";

    @Test
    public void shouldDeclareRestControllerApiAnnotations() throws IOException {
        Path sourceRoot = mainSourceRoot();

        ApiAnnotationArchitectureRuleSupport.assertRestControllersDeclareRequestMapping(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertRestControllerRequestMappingsUseApiResourcePath(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertRestControllersDeclareApi(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertApiTagsDoNotUseNumericPrefix(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertMappedMethodsDeclareOperation(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertMappedMethodsDeclareSingleHttpMapping(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertMappedMethodsUsePostOrGetMapping(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertJsonRequestMethodsUsePostMapping(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertGetMappingMethodsReturnVoid(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertOperationDeclaresAccessAnnotation(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertRequestBodyRequestParametersDeclareValid(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertControllersDoNotCreateResponses(sourceRoot);
    }

    @Test
    public void shouldDeclareRequestAndResponseModelAnnotations() {
        JavaClasses classes = importPackages(BASE_PACKAGE);

        ApiAnnotationArchitectureRuleSupport.requestClassAnnotationsRequired(BASE_PACKAGE)
                .check(classes);
        ApiAnnotationArchitectureRuleSupport.responseClassAnnotationsRequired(BASE_PACKAGE)
                .check(classes);
    }

    private Path mainSourceRoot() {
        return Paths.get("src/main/java");
    }
}
