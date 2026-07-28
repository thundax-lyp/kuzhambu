package com.thundax.kuzhambu.common.test.architecture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
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
        Files.writeString(source.resolve("SampleCreateCommand.java"), "public class SampleCreateCommand {}");

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
                application.resolve("command/SampleCreateCommand.java"), "public class SampleCreateCommand {}");
        Files.writeString(application.resolve("query/SampleSearchQuery.java"), "public class SampleSearchQuery {}");
        Files.writeString(application.resolve("result/SampleDetailResult.java"), "public class SampleDetailResult {}");

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
                "public class PromptSaveCommand {}");
        Files.writeString(
                application.resolve("result/SharedOperationResult.java"), "public class SharedOperationResult {}");

        assertDoesNotThrow(() -> NamingArchitectureRuleSupport.assertApplicationContractSourcesUnderDedicatedPackages(
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

    private Path applicationSourceRoot() {
        return tempDir.resolve("kuzhambu-sample-application/src/main/java");
    }
}
