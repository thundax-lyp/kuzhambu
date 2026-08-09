package com.thundax.kuzhambu.knowledge.interfaces;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.ApiAnnotationArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ApiSurfaceArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.BoundaryAssemblerNullnessAllowances;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.SpringBeanArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class KnowledgeInterfaceArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.knowledge";

    @Test
    void interfaceSpringBeansShouldDeclareSingleConstructor() {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".interfaces");

        SpringBeanArchitectureRuleSupport.assertDirectSpringBeansHaveSingleConstructor(classes);
    }

    @Test
    void interfaceApiAnnotationsShouldKeepContractShape() throws Exception {
        ApiAnnotationArchitectureRuleSupport.assertPostMappingMethodsDoNotUsePathOrQueryParameters(
                Path.of("src/main/java"));
        ApiSurfaceArchitectureRuleSupport.assertApiModelsDoNotExposePriority(Path.of("src/main/java"));
        ApiSurfaceArchitectureRuleSupport.assertSortRequestsUseOrderedIdsOnly(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertBoundaryAssemblerPublicMethodsUseNonNullContracts(
                Collections.singletonList(Path.of("src/main/java")),
                BoundaryAssemblerNullnessAllowances.legacyClasses(
                        "com.thundax.kuzhambu.knowledge.interfaces.admin.graph.assembler.KnowledgeGraphExtractionInterfaceAssembler",
                        "com.thundax.kuzhambu.knowledge.interfaces.admin.lineage.assembler.KnowledgeLineageInterfaceAssembler",
                        "com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.assembler.KnowledgeGraphRefinementInterfaceAssembler",
                        "com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.assembler.KnowledgeQualityReportInterfaceAssembler",
                        "com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.assembler.KnowledgeTaxonomyInterfaceAssembler",
                        "com.thundax.kuzhambu.knowledge.interfaces.admin.workbench.assembler.KnowledgeGraphWorkbenchInterfaceAssembler",
                        "com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.assembler.KnowledgePortalAtlasInterfaceAssembler",
                        "com.thundax.kuzhambu.knowledge.interfaces.portal.home.assembler.KnowledgePortalHomeInterfaceAssembler",
                        "com.thundax.kuzhambu.knowledge.interfaces.portal.lineage.assembler.KnowledgePortalLineageInterfaceAssembler",
                        "com.thundax.kuzhambu.knowledge.interfaces.portal.quality.assembler.KnowledgePortalQualityInterfaceAssembler"));
    }
}
