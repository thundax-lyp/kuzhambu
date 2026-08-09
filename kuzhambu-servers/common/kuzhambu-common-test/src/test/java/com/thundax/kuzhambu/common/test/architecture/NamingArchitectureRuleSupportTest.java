package com.thundax.kuzhambu.common.test.architecture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NamingArchitectureRuleSupportTest {

    @TempDir
    private Path tempDir;

    @Test
    void valueObjectIdGateShouldRejectPackagePrivateStaticMethods() throws Exception {
        Path source = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/domain/core/model/valueobject");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleId.java"),
                """
                package com.thundax.kuzhambu.sample.domain.core.model.valueobject;

                final class SampleId {
                    static SampleId of(String value) {
                        return new SampleId();
                    }
                }
                """);

        assertThrows(
                AssertionError.class,
                () -> NamingArchitectureRuleSupport.assertValueObjectIdSourcesDeclareNoStaticMethods(
                        tempDir.resolve("src/main/java")));
    }

    @Test
    void applicationCommandQueryGateShouldRejectExplicitMethods() throws Exception {
        Path source = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/application/core/command");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleCreateCommand.java"),
                """
                package com.thundax.kuzhambu.sample.application.core.command;

                public class SampleCreateCommand {
                    private String name;

                    public String nameValue() {
                        return name;
                    }
                }
                """);

        assertThrows(
                AssertionError.class,
                () -> NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesDeclareNoMethods(
                        tempDir.resolve("src/main/java")));
    }

    @Test
    void applicationCommandQueryGateShouldRejectPackagePrivateMethods() throws Exception {
        Path source = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/application/core/command");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleCreateCommand.java"),
                """
                package com.thundax.kuzhambu.sample.application.core.command;

                public class SampleCreateCommand {
                    private String name;

                    String nameValue() {
                        return name;
                    }
                }
                """);

        assertThrows(
                AssertionError.class,
                () -> NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesDeclareNoMethods(
                        tempDir.resolve("src/main/java")));
    }

    @Test
    void applicationCommandQueryGateShouldAllowExplicitConstructors() throws Exception {
        Path source = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/application/core/command");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleCreateCommand.java"),
                """
                package com.thundax.kuzhambu.sample.application.core.command;

                public class SampleCreateCommand {
                    private String name;

                    public SampleCreateCommand(String name) {
                        this.name = name;
                    }
                }
                """);

        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesDeclareNoMethods(
                tempDir.resolve("src/main/java"));
    }

    @Test
    void applicationCommandQueryGateShouldIgnoreMethodBodyControlFlow() throws Exception {
        Path source = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/application/core/command");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleCreateCommand.java"),
                """
                package com.thundax.kuzhambu.sample.application.core.command;

                import lombok.Getter;
                import lombok.NoArgsConstructor;
                import lombok.Setter;

                @Getter
                @Setter
                @NoArgsConstructor
                public class SampleCreateCommand {
                    private String name;

                    public SampleCreateCommand(String name) {
                        if (name == null) {
                            return;
                        }
                        this.name = name;
                    }
                }
                """);

        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesDeclareNoMethods(
                tempDir.resolve("src/main/java"));
    }

    @Test
    void applicationContractPackageGateShouldRejectContractsOutsideDedicatedPackages() throws Exception {
        Path source = applicationSourceRoot().resolve("com/thundax/kuzhambu/sample/application/core");
        Files.createDirectories(source);
        Files.writeString(source.resolve("SampleCreateCommand.java"), "public class SampleCreateCommand {}");
        Files.writeString(source.resolve("SampleSearchQuery.java"), "public class SampleSearchQuery {}");
        Files.writeString(source.resolve("SampleDetailResult.java"), "public class SampleDetailResult {}");

        assertThrows(
                AssertionError.class,
                () -> NamingArchitectureRuleSupport.assertApplicationContractSourcesUnderDedicatedPackages(
                        applicationSourceRoot()));
    }

    @Test
    void applicationContractPackageGateShouldRejectServiceNestedContractPackages() throws Exception {
        Path source = applicationSourceRoot().resolve("com/thundax/kuzhambu/sample/application/core/service/command");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleCreateCommand.java"),
                "package com.thundax.kuzhambu.sample.application.core.service.command;\n"
                        + "public class SampleCreateCommand {}");

        assertThrows(
                AssertionError.class,
                () -> NamingArchitectureRuleSupport.assertApplicationContractSourcesUnderDedicatedPackages(
                        applicationSourceRoot()));
    }

    @Test
    void applicationContractPackageGateShouldRejectFacadeNestedContractPackages() throws Exception {
        Path source = applicationSourceRoot().resolve("com/thundax/kuzhambu/sample/application/facade/command");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleCreateCommand.java"),
                "package com.thundax.kuzhambu.sample.application.facade.command;\n"
                        + "public class SampleCreateCommand {}");

        assertThrows(
                AssertionError.class,
                () -> NamingArchitectureRuleSupport.assertApplicationContractSourcesUnderDedicatedPackages(
                        applicationSourceRoot()));
    }

    @Test
    void applicationContractPackageGateShouldRejectEstablishedStructuralPackages() throws Exception {
        for (String structuralPackage : List.of(
                "configure",
                "configuration",
                "dto",
                "exception",
                "executor",
                "factory",
                "gateway",
                "handler",
                "helper",
                "misc",
                "model",
                "resolver",
                "runtime",
                "util",
                "utils")) {
            Path source = applicationSourceRoot()
                    .resolve("com/thundax/kuzhambu/sample/application/core")
                    .resolve(structuralPackage)
                    .resolve("command");
            Files.createDirectories(source);
            Files.writeString(
                    source.resolve("SampleCreateCommand.java"),
                    "package com.thundax.kuzhambu.sample.application.core."
                            + structuralPackage
                            + ".command;\n"
                            + "public class SampleCreateCommand {}");
        }

        assertThrows(
                AssertionError.class,
                () -> NamingArchitectureRuleSupport.assertApplicationContractSourcesUnderDedicatedPackages(
                        applicationSourceRoot()));
    }

    @Test
    void applicationContractPackageGateShouldRejectContractsOutsideApplicationPackage() throws Exception {
        Path source = applicationSourceRoot().resolve("com/thundax/kuzhambu/sample/misc");
        Files.createDirectories(source);
        Files.writeString(source.resolve("SampleCreateCommand.java"), "public class SampleCreateCommand {}");

        assertThrows(
                AssertionError.class,
                () -> NamingArchitectureRuleSupport.assertApplicationContractSourcesUnderDedicatedPackages(
                        applicationSourceRoot()));
    }

    @Test
    void applicationContractPackageGateShouldAllowDedicatedPackages() throws Exception {
        Path application = applicationSourceRoot().resolve("com/thundax/kuzhambu/sample/application/core");
        Files.createDirectories(application.resolve("command"));
        Files.createDirectories(application.resolve("query"));
        Files.createDirectories(application.resolve("result"));
        Files.writeString(
                application.resolve("command/SampleCreateCommand.java"),
                "package com.thundax.kuzhambu.sample.application.core.command;\n"
                        + "public class SampleCreateCommand {}");
        Files.writeString(
                application.resolve("query/SampleSearchQuery.java"),
                "package com.thundax.kuzhambu.sample.application.core.query;\n" + "public class SampleSearchQuery {}");
        Files.writeString(
                application.resolve("result/SampleDetailResult.java"),
                "package com.thundax.kuzhambu.sample.application.core.result;\n"
                        + "public class SampleDetailResult {}");

        assertDoesNotThrow(() -> NamingArchitectureRuleSupport.assertApplicationContractSourcesUnderDedicatedPackages(
                applicationSourceRoot()));
    }

    @Test
    void applicationContractPackageGateShouldAllowNestedSubdomainsAndApplicationLevelResults() throws Exception {
        Path application = applicationSourceRoot().resolve("com/thundax/kuzhambu/sample/application");
        Files.createDirectories(application.resolve("config/prompt/command"));
        Files.createDirectories(application.resolve("result"));
        Files.writeString(
                application.resolve("config/prompt/command/PromptSaveCommand.java"),
                "package com.thundax.kuzhambu.sample.application.config.prompt.command;\n"
                        + "public class PromptSaveCommand {}");
        Files.writeString(
                application.resolve("result/SharedOperationResult.java"),
                "package com.thundax.kuzhambu.sample.application.result;\n" + "public class SharedOperationResult {}");

        assertDoesNotThrow(() -> NamingArchitectureRuleSupport.assertApplicationContractSourcesUnderDedicatedPackages(
                applicationSourceRoot()));
    }

    @Test
    void implContractGateShouldRejectImplWithoutNamedInterface() throws Exception {
        Path source = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/application/core/service/impl");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleApplicationServiceImpl.java"),
                """
                package com.thundax.kuzhambu.sample.application.core.service.impl;

                public class SampleApplicationServiceImpl {
                }
                """);

        assertThrows(
                AssertionError.class,
                () -> ImplContractArchitectureRuleSupport.assertImplClassesImplementNamedInterface(
                        compileAndImport(tempDir.resolve("src/main/java")), Collections.emptySet()));
    }

    @Test
    void implContractGateShouldAllowImplWithNamedInterface() throws Exception {
        Path service = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/application/core/service");
        Path impl = service.resolve("impl");
        Files.createDirectories(impl);
        Files.writeString(
                service.resolve("SampleApplicationService.java"),
                """
                package com.thundax.kuzhambu.sample.application.core.service;

                public interface SampleApplicationService {
                }
                """);
        Files.writeString(
                impl.resolve("SampleApplicationServiceImpl.java"),
                """
                package com.thundax.kuzhambu.sample.application.core.service.impl;

                import com.thundax.kuzhambu.sample.application.core.service.SampleApplicationService;

                public class SampleApplicationServiceImpl implements SampleApplicationService {
                }
                """);

        assertDoesNotThrow(() -> ImplContractArchitectureRuleSupport.assertImplClassesImplementNamedInterface(
                compileAndImport(tempDir.resolve("src/main/java")), Collections.emptySet()));
    }

    @Test
    void implContractGateShouldRejectProductionDependencyOnImplType() throws Exception {
        Path service = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/application/core/service");
        Path impl = service.resolve("impl");
        Files.createDirectories(impl);
        Files.writeString(
                service.resolve("SampleApplicationService.java"),
                """
                package com.thundax.kuzhambu.sample.application.core.service;

                public interface SampleApplicationService {
                }
                """);
        Files.writeString(
                impl.resolve("SampleApplicationServiceImpl.java"),
                """
                package com.thundax.kuzhambu.sample.application.core.service.impl;

                import com.thundax.kuzhambu.sample.application.core.service.SampleApplicationService;

                public class SampleApplicationServiceImpl implements SampleApplicationService {
                }
                """);
        Files.writeString(
                service.resolve("SampleConsumer.java"),
                """
                package com.thundax.kuzhambu.sample.application.core.service;

                import com.thundax.kuzhambu.sample.application.core.service.impl.SampleApplicationServiceImpl;

                public class SampleConsumer {

                    private final SampleApplicationServiceImpl service;

                    public SampleConsumer(SampleApplicationServiceImpl service) {
                        this.service = service;
                    }
                }
                """);

        assertThrows(
                AssertionError.class,
                () -> ImplContractArchitectureRuleSupport.assertProductionCodeDoesNotDependOnImplTypes(
                        compileAndImport(tempDir.resolve("src/main/java")), Collections.emptySet()));
    }

    @Test
    void implContractGateShouldAllowNestedContractOnImplType() throws Exception {
        Path service = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/application/core/service");
        Path impl = service.resolve("impl");
        Files.createDirectories(impl);
        Files.writeString(
                service.resolve("SampleApplicationService.java"),
                """
                package com.thundax.kuzhambu.sample.application.core.service;

                public interface SampleApplicationService {
                }
                """);
        Files.writeString(
                impl.resolve("SampleApplicationServiceImpl.java"),
                """
                package com.thundax.kuzhambu.sample.application.core.service.impl;

                import com.thundax.kuzhambu.sample.application.core.service.SampleApplicationService;

                public class SampleApplicationServiceImpl implements SampleApplicationService {
                    public interface CacheChangedListener {
                    }
                }
                """);
        Files.writeString(
                service.resolve("SampleConsumer.java"),
                """
                package com.thundax.kuzhambu.sample.application.core.service;

                import com.thundax.kuzhambu.sample.application.core.service.impl.SampleApplicationServiceImpl;

                public class SampleConsumer implements SampleApplicationServiceImpl.CacheChangedListener {
                }
                """);

        ImplContractArchitectureRuleSupport.assertProductionCodeDoesNotDependOnImplTypes(
                compileAndImport(tempDir.resolve("src/main/java")), Collections.emptySet());
    }

    @Test
    void implContractGateShouldRejectNestedClassDependingOnImplType() throws Exception {
        Path service = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/application/core/service");
        Path impl = service.resolve("impl");
        Files.createDirectories(impl);
        Files.writeString(
                service.resolve("SampleApplicationService.java"),
                """
                package com.thundax.kuzhambu.sample.application.core.service;

                public interface SampleApplicationService {
                }
                """);
        Files.writeString(
                impl.resolve("SampleApplicationServiceImpl.java"),
                """
                package com.thundax.kuzhambu.sample.application.core.service.impl;

                import com.thundax.kuzhambu.sample.application.core.service.SampleApplicationService;

                public class SampleApplicationServiceImpl implements SampleApplicationService {
                }
                """);
        Files.writeString(
                service.resolve("SampleConsumer.java"),
                """
                package com.thundax.kuzhambu.sample.application.core.service;

                import com.thundax.kuzhambu.sample.application.core.service.impl.SampleApplicationServiceImpl;

                public class SampleConsumer {

                    static class NestedConsumer {

                        private final SampleApplicationServiceImpl service;

                        NestedConsumer(SampleApplicationServiceImpl service) {
                            this.service = service;
                        }
                    }
                }
                """);

        assertThrows(
                AssertionError.class,
                () -> ImplContractArchitectureRuleSupport.assertProductionCodeDoesNotDependOnImplTypes(
                        compileAndImport(tempDir.resolve("src/main/java")), Collections.emptySet()));
    }

    @Test
    void implContractGateShouldAllowNestedClassDependingOnOwnEnclosingImplType() throws Exception {
        Path service = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/application/core/service");
        Path impl = service.resolve("impl");
        Files.createDirectories(impl);
        Files.writeString(
                service.resolve("SampleApplicationService.java"),
                """
                package com.thundax.kuzhambu.sample.application.core.service;

                public interface SampleApplicationService {
                }
                """);
        Files.writeString(
                impl.resolve("SampleApplicationServiceImpl.java"),
                """
                package com.thundax.kuzhambu.sample.application.core.service.impl;

                import com.thundax.kuzhambu.sample.application.core.service.SampleApplicationService;

                public class SampleApplicationServiceImpl implements SampleApplicationService {

                    class NestedWorker {

                        private final SampleApplicationServiceImpl service;

                        NestedWorker() {
                            this.service = SampleApplicationServiceImpl.this;
                        }
                    }
                }
                """);

        ImplContractArchitectureRuleSupport.assertProductionCodeDoesNotDependOnImplTypes(
                compileAndImport(tempDir.resolve("src/main/java")), Collections.emptySet());
    }

    @Test
    void applicationContractPackageGateShouldRejectMismatchedDeclaredPackage() throws Exception {
        Path source = applicationSourceRoot().resolve("com/thundax/kuzhambu/sample/application/core/command");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleCreateCommand.java"),
                "package com.thundax.kuzhambu.sample.application.core;\n" + "public class SampleCreateCommand {}");

        assertThrows(
                AssertionError.class,
                () -> NamingArchitectureRuleSupport.assertApplicationContractSourcesUnderDedicatedPackages(
                        applicationSourceRoot()));
    }

    @Test
    void applicationContractPackageGateShouldIgnoreContractsOutsideApplicationModule() throws Exception {
        Path interfaceSourceRoot = tempDir.resolve("kuzhambu-sample-interface/src/main/java");
        Path source = interfaceSourceRoot.resolve("com/thundax/kuzhambu/sample/application/core");
        Files.createDirectories(source);
        Files.writeString(source.resolve("AdminAuthCommand.java"), "public class AdminAuthCommand {}");

        assertDoesNotThrow(() -> NamingArchitectureRuleSupport.assertApplicationContractSourcesUnderDedicatedPackages(
                interfaceSourceRoot));
    }

    @Test
    void commandQueryConstructionGateShouldRejectControllerConstruction() throws Exception {
        Path source = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/interfaces/admin/controller");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleController.java"),
                """
                package com.thundax.kuzhambu.sample.interfaces.admin.controller;

                public class SampleController {
                    Object create() {
                        return new SampleCreateCommand();
                    }
                }
                """);

        assertThrows(
                AssertionError.class,
                () ->
                        NamingArchitectureRuleSupport
                                .assertApplicationCommandQueryConstructionInAssemblersOrApplicationServices(
                                        List.of(tempDir.resolve("src/main/java")), Collections.emptyList()));
    }

    @Test
    void commandQueryConstructionGateShouldAllowAssemblersAndApplicationServices() throws Exception {
        Path assembler = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/interfaces/admin/assembler");
        Path service = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/application/core/service/impl");
        Files.createDirectories(assembler);
        Files.createDirectories(service);
        Files.writeString(
                assembler.resolve("SampleInterfaceAssembler.java"),
                """
                package com.thundax.kuzhambu.sample.interfaces.admin.assembler;

                public final class SampleInterfaceAssembler {
                    Object create() {
                        return new SampleCreateCommand();
                    }
                }
                """);
        Files.writeString(
                service.resolve("SampleApplicationServiceImpl.java"),
                """
                package com.thundax.kuzhambu.sample.application.core.service.impl;

                public final class SampleApplicationServiceImpl {
                    Object create() {
                        return new SampleCreateCommand();
                    }
                }
                """);

        NamingArchitectureRuleSupport.assertApplicationCommandQueryConstructionInAssemblersOrApplicationServices(
                List.of(tempDir.resolve("src/main/java")), Collections.emptyList());
    }

    @Test
    void commandQueryConstructionGateShouldHonorAllowlist() throws Exception {
        Path source = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/interfaces/admin/controller");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleController.java"),
                """
                package com.thundax.kuzhambu.sample.interfaces.admin.controller;

                public class SampleController {
                    Object create() {
                        return new SampleCreateCommand();
                    }
                }
                """);

        NamingArchitectureRuleSupport.assertApplicationCommandQueryConstructionInAssemblersOrApplicationServices(
                List.of(tempDir.resolve("src/main/java")),
                List.of(
                        ArchitectureRuleAllowance.of(
                                NamingArchitectureRuleSupport.commandQueryConstructionKey(
                                        "com.thundax.kuzhambu.sample.interfaces.admin.controller.SampleController",
                                        "SampleCreateCommand",
                                        1),
                                "Controller still constructs an application command directly.",
                                "Move request-to-command conversion into SampleInterfaceAssembler, then remove this allowance.")));
    }

    @Test
    void commandQueryAssemblerNullReturnGateShouldRejectCommandReturnNull() throws Exception {
        Path source = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/interfaces/admin/assembler");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleInterfaceAssembler.java"),
                """
                package com.thundax.kuzhambu.sample.interfaces.admin.assembler;

                public class SampleInterfaceAssembler {
                    SampleCreateCommand toCommand(Object request) {
                        if (request == null) {
                            return null;
                        }
                        return new SampleCreateCommand();
                    }
                }
                """);

        assertThrows(
                AssertionError.class,
                () -> NamingArchitectureRuleSupport.assertAssemblersDoNotReturnNullApplicationCommandOrQuery(
                        List.of(tempDir.resolve("src/main/java")), Collections.emptyList()));
    }

    @Test
    void commandQueryAssemblerNullReturnGateShouldIgnoreNonCommandReturns() throws Exception {
        Path source = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/application/facade/assembler");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleFacadeAssembler.java"),
                """
                package com.thundax.kuzhambu.sample.application.facade.assembler;

                public class SampleFacadeAssembler {
                    SampleResponse toResponse(Object result) {
                        if (result == null) {
                            return null;
                        }
                        return new SampleResponse();
                    }
                }
                """);

        NamingArchitectureRuleSupport.assertAssemblersDoNotReturnNullApplicationCommandOrQuery(
                List.of(tempDir.resolve("src/main/java")), Collections.emptyList());
    }

    @Test
    void commandQueryAssemblerNullReturnGateShouldHonorAllowlist() throws Exception {
        Path source = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/application/facade/assembler");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleFacadeAssembler.java"),
                """
                package com.thundax.kuzhambu.sample.application.facade.assembler;

                public class SampleFacadeAssembler {
                    SampleCreateCommand toCommand(Object request) {
                        if (request == null) {
                            return null;
                        }
                        return new SampleCreateCommand();
                    }
                }
                """);

        NamingArchitectureRuleSupport.assertAssemblersDoNotReturnNullApplicationCommandOrQuery(
                List.of(tempDir.resolve("src/main/java")),
                List.of(
                        ArchitectureRuleAllowance.of(
                                NamingArchitectureRuleSupport.commandQueryAssemblerNullReturnKey(
                                        "com.thundax.kuzhambu.sample.application.facade.assembler.SampleFacadeAssembler",
                                        "toCommand",
                                        "SampleCreateCommand",
                                        1),
                                "Facade assembler can still return null for a command contract.",
                                "Move null handling to caller validation or return a concrete command, then remove this allowance.")));
    }

    @Test
    void domainServiceGateShouldRejectConcreteDomainServiceClassInInterfacePackage() throws Exception {
        Path source = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/domain/service");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleDomainService.java"),
                """
                package com.thundax.kuzhambu.sample.domain.service;

                import com.thundax.kuzhambu.sample.domain.core.repository.SampleRepository;

                public class SampleDomainService {
                    private final SampleRepository repository;

                    public SampleDomainService(SampleRepository repository) {
                        this.repository = repository;
                    }
                }
                """);

        assertThrows(
                AssertionError.class,
                () -> NamingArchitectureRuleSupport.assertDomainServiceSourcesUseRepositoryBoundary(
                        tempDir.resolve("src/main/java")));
    }

    @Test
    void domainServiceGateShouldRejectDomainServiceImplDeclaredAsInterface() throws Exception {
        Path source = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/domain/service/impl");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleDomainServiceImpl.java"),
                """
                package com.thundax.kuzhambu.sample.domain.service.impl;

                public interface SampleDomainServiceImpl {
                }
                """);

        assertThrows(
                AssertionError.class,
                () -> NamingArchitectureRuleSupport.assertDomainServiceSourcesUseRepositoryBoundary(
                        tempDir.resolve("src/main/java")));
    }

    @Test
    void domainServiceGateShouldRejectImplementationWithoutCorrespondingInterface() throws Exception {
        Path source = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/domain/service/impl");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleDomainServiceImpl.java"),
                """
                package com.thundax.kuzhambu.sample.domain.service.impl;

                import com.thundax.kuzhambu.sample.domain.core.repository.SampleRepository;

                public class SampleDomainServiceImpl {
                    private final SampleRepository repository;

                    public SampleDomainServiceImpl(SampleRepository repository) {
                        this.repository = repository;
                    }
                }
                """);

        assertThrows(
                AssertionError.class,
                () -> NamingArchitectureRuleSupport.assertDomainServiceSourcesUseRepositoryBoundary(
                        tempDir.resolve("src/main/java")));
    }

    @Test
    void domainServiceGateShouldRejectImplementationWithExpectedInterfaceOnlyAsGenericArgument() throws Exception {
        Path source = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/domain/service/impl");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleDomainServiceImpl.java"),
                """
                package com.thundax.kuzhambu.sample.domain.service.impl;

                import com.thundax.kuzhambu.sample.domain.core.repository.SampleRepository;
                import com.thundax.kuzhambu.sample.domain.service.SampleDomainService;
                import java.util.function.Supplier;

                public class SampleDomainServiceImpl implements Supplier<SampleDomainService> {
                    private final SampleRepository repository;

                    public SampleDomainServiceImpl(SampleRepository repository) {
                        this.repository = repository;
                    }

                    @Override
                    public SampleDomainService get() {
                        return null;
                    }
                }
                """);

        assertThrows(
                AssertionError.class,
                () -> NamingArchitectureRuleSupport.assertDomainServiceSourcesUseRepositoryBoundary(
                        tempDir.resolve("src/main/java")));
    }

    @Test
    void domainServiceGateShouldRejectAbstractImplementation() throws Exception {
        Path source = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/domain/service/impl");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleDomainServiceImpl.java"),
                """
                package com.thundax.kuzhambu.sample.domain.service.impl;

                import com.thundax.kuzhambu.sample.domain.core.repository.SampleRepository;
                import com.thundax.kuzhambu.sample.domain.service.SampleDomainService;

                public abstract class SampleDomainServiceImpl implements SampleDomainService {
                    private final SampleRepository repository;

                    protected SampleDomainServiceImpl(SampleRepository repository) {
                        this.repository = repository;
                    }
                }
                """);

        assertThrows(
                AssertionError.class,
                () -> NamingArchitectureRuleSupport.assertDomainServiceSourcesUseRepositoryBoundary(
                        tempDir.resolve("src/main/java")));
    }

    @Test
    void domainServiceGateShouldAllowInterfaceAndRepositoryBackedImplementation() throws Exception {
        Path service = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/domain/service");
        Path impl = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/domain/service/impl");
        Files.createDirectories(service);
        Files.createDirectories(impl);
        Files.writeString(
                service.resolve("SampleDomainService.java"),
                """
                package com.thundax.kuzhambu.sample.domain.service;

                public interface SampleDomainService {
                    void bind();
                }
                """);
        Files.writeString(
                impl.resolve("SampleDomainServiceImpl.java"),
                """
                package com.thundax.kuzhambu.sample.domain.service.impl;

                import com.thundax.kuzhambu.sample.domain.core.repository.SampleRepository;
                import com.thundax.kuzhambu.sample.domain.service.SampleDomainService;

                public class SampleDomainServiceImpl implements SampleDomainService {
                    private final SampleRepository repository;

                    public SampleDomainServiceImpl(SampleRepository repository) {
                        this.repository = repository;
                    }

                    @Override
                    public void bind() {
                    }
                }
                """);

        assertDoesNotThrow(() -> NamingArchitectureRuleSupport.assertDomainServiceSourcesUseRepositoryBoundary(
                tempDir.resolve("src/main/java")));
    }

    private Path applicationSourceRoot() {
        return tempDir.resolve("kuzhambu-sample-application/src/main/java");
    }

    private JavaClasses compileAndImport(Path sourceRoot) throws Exception {
        Path classesRoot = tempDir.resolve("target/test-classes");
        Files.createDirectories(classesRoot);
        List<String> sourceFiles;
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            sourceFiles = paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        org.junit.jupiter.api.Assertions.assertNotNull(compiler, "JDK compiler is required");
        List<String> args = new java.util.ArrayList<String>();
        args.add("-d");
        args.add(classesRoot.toString());
        args.addAll(sourceFiles);
        org.junit.jupiter.api.Assertions.assertEquals(0, compiler.run(null, null, null, args.toArray(new String[0])));
        return new ClassFileImporter().importPath(classesRoot);
    }
}
