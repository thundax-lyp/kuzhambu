package com.thundax.kuzhambu.classics.domain;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.AnnotationBoundaryArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ModuleAndDependencyArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.SpringBeanArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ClassicsDomainArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.classics";

    @Test
    void domainLayerShouldKeepArchitectureBoundary() throws Exception {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".domain");

        ModuleAndDependencyArchitectureRuleSupport.assertCrossDomainDependencyBoundary(classes, "classics");
        AnnotationBoundaryArchitectureRuleSupport.assertDomainSpringAndPersistenceFree(classes, BASE_PACKAGE);
        SpringBeanArchitectureRuleSupport.assertDirectSpringBeansHaveSingleConstructor(classes);
        NamingArchitectureRuleSupport.assertCodecPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertValueObjectPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertValueObjectIdSourcesDeclareNoStaticMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertBaseIdTypes(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertEntityPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertEntitySourcesDeclareOnlyRequiredAnnotations(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertDomainEnumPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertDomainServiceSourcesUseRepositoryBoundary(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertRepositoryPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertRepositoryInterfaceMethodNames(
                classes,
                NamingArchitectureRuleSupport.legacyRepositoryInterfaceMethodNameAllowances(
                        "com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository.advanceMilestone",
                        "com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository.bindFastGptCollection",
                        "com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository.claimEsCleanup",
                        "com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository.claimExecution",
                        "com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository.claimFastGptCleanup",
                        "com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository.completeEsCleanup",
                        "com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository.completeFastGptCleanup",
                        "com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository.failEsCleanup",
                        "com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository.failFastGptCleanup",
                        "com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository.lockByContent",
                        "com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository.markContentDeleted",
                        "com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository.markSucceeded",
                        "com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository.markTerminalFailure",
                        "com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository.markThreadStarted",
                        "com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository.releaseEsCleanupClaim",
                        "com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository.releaseExecutionClaim",
                        "com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository.releaseFastGptCleanupClaim",
                        "com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository.releaseForRetry"));
    }
}
