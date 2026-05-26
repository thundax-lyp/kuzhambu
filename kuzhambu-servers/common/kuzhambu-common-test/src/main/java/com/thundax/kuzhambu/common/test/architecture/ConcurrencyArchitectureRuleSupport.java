package com.thundax.kuzhambu.common.test.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.Source;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class ConcurrencyArchitectureRuleSupport {

    private ConcurrencyArchitectureRuleSupport() {}

    public static JavaClasses importMainClasses(String basePackage) {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(basePackage);
    }

    public static ArchRule shouldNotCreateRawThreads(String basePackage) {
        return ArchRuleDefinition.classes()
                .should(notContainSourceToken("new Thread(", "create raw Thread"))
                .because("Use managed executors. Do not create raw threads.");
    }

    public static ArchRule shouldNotUseExecutorsFactory(String basePackage) {
        return ArchRuleDefinition.classes()
                .should(notContainSourceToken("Executors.", "call Executors factory"))
                .because("Use managed executors. Do not use Executors factories.");
    }

    public static ArchRule shouldNotUseCompletableFutureAsyncWithoutExecutor(String basePackage) {
        return ArchRuleDefinition.classes()
                .should(new ArchCondition<JavaClass>("call CompletableFuture async without executor") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        if (containsCompletableFutureAsyncWithoutExecutor(source(item))) {
                            events.add(SimpleConditionEvent.violated(
                                    item,
                                    item.getFullName()
                                            + " calls CompletableFuture async method without explicit executor"));
                        }
                    }
                })
                .because("Async work must use an explicit managed executor.");
    }

    private static ArchCondition<JavaClass> notContainSourceToken(String token, String description) {
        return new ArchCondition<JavaClass>(description) {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                if (source(item).contains(token)) {
                    events.add(SimpleConditionEvent.violated(
                            item, item.getFullName() + " source contains forbidden token " + token));
                }
            }
        };
    }

    private static boolean containsCompletableFutureAsyncWithoutExecutor(String source) {
        String[] lines = source.split("\\r?\\n");
        for (String line : lines) {
            boolean asyncCall =
                    line.contains("CompletableFuture.runAsync(") || line.contains("CompletableFuture.supplyAsync(");
            if (asyncCall && !line.contains(",")) {
                return true;
            }
        }
        return false;
    }

    private static String source(JavaClass item) {
        try {
            Optional<Source> source = item.getSource();
            if (!source.isPresent()) {
                return "";
            }
            URI uri = source.get().getUri();
            if (uri == null) {
                return sourceByClassName(item);
            }
            if (!uri.toString().endsWith(".java")) {
                return sourceByClassName(item);
            }
            return new String(Files.readAllBytes(Paths.get(uri)), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read source for " + item.getFullName(), ex);
        }
    }

    private static String sourceByClassName(JavaClass item) throws IOException {
        String sourcePath = item.getFullName().replace('.', '/') + ".java";
        List<String> suffixes = Arrays.asList("src/main/java/" + sourcePath, "src/test/java/" + sourcePath);
        try (Stream<Path> paths = Files.walk(Paths.get("."))) {
            Optional<Path> path = paths.filter(Files::isRegularFile)
                    .filter(candidate -> matchesAnySuffix(candidate, suffixes))
                    .findFirst();
            if (!path.isPresent()) {
                return "";
            }
            return new String(Files.readAllBytes(path.get()), StandardCharsets.UTF_8);
        }
    }

    private static boolean matchesAnySuffix(Path path, List<String> suffixes) {
        String value = path.normalize().toString().replace('\\', '/');
        for (String suffix : suffixes) {
            if (value.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }
}
