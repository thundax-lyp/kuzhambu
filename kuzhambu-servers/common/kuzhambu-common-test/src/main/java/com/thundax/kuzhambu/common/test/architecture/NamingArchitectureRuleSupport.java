package com.thundax.kuzhambu.common.test.architecture;

import static com.thundax.kuzhambu.common.test.architecture.ArchitectureAssertions.assertTrue;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class NamingArchitectureRuleSupport {

    private static final String CONFIGURATION_ANNOTATION = "org.springframework.context.annotation.Configuration";
    private static final String CONFIGURATION_PROPERTIES_ANNOTATION =
            "org.springframework.boot.context.properties.ConfigurationProperties";
    private static final String ARCHITECTURE_ROLE_SUFFIXES =
            ".*(Mapper|Converter|Assembler|DAO|Service|Controller|Repository|Facade|Gateway|Adapter|Client|Handler"
                    + "|Processor|Manager|Factory)";
    private static final String GENERIC_HELPER_NAMES = "(List|Object|Data|Common|Base|Generic)Helper";
    private static final Pattern SERVICE_QUERY_SETTER_DECLARATION_PATTERN =
            Pattern.compile("\\bpublic\\s+void\\s+set[A-Z][A-Za-z0-9_]*\\s*\\(");
    private static final Pattern STATIC_METHOD_DECLARATION_PATTERN = Pattern.compile(
            "\\b(?:(?:public|protected|private)\\s+)?static\\s+(?:<[^>]+>\\s+)?[\\w<>?,.\\[\\]\\s]+\\s+\\w+\\s*\\(");
    private static final Pattern METHOD_DECLARATION_PATTERN =
            Pattern.compile("(?m)^\\s*(?:@[\\w.]+(?:\\([^\\n]*\\))?\\s*)*"
                    + "(?:(?:public|protected|private)\\s+)?"
                    + "(?:(?:static|final|synchronized|abstract|native|strictfp)\\s+)*"
                    + "(?:<[^>]+>\\s+)?[\\w<>?,.\\[\\]][\\w<>?,.\\[\\] ]*\\s+(\\w+)\\s*\\([^;{}]*\\)\\s*"
                    + "(?:throws\\s+[^;{]+)?\\{");
    private static final Pattern COMMAND_QUERY_CONSTRUCTION_PATTERN =
            Pattern.compile("\\bnew\\s+([A-Z][A-Za-z0-9_]*(?:Command|Query))\\s*\\(");
    private static final Pattern COMMAND_QUERY_RETURNING_METHOD_DECLARATION_PATTERN =
            Pattern.compile("(?m)^\\s*(?:@[\\w.]+(?:\\([^\\n]*\\))?\\s*)*"
                    + "(?:(?:public|protected|private)\\s+)?"
                    + "(?:(?:static|final|synchronized|abstract|native|strictfp)\\s+)*"
                    + "(?:<[^>]+>\\s+)?(?:[\\w<>?,.\\[\\]]+\\.)?([A-Z][A-Za-z0-9_]*(?:Command|Query))\\s+"
                    + "(\\w+)\\s*\\([^;{}]*\\)\\s*(?:throws\\s+[^;{]+)?\\{");
    private static final Pattern PUBLIC_METHOD_DECLARATION_PATTERN =
            Pattern.compile("(?m)^\\s*(?:@[\\w.]+(?:\\([^\\n]*\\))?\\s*)*public\\s+"
                    + "(?:(?:@[\\w.]+(?:\\([^\\n]*\\))?|static|final|synchronized)\\s+)*"
                    + "(?:<[^>]+>\\s+)?([\\w<>?,.\\[\\] ]+)\\s+"
                    + "(\\w+)\\s*\\(([^;{}]*)\\)\\s*(?:throws\\s+[^;{]+)?\\{");
    private static final Pattern QUERY_PAGE_FIELD_PATTERN =
            Pattern.compile("\\b(?:pageNo|pageSize|pageNum|offset|limit)\\b");
    private static final Pattern EMBEDDED_PAGE_QUERY_PATTERN =
            Pattern.compile("\\bPageQuery\\b|com\\.thundax\\.kuzhambu\\.common\\.core\\.page\\.PageQuery");
    private static final Set<String> SERVICE_QUERY_REQUIRED_ANNOTATIONS =
            new LinkedHashSet<String>(Arrays.asList("Getter", "Setter", "NoArgsConstructor", "AllArgsConstructor"));
    private static final Set<String> APPLICATION_STRUCTURAL_PACKAGES = Set.of(
            "assembler",
            "command",
            "configure",
            "configuration",
            "dto",
            "exception",
            "executor",
            "facade",
            "factory",
            "gateway",
            "handler",
            "helper",
            "impl",
            "misc",
            "model",
            "query",
            "result",
            "resolver",
            "runtime",
            "service",
            "support",
            "util",
            "utils");
    private static final Set<String> ENTITY_REQUIRED_ANNOTATIONS =
            new LinkedHashSet<String>(Arrays.asList("Getter", "Setter", "NoArgsConstructor", "AllArgsConstructor"));
    private static final Set<String> MAPPER_REQUIRED_ANNOTATIONS = new LinkedHashSet<String>(Arrays.asList("Mapper"));
    private static final Set<String> DATA_OBJECT_REQUIRED_LOMBOK_ANNOTATIONS =
            new LinkedHashSet<String>(Arrays.asList("Data", "NoArgsConstructor", "AllArgsConstructor"));
    private static final Set<String> DATA_OBJECT_LOMBOK_ANNOTATIONS = new LinkedHashSet<String>(
            Arrays.asList("Data", "Getter", "Setter", "NoArgsConstructor", "AllArgsConstructor", "Builder"));
    private static final Pattern SERVICE_QUERY_CLASS_DECLARATION_PATTERN =
            Pattern.compile("(?s)(.*?)\\bpublic\\s+class\\s+\\w+Query\\b");
    private static final Pattern ENTITY_CLASS_DECLARATION_PATTERN =
            Pattern.compile("(?s)(.*?)\\bpublic\\s+class\\s+\\w+\\b");
    private static final Pattern INTERFACE_DECLARATION_PATTERN =
            Pattern.compile("(?s)(.*?)\\bpublic\\s+interface\\s+\\w+\\b");
    private static final Pattern SOURCE_ANNOTATION_PATTERN = Pattern.compile("@(?:[\\w.]+\\.)?(\\w+)\\b");
    private static final Pattern PACKAGE_DECLARATION_PATTERN =
            Pattern.compile("(?m)^\\s*package\\s+([a-zA-Z_][\\w]*(?:\\.[a-zA-Z_][\\w]*)*)\\s*;");
    private static final Pattern LOMBOK_ANNOTATION_PATTERN =
            Pattern.compile("(?m)^\\s*@(Getter|Setter|Data|Builder|NoArgsConstructor|AllArgsConstructor|"
                    + "RequiredArgsConstructor|Value|With)\\b");
    private static final Set<String> PRIMITIVE_TYPES =
            Set.of("boolean", "byte", "char", "short", "int", "long", "float", "double");
    private static final Pattern DOMAIN_REPOSITORY_IMPORT_PATTERN =
            Pattern.compile("(?m)^\\s*import\\s+(com\\.thundax\\.kuzhambu\\.[\\w.]+\\.domain\\.[\\w.]+\\.repository\\."
                    + "(\\w+Repository))\\s*;");
    private static final Pattern TYPE_DECLARATION_NAME_PATTERN =
            Pattern.compile("\\b(?:class|interface|enum|record|@interface)\\s+([A-Z][A-Za-z0-9_]*)\\b");

    private NamingArchitectureRuleSupport() {}

    public static void assertHelperNamesBounded(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (javaClass.getSimpleName().matches(GENERIC_HELPER_NAMES)) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue("Helper names must bind a concrete boundary: " + violations, violations.isEmpty());
    }

    public static void assertToolPackagesOutOfArchitectureRoleNames(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (isToolPackage(javaClass) && javaClass.getSimpleName().matches(ARCHITECTURE_ROLE_SUFFIXES)) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue("Tool packages must not use architecture role suffixes: " + violations, violations.isEmpty());
    }

    public static void assertLayerTypeNames(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (isTestType(javaClass) || javaClass.getName().contains("$")) {
                continue;
            }
            collectLayerTypeNameViolation(javaClass, violations);
        }

        assertTrue("Layer types must use the fixed suffix for their package: " + violations, violations.isEmpty());
    }

    public static void assertApplicationServicesUseApplicationServiceSuffix(JavaClasses classes, String basePackage) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (isTestType(javaClass) || javaClass.getName().contains("$")) {
                continue;
            }
            if (!isPackageUnder(javaClass, basePackage + ".application")) {
                continue;
            }
            String simpleName = javaClass.getSimpleName();
            if (simpleName.endsWith("Service") && !simpleName.endsWith("ApplicationService")) {
                violations.add(javaClass.getName());
            }
            if (simpleName.endsWith("ServiceImpl") && !simpleName.endsWith("ApplicationServiceImpl")) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue(
                "Application layer *Service types must be named *ApplicationService: " + violations,
                violations.isEmpty());
    }

    public static void assertCodecPlacement(JavaClasses classes, String basePackage) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (isTestType(javaClass) || javaClass.getName().contains("$")) {
                continue;
            }
            if (!javaClass.getSimpleName().endsWith("Codec")) {
                continue;
            }
            if (!isPackageUnder(javaClass, basePackage + ".domain")
                    || !javaClass.getPackageName().contains(".codec")) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue(
                "*Codec types must be placed under com.thundax.kuzhambu.{module}.domain.{domain}.codec: " + violations,
                violations.isEmpty());
    }

    public static void assertValueObjectPlacement(JavaClasses classes, String basePackage) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (isTestType(javaClass) || javaClass.getName().contains("$")) {
                continue;
            }
            if (!javaClass.getPackageName().contains(".valueobject")) {
                continue;
            }
            if (!matchesModuleSubdomainPackage(
                    javaClass.getPackageName(), basePackage + ".domain", ".model.valueobject")) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue(
                "valueobject packages must only be com.thundax.kuzhambu.{module}.domain.{domain}.model.valueobject: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertValueObjectIdSourcesDeclareNoStaticMethods(Path sourceRoot) {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        if (!Files.exists(sourceRoot)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(NamingArchitectureRuleSupport::isValueObjectIdSource)
                    .filter(NamingArchitectureRuleSupport::containsStaticMethodDeclaration)
                    .map(path -> ArchitectureSourceSupport.repositoryPath(root, path))
                    .forEach(violations::add);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan valueobject *Id source files under " + sourceRoot, e);
        }

        assertTrue(
                "valueobject *Id source must not declare static methods; create/nullable conversion belongs in *Codec: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertApplicationCommandQuerySourcesDeclareNoMethods(Path sourceRoot) {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        if (!Files.exists(sourceRoot)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(NamingArchitectureRuleSupport::isApplicationCommandOrQuerySource)
                    .forEach(path -> collectMethodDeclarationViolations(root, path, violations));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to scan application command/query source files under " + sourceRoot, e);
        }

        assertTrue(
                "Application *Command/*Query source must only define fields; creation and conversion belong in "
                        + "*InterfaceAssembler or application services: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertApplicationCommandQuerySourcesAreRecords(
            Path sourceRoot, Collection<ArchitectureRuleAllowance> legacyAllowances) {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        Map<String, ArchitectureRuleAllowance> allowlist = architectureAllowlist(legacyAllowances);
        Set<String> matchedAllowances = new HashSet<String>();
        List<String> violations = new ArrayList<String>();

        if (!Files.exists(sourceRoot)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(NamingArchitectureRuleSupport::isApplicationCommandOrQuerySource)
                    .forEach(path -> collectApplicationCommandQueryRecordViolation(
                            root, path, violations, allowlist, matchedAllowances));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to scan application command/query source files under " + sourceRoot, e);
        }

        List<String> staleAllowances = allowlist.keySet().stream()
                .filter(key -> !matchedAllowances.contains(key))
                .toList();

        assertTrue(
                "Application *Command/*Query source must be declared as Java records and must not use Lombok "
                        + "annotations. Legacy allowances must include a description and remediation and must be "
                        + "removed after each contract is converted to record. Violations: "
                        + violations
                        + ". Stale allowances: "
                        + staleAllowances,
                violations.isEmpty() && staleAllowances.isEmpty());
    }

    public static void assertApplicationCommandQueryConstructionInAssemblersOrApplicationServices(
            Collection<Path> sourceRoots, Collection<ArchitectureRuleAllowance> legacyAllowances) {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        Map<String, ArchitectureRuleAllowance> allowlist = architectureAllowlist(legacyAllowances);
        Set<String> matchedAllowances = new HashSet<String>();
        List<String> violations = new ArrayList<String>();

        for (Path sourceRoot : sourceRoots) {
            if (!Files.exists(sourceRoot)) {
                continue;
            }
            try (Stream<Path> paths = Files.walk(sourceRoot)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".java"))
                        .forEach(path -> collectCommandQueryConstructionViolations(
                                root, path, violations, allowlist, matchedAllowances));
            } catch (IOException e) {
                throw new IllegalStateException("Failed to scan command/query construction under " + sourceRoot, e);
            }
        }

        List<String> staleAllowances = allowlist.keySet().stream()
                .filter(key -> !matchedAllowances.contains(key))
                .toList();

        assertTrue(
                "Application *Command/*Query construction must stay in *InterfaceAssembler, *FacadeAssembler, or "
                        + "ApplicationService orchestration. Controllers and facade impls must delegate request "
                        + "conversion to assemblers; non-service application helpers must move construction to the "
                        + "calling ApplicationService or a dedicated assembler. Violations: "
                        + violations
                        + ". Stale allowances: "
                        + staleAllowances,
                violations.isEmpty() && staleAllowances.isEmpty());
    }

    public static void assertAssemblersDoNotReturnNullApplicationCommandOrQuery(
            Collection<Path> sourceRoots, Collection<ArchitectureRuleAllowance> legacyAllowances) {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        Map<String, ArchitectureRuleAllowance> allowlist = architectureAllowlist(legacyAllowances);
        Set<String> matchedAllowances = new HashSet<String>();
        List<String> violations = new ArrayList<String>();

        for (Path sourceRoot : sourceRoots) {
            if (!Files.exists(sourceRoot)) {
                continue;
            }
            try (Stream<Path> paths = Files.walk(sourceRoot)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".java"))
                        .forEach(path -> collectAssemblerNullCommandQueryReturnViolations(
                                root, path, violations, allowlist, matchedAllowances));
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Failed to scan assembler command/query null returns under " + sourceRoot, e);
            }
        }

        List<String> staleAllowances = allowlist.keySet().stream()
                .filter(key -> !matchedAllowances.contains(key))
                .toList();

        assertTrue(
                "*InterfaceAssembler and *FacadeAssembler methods returning application *Command/*Query must not "
                        + "return null. Null input handling belongs in caller validation or an explicit use-case "
                        + "branch; assemblers must return concrete contract objects. Violations: "
                        + violations
                        + ". Stale allowances: "
                        + staleAllowances,
                violations.isEmpty() && staleAllowances.isEmpty());
    }

    public static void assertBoundaryAssemblerPublicMethodsUseNonNullContracts(
            Collection<Path> sourceRoots, Collection<ArchitectureRuleAllowance> legacyAllowances) {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        Map<String, ArchitectureRuleAllowance> allowlist = architectureAllowlist(legacyAllowances);
        Set<String> matchedAllowances = new HashSet<String>();
        List<String> violations = new ArrayList<String>();

        for (Path sourceRoot : sourceRoots) {
            if (!Files.exists(sourceRoot)) {
                continue;
            }
            try (Stream<Path> paths = Files.walk(sourceRoot)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".java"))
                        .filter(NamingArchitectureRuleSupport::isBoundaryAssemblerSource)
                        .forEach(path -> collectBoundaryAssemblerNonNullContractViolations(
                                root, path, violations, allowlist, matchedAllowances));
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Failed to scan boundary assembler nullness contracts under " + sourceRoot, e);
            }
        }

        List<String> staleAllowances = allowlist.keySet().stream()
                .filter(key -> !matchedAllowances.contains(key))
                .toList();

        assertTrue(
                "Interface, facade and application assembler public methods must use non-null contracts. Public "
                        + "assembler methods must be annotated with org.springframework.lang.NonNull, must not return "
                        + "null, and each reference parameter must be annotated with org.springframework.lang.NonNull "
                        + "and guarded with "
                        + "Objects.requireNonNull(parameter, ...). Null-as-default behavior must be explicit and "
                        + "allowlisted until migrated. Violations: "
                        + violations
                        + ". Stale allowances: "
                        + staleAllowances,
                violations.isEmpty() && staleAllowances.isEmpty());
    }

    public static void assertApplicationQueriesDoNotOwnPageState(
            Path sourceRoot, Collection<ArchitectureRuleAllowance> legacyAllowances) {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        Map<String, ArchitectureRuleAllowance> allowlist = architectureAllowlist(legacyAllowances);
        Set<String> matchedAllowances = new HashSet<String>();
        List<String> violations = new ArrayList<String>();

        if (!Files.exists(sourceRoot)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(NamingArchitectureRuleSupport::isServiceQuerySource)
                    .forEach(path -> collectApplicationQueryPageStateViolations(
                            root, path, violations, allowlist, matchedAllowances));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan application query page state under " + sourceRoot, e);
        }

        List<String> staleAllowances = allowlist.keySet().stream()
                .filter(key -> !matchedAllowances.contains(key))
                .toList();

        assertTrue(
                "Application *Query types must not own pagination state. PageQuery is the single normalized "
                        + "application pagination contract; paged use cases should accept BusinessQuery + PageQuery, "
                        + "and query fields must not include pageNo/pageSize/pageNum/offset/limit or embedded PageQuery. "
                        + "Violations: "
                        + violations
                        + ". Stale allowances: "
                        + staleAllowances,
                violations.isEmpty() && staleAllowances.isEmpty());
    }

    public static void assertApplicationContractSourcesUnderDedicatedPackages(Path sourceRoot) {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        if (!Files.exists(sourceRoot) || !isApplicationModuleSourceRoot(sourceRoot)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(NamingArchitectureRuleSupport::isApplicationContractSource)
                    .filter(path -> !isApplicationContractUnderDedicatedPackage(path))
                    .map(path -> ArchitectureSourceSupport.repositoryPath(root, path))
                    .forEach(violations::add);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan application contract source files under " + sourceRoot, e);
        }
        Path classesRoot = sourceRoot
                .toAbsolutePath()
                .normalize()
                .resolve("../../..")
                .normalize()
                .resolve("target/classes");
        if (Files.isDirectory(classesRoot)) {
            for (JavaClass javaClass : new ClassFileImporter().importPath(classesRoot)) {
                if (isDeclaredUnderSourceRoot(javaClass, sourceRoot)
                        && isApplicationContractType(javaClass)
                        && !isApplicationContractUnderDedicatedPackage(javaClass)) {
                    violations.add(javaClass.getName());
                }
            }
        }

        assertTrue(
                "In *-application modules, *Command/*Query/*Result sources and declared types must be placed in "
                        + "application/**/command, application/**/query, or application/**/result: "
                        + violations,
                violations.isEmpty());
    }

    private static boolean isDeclaredUnderSourceRoot(JavaClass javaClass, Path sourceRoot) {
        return javaClass
                .getSource()
                .flatMap(source -> source.getFileName())
                .map(fileName -> sourceRoot
                        .resolve(javaClass.getPackageName().replace('.', '/'))
                        .resolve(fileName))
                .filter(Files::isRegularFile)
                .isPresent();
    }

    private static void collectApplicationCommandQueryRecordViolation(
            Path root,
            Path path,
            List<String> violations,
            Map<String, ArchitectureRuleAllowance> allowlist,
            Set<String> matchedAllowances) {
        String source = ArchitectureSourceSupport.readSourceWithoutComments(path);
        String typeName = applicationCommandQueryTypeName(source, path);
        boolean record = Pattern.compile("\\brecord\\s+" + pathFileNameWithoutExtension(path) + "\\b")
                .matcher(source)
                .find();
        boolean lombokAnnotated = LOMBOK_ANNOTATION_PATTERN.matcher(source).find();
        if (record && !lombokAnnotated) {
            return;
        }
        String key = "COMMAND_QUERY_RECORD:" + typeName;
        if (isAllowlisted(key, allowlist, matchedAllowances)) {
            return;
        }
        List<String> reasons = new ArrayList<String>();
        if (!record) {
            reasons.add("not record");
        }
        if (lombokAnnotated) {
            reasons.add("uses Lombok annotation");
        }
        violations.add(key + " in " + ArchitectureSourceSupport.repositoryPath(root, path) + " is invalid: " + reasons);
    }

    private static void collectCommandQueryConstructionViolations(
            Path root,
            Path path,
            List<String> violations,
            Map<String, ArchitectureRuleAllowance> allowlist,
            Set<String> matchedAllowances) {
        String source = ArchitectureSourceSupport.readSourceWithoutComments(path);
        Matcher matcher = COMMAND_QUERY_CONSTRUCTION_PATTERN.matcher(source);
        String ownerTypeName = sourceTypeName(source, path);
        if (isAllowedCommandQueryConstructionOwner(ownerTypeName)) {
            return;
        }
        Map<String, Integer> occurrenceCounts = new HashMap<String, Integer>();
        while (matcher.find()) {
            String constructedType = matcher.group(1);
            int occurrence = occurrenceCounts.merge(constructedType, 1, Integer::sum);
            String key = commandQueryConstructionKey(ownerTypeName, constructedType, occurrence);
            if (isAllowlisted(key, allowlist, matchedAllowances)) {
                continue;
            }
            violations.add(key + " in " + ArchitectureSourceSupport.repositoryPath(root, path));
        }
    }

    private static boolean isAllowedCommandQueryConstructionOwner(String ownerTypeName) {
        String simpleName = ownerTypeName.substring(ownerTypeName.lastIndexOf('.') + 1);
        if (simpleName.endsWith("InterfaceAssembler") || simpleName.endsWith("FacadeAssembler")) {
            return true;
        }
        return ownerTypeName.contains(".application.")
                && (simpleName.endsWith("ApplicationService") || simpleName.endsWith("ApplicationServiceImpl"));
    }

    public static String commandQueryConstructionKey(String ownerTypeName, String constructedType, int occurrence) {
        return "COMMAND_QUERY_CONSTRUCTION:" + ownerTypeName + "#" + constructedType + ":" + occurrence;
    }

    private static void collectAssemblerNullCommandQueryReturnViolations(
            Path root,
            Path path,
            List<String> violations,
            Map<String, ArchitectureRuleAllowance> allowlist,
            Set<String> matchedAllowances) {
        String source = ArchitectureSourceSupport.readSourceWithoutComments(path);
        String ownerTypeName = sourceTypeName(source, path);
        String simpleName = ownerTypeName.substring(ownerTypeName.lastIndexOf('.') + 1);
        if (!simpleName.endsWith("InterfaceAssembler") && !simpleName.endsWith("FacadeAssembler")) {
            return;
        }

        Matcher matcher = COMMAND_QUERY_RETURNING_METHOD_DECLARATION_PATTERN.matcher(source);
        Map<String, Integer> occurrenceCounts = new HashMap<String, Integer>();
        while (matcher.find()) {
            String returnType = matcher.group(1);
            String methodName = matcher.group(2);
            if (!methodBodyContainsReturnNull(source, matcher.end() - 1)) {
                continue;
            }
            String occurrenceKey = methodName + ":" + returnType;
            int occurrence = occurrenceCounts.merge(occurrenceKey, 1, Integer::sum);
            String key = commandQueryAssemblerNullReturnKey(ownerTypeName, methodName, returnType, occurrence);
            if (isAllowlisted(key, allowlist, matchedAllowances)) {
                continue;
            }
            violations.add(key + " in " + ArchitectureSourceSupport.repositoryPath(root, path));
        }
    }

    private static void collectApplicationQueryPageStateViolations(
            Path root,
            Path path,
            List<String> violations,
            Map<String, ArchitectureRuleAllowance> allowlist,
            Set<String> matchedAllowances) {
        String source = ArchitectureSourceSupport.readSourceWithoutComments(path);
        String typeName = sourceTypeName(source, path);
        String simpleName = pathFileNameWithoutExtension(path);
        if (simpleName.contains("Page")) {
            collectAllowlistedViolation(
                    applicationQueryPageTypeKey(typeName),
                    ArchitectureSourceSupport.repositoryPath(root, path) + " names a business query as *Page*Query",
                    violations,
                    allowlist,
                    matchedAllowances);
        }
        if (QUERY_PAGE_FIELD_PATTERN.matcher(source).find()) {
            collectAllowlistedViolation(
                    applicationQueryPageFieldsKey(typeName),
                    ArchitectureSourceSupport.repositoryPath(root, path)
                            + " declares raw pagination fields such as pageNo/pageSize/pageNum/offset/limit",
                    violations,
                    allowlist,
                    matchedAllowances);
        }
        if (EMBEDDED_PAGE_QUERY_PATTERN.matcher(source).find()) {
            collectAllowlistedViolation(
                    applicationQueryEmbeddedPageQueryKey(typeName),
                    ArchitectureSourceSupport.repositoryPath(root, path)
                            + " embeds PageQuery inside a business Query instead of passing it as a separate "
                            + "ApplicationService parameter",
                    violations,
                    allowlist,
                    matchedAllowances);
        }
    }

    private static void collectBoundaryAssemblerNonNullContractViolations(
            Path root,
            Path path,
            List<String> violations,
            Map<String, ArchitectureRuleAllowance> allowlist,
            Set<String> matchedAllowances) {
        String source = ArchitectureSourceSupport.readSourceWithoutComments(path);
        String ownerTypeName = sourceTypeName(source, path);
        Matcher matcher = PUBLIC_METHOD_DECLARATION_PATTERN.matcher(source);
        Map<String, Integer> returnAnnotationOccurrenceCounts = new HashMap<String, Integer>();
        Map<String, Integer> nullReturnOccurrenceCounts = new HashMap<String, Integer>();
        Map<String, Integer> parameterOccurrenceCounts = new HashMap<String, Integer>();
        while (matcher.find()) {
            String returnType = compactType(matcher.group(1));
            String methodName = matcher.group(2);
            String parameters = matcher.group(3);
            String declaration = matcher.group(0);
            int bodyStart = matcher.end() - 1;
            String body = methodBody(source, bodyStart);
            if (body == null) {
                continue;
            }
            collectBoundaryAssemblerReturnAnnotationViolation(
                    root,
                    path,
                    violations,
                    allowlist,
                    matchedAllowances,
                    ownerTypeName,
                    methodName,
                    returnType,
                    returnAnnotationOccurrenceCounts,
                    declaration,
                    source);
            collectBoundaryAssemblerNullReturnViolation(
                    root,
                    path,
                    violations,
                    allowlist,
                    matchedAllowances,
                    ownerTypeName,
                    methodName,
                    returnType,
                    nullReturnOccurrenceCounts,
                    body);
            collectBoundaryAssemblerNullableParameterViolations(
                    root,
                    path,
                    violations,
                    allowlist,
                    matchedAllowances,
                    ownerTypeName,
                    methodName,
                    parameterOccurrenceCounts,
                    parameters,
                    body,
                    source);
        }
    }

    private static void collectBoundaryAssemblerNullReturnViolation(
            Path root,
            Path path,
            List<String> violations,
            Map<String, ArchitectureRuleAllowance> allowlist,
            Set<String> matchedAllowances,
            String ownerTypeName,
            String methodName,
            String returnType,
            Map<String, Integer> occurrenceCounts,
            String body) {
        if ("void".equals(returnType) || !containsNullReturnExpression(body)) {
            return;
        }
        int occurrence = occurrenceCounts.merge(methodName + ":" + returnType, 1, Integer::sum);
        String key = boundaryAssemblerNullReturnKey(ownerTypeName, methodName, returnType, occurrence);
        if (isAllowlisted(key, allowlist, matchedAllowances)) {
            return;
        }
        violations.add(key + " in " + ArchitectureSourceSupport.repositoryPath(root, path));
    }

    private static void collectBoundaryAssemblerReturnAnnotationViolation(
            Path root,
            Path path,
            List<String> violations,
            Map<String, ArchitectureRuleAllowance> allowlist,
            Set<String> matchedAllowances,
            String ownerTypeName,
            String methodName,
            String returnType,
            Map<String, Integer> occurrenceCounts,
            String declaration,
            String source) {
        if ("void".equals(returnType) || hasNonNullAnnotation(methodHeader(declaration), source)) {
            return;
        }
        int occurrence = occurrenceCounts.merge(methodName + ":" + returnType, 1, Integer::sum);
        String key = boundaryAssemblerNullReturnKey(ownerTypeName, methodName, returnType, occurrence);
        if (isAllowlisted(key, allowlist, matchedAllowances)) {
            return;
        }
        violations.add(key + " in " + ArchitectureSourceSupport.repositoryPath(root, path)
                + " lacks org.springframework.lang.NonNull");
    }

    private static void collectBoundaryAssemblerNullableParameterViolations(
            Path root,
            Path path,
            List<String> violations,
            Map<String, ArchitectureRuleAllowance> allowlist,
            Set<String> matchedAllowances,
            String ownerTypeName,
            String methodName,
            Map<String, Integer> occurrenceCounts,
            String parameters,
            String body,
            String source) {
        for (MethodParameter parameter : parseMethodParameters(parameters, source)) {
            if (parameter.referenceType()
                    && (!parameter.nonNullAnnotated() || !containsRequireNonNullGuard(body, parameter.name()))) {
                int occurrence = occurrenceCounts.merge(
                        methodName + ":" + parameter.name() + ":" + parameter.type(), 1, Integer::sum);
                String key = boundaryAssemblerNullableParameterKey(
                        ownerTypeName, methodName, parameter.name(), parameter.type(), occurrence);
                if (isAllowlisted(key, allowlist, matchedAllowances)) {
                    continue;
                }
                violations.add(key + " in " + ArchitectureSourceSupport.repositoryPath(root, path));
            }
        }
    }

    private static void collectAllowlistedViolation(
            String key,
            String description,
            List<String> violations,
            Map<String, ArchitectureRuleAllowance> allowlist,
            Set<String> matchedAllowances) {
        if (isAllowlisted(key, allowlist, matchedAllowances)) {
            return;
        }
        violations.add(key + " in " + description);
    }

    public static String applicationQueryPageTypeKey(String typeName) {
        return "PAGE_QUERY_TYPE:" + typeName;
    }

    public static String applicationQueryPageFieldsKey(String typeName) {
        return "PAGE_QUERY_FIELDS:" + typeName;
    }

    public static String applicationQueryEmbeddedPageQueryKey(String typeName) {
        return "PAGE_QUERY_EMBEDDED:" + typeName;
    }

    private static boolean methodBodyContainsReturnNull(String source, int openingBraceIndex) {
        String body = methodBody(source, openingBraceIndex);
        return body != null && containsNullReturnExpression(body);
    }

    private static String methodBody(String source, int openingBraceIndex) {
        int depth = 0;
        for (int index = openingBraceIndex; index < source.length(); index++) {
            char current = source.charAt(index);
            if (current == '{') {
                depth++;
                continue;
            }
            if (current == '}') {
                depth--;
                if (depth == 0) {
                    return source.substring(openingBraceIndex + 1, index);
                }
            }
        }
        return null;
    }

    private static boolean containsNullReturnExpression(String body) {
        Matcher matcher = Pattern.compile("\\breturn\\b").matcher(body);
        while (matcher.find()) {
            int semicolon = findReturnSemicolon(body, matcher.end());
            if (semicolon < 0) {
                continue;
            }
            if (expressionCanEvaluateToNull(body.substring(matcher.end(), semicolon))) {
                return true;
            }
        }
        return false;
    }

    private static int findReturnSemicolon(String body, int start) {
        int braceDepth = 0;
        int bracketDepth = 0;
        int parenDepth = 0;
        boolean inSingleQuotedString = false;
        boolean inDoubleQuotedString = false;
        boolean escaped = false;
        for (int index = start; index < body.length(); index++) {
            char current = body.charAt(index);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (current == '\\') {
                escaped = inSingleQuotedString || inDoubleQuotedString;
                continue;
            }
            if (inSingleQuotedString) {
                if (current == '\'') {
                    inSingleQuotedString = false;
                }
                continue;
            }
            if (inDoubleQuotedString) {
                if (current == '"') {
                    inDoubleQuotedString = false;
                }
                continue;
            }
            if (current == '\'') {
                inSingleQuotedString = true;
                continue;
            }
            if (current == '"') {
                inDoubleQuotedString = true;
                continue;
            }
            if (current == '{') {
                braceDepth++;
            } else if (current == '}' && braceDepth > 0) {
                braceDepth--;
            } else if (current == '[') {
                bracketDepth++;
            } else if (current == ']' && bracketDepth > 0) {
                bracketDepth--;
            } else if (current == '(') {
                parenDepth++;
            } else if (current == ')' && parenDepth > 0) {
                parenDepth--;
            } else if (current == ';' && braceDepth == 0 && bracketDepth == 0 && parenDepth == 0) {
                return index;
            }
        }
        return -1;
    }

    private static boolean expressionCanEvaluateToNull(String expression) {
        String normalized = stripOuterParentheses(expression.trim());
        if ("null".equals(normalized)) {
            return true;
        }
        if (castExpressionCanEvaluateToNull(normalized) || switchExpressionCanEvaluateToNull(normalized)) {
            return true;
        }
        int questionIndex = findTopLevelCharacter(normalized, '?', 0);
        if (questionIndex < 0) {
            return false;
        }
        int colonIndex = findMatchingTernaryColon(normalized, questionIndex + 1);
        return colonIndex > questionIndex
                && (expressionCanEvaluateToNull(normalized.substring(questionIndex + 1, colonIndex))
                        || expressionCanEvaluateToNull(normalized.substring(colonIndex + 1)));
    }

    private static boolean castExpressionCanEvaluateToNull(String expression) {
        if (!expression.startsWith("(")) {
            return false;
        }
        int closingIndex = skipNestedExpression(expression, 0);
        if (closingIndex <= 0 || closingIndex >= expression.length() - 1) {
            return false;
        }
        String castType = expression.substring(1, closingIndex).trim();
        String operand = expression.substring(closingIndex + 1).trim();
        return !castType.isBlank() && expressionCanEvaluateToNull(operand);
    }

    private static boolean switchExpressionCanEvaluateToNull(String expression) {
        if (!expression.startsWith("switch")) {
            return false;
        }
        int bodyStart = expression.indexOf('{');
        if (bodyStart < 0) {
            return false;
        }
        int bodyEnd = skipNestedExpression(expression, bodyStart);
        String body = expression.substring(bodyStart + 1, bodyEnd);
        if (Pattern.compile("->\\s*(?:\\([^;{}]+\\)\\s*)?null\\b").matcher(body).find()) {
            return true;
        }
        Matcher matcher = Pattern.compile("\\byield\\b").matcher(body);
        while (matcher.find()) {
            int semicolon = findReturnSemicolon(body, matcher.end());
            if (semicolon >= 0 && expressionCanEvaluateToNull(body.substring(matcher.end(), semicolon))) {
                return true;
            }
        }
        return false;
    }

    private static String stripOuterParentheses(String expression) {
        String normalized = expression;
        while (normalized.startsWith("(") && normalized.endsWith(")") && wrapsWholeExpression(normalized)) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }
        return normalized;
    }

    private static boolean wrapsWholeExpression(String expression) {
        int depth = 0;
        for (int index = 0; index < expression.length(); index++) {
            char current = expression.charAt(index);
            if (current == '(') {
                depth++;
            } else if (current == ')') {
                depth--;
                if (depth == 0 && index < expression.length() - 1) {
                    return false;
                }
            }
        }
        return depth == 0;
    }

    private static int findMatchingTernaryColon(String expression, int start) {
        int nestedTernaryDepth = 0;
        for (int index = start; index < expression.length(); index++) {
            char current = expression.charAt(index);
            if (current == '?') {
                nestedTernaryDepth++;
            } else if (current == ':') {
                if (nestedTernaryDepth == 0) {
                    return index;
                }
                nestedTernaryDepth--;
            } else if (current == '(' || current == '[' || current == '{') {
                index = skipNestedExpression(expression, index);
            }
        }
        return -1;
    }

    private static int findTopLevelCharacter(String expression, char target, int start) {
        for (int index = start; index < expression.length(); index++) {
            char current = expression.charAt(index);
            if (current == target) {
                return index;
            }
            if (current == '(' || current == '[' || current == '{') {
                index = skipNestedExpression(expression, index);
            }
        }
        return -1;
    }

    private static int skipNestedExpression(String expression, int openingIndex) {
        char opening = expression.charAt(openingIndex);
        char closing = opening == '(' ? ')' : opening == '[' ? ']' : '}';
        int depth = 0;
        for (int index = openingIndex; index < expression.length(); index++) {
            char current = expression.charAt(index);
            if (current == opening) {
                depth++;
            } else if (current == closing) {
                depth--;
                if (depth == 0) {
                    return index;
                }
            }
        }
        return expression.length() - 1;
    }

    public static String commandQueryAssemblerNullReturnKey(
            String ownerTypeName, String methodName, String returnType, int occurrence) {
        return "COMMAND_QUERY_ASSEMBLER_NULL_RETURN:"
                + ownerTypeName
                + "#"
                + methodName
                + ":"
                + returnType
                + ":"
                + occurrence;
    }

    public static String boundaryAssemblerNullReturnKey(
            String ownerTypeName, String methodName, String returnType, int occurrence) {
        return "BOUNDARY_ASSEMBLER_NULL_RETURN:"
                + ownerTypeName
                + "#"
                + methodName
                + ":"
                + returnType
                + ":"
                + occurrence;
    }

    public static String boundaryAssemblerNullableParameterKey(
            String ownerTypeName, String methodName, String parameterName, String parameterType, int occurrence) {
        return "BOUNDARY_ASSEMBLER_NULL_PARAMETER:"
                + ownerTypeName
                + "#"
                + methodName
                + ":"
                + parameterName
                + ":"
                + parameterType
                + ":"
                + occurrence;
    }

    public static List<String> boundaryAssemblerNonNullContractViolationKeysForClasses(String... classNames) {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        return boundaryAssemblerNonNullContractViolationKeysForClasses(List.of(root), classNames);
    }

    public static List<String> boundaryAssemblerNonNullContractViolationKeysForClasses(
            Collection<Path> sourceRoots, String... classNames) {
        List<String> keys = new ArrayList<String>();
        for (String className : classNames) {
            Path sourcePath = findSourcePath(sourceRoots, className);
            if (sourcePath == null) {
                throw new IllegalArgumentException("Can not find boundary assembler source for " + className);
            }
            keys.addAll(boundaryAssemblerNonNullContractViolationKeys(sourcePath));
        }
        return keys;
    }

    private static Path findSourcePath(Collection<Path> sourceRoots, String className) {
        String suffix = className.replace('.', '/') + ".java";
        for (Path sourceRoot : sourceRoots) {
            if (!Files.exists(sourceRoot)) {
                continue;
            }
            try (Stream<Path> paths = Files.walk(sourceRoot)) {
                Optional<Path> sourcePath = paths.filter(Files::isRegularFile)
                        .filter(path ->
                                ArchitectureSourceSupport.normalizePath(path).endsWith(suffix))
                        .findFirst();
                if (sourcePath.isPresent()) {
                    return sourcePath.get();
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to find boundary assembler source for " + className, e);
            }
        }
        return null;
    }

    private static List<String> boundaryAssemblerNonNullContractViolationKeys(Path path) {
        String source = ArchitectureSourceSupport.readSourceWithoutComments(path);
        String ownerTypeName = sourceTypeName(source, path);
        Matcher matcher = PUBLIC_METHOD_DECLARATION_PATTERN.matcher(source);
        Map<String, Integer> returnAnnotationOccurrenceCounts = new HashMap<String, Integer>();
        Map<String, Integer> nullReturnOccurrenceCounts = new HashMap<String, Integer>();
        Map<String, Integer> parameterOccurrenceCounts = new HashMap<String, Integer>();
        Set<String> keys = new LinkedHashSet<String>();
        while (matcher.find()) {
            String returnType = compactType(matcher.group(1));
            String methodName = matcher.group(2);
            String parameters = matcher.group(3);
            String declaration = matcher.group(0);
            String body = methodBody(source, matcher.end() - 1);
            if (body == null) {
                continue;
            }
            if (!"void".equals(returnType) && !hasNonNullAnnotation(methodHeader(declaration), source)) {
                int occurrence = returnAnnotationOccurrenceCounts.merge(methodName + ":" + returnType, 1, Integer::sum);
                keys.add(boundaryAssemblerNullReturnKey(ownerTypeName, methodName, returnType, occurrence));
            }
            if (!"void".equals(returnType) && containsNullReturnExpression(body)) {
                int occurrence = nullReturnOccurrenceCounts.merge(methodName + ":" + returnType, 1, Integer::sum);
                keys.add(boundaryAssemblerNullReturnKey(ownerTypeName, methodName, returnType, occurrence));
            }
            for (MethodParameter parameter : parseMethodParameters(parameters, source)) {
                if (parameter.referenceType()
                        && (!parameter.nonNullAnnotated() || !containsRequireNonNullGuard(body, parameter.name()))) {
                    int occurrence = parameterOccurrenceCounts.merge(
                            methodName + ":" + parameter.name() + ":" + parameter.type(), 1, Integer::sum);
                    keys.add(boundaryAssemblerNullableParameterKey(
                            ownerTypeName, methodName, parameter.name(), parameter.type(), occurrence));
                }
            }
        }
        return new ArrayList<String>(keys);
    }

    private static String applicationCommandQueryTypeName(String source, Path path) {
        return sourceTypeName(source, path);
    }

    private static String sourceTypeName(String source, Path path) {
        Matcher matcher = PACKAGE_DECLARATION_PATTERN.matcher(source);
        if (!matcher.find()) {
            return pathFileNameWithoutExtension(path);
        }
        return matcher.group(1) + "." + pathFileNameWithoutExtension(path);
    }

    private static String pathFileNameWithoutExtension(Path path) {
        String fileName = path.getFileName().toString();
        int extensionIndex = fileName.lastIndexOf('.');
        return extensionIndex < 0 ? fileName : fileName.substring(0, extensionIndex);
    }

    private static Map<String, ArchitectureRuleAllowance> architectureAllowlist(
            Collection<ArchitectureRuleAllowance> legacyAllowances) {
        Map<String, ArchitectureRuleAllowance> allowlist = new LinkedHashMap<String, ArchitectureRuleAllowance>();
        for (ArchitectureRuleAllowance allowance : legacyAllowances) {
            ArchitectureRuleAllowance previous = allowlist.put(allowance.key(), allowance);
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate architecture allowlist key: " + allowance.key());
            }
        }
        return allowlist;
    }

    private static boolean isAllowlisted(
            String key, Map<String, ArchitectureRuleAllowance> allowlist, Set<String> matchedAllowances) {
        if (!allowlist.containsKey(key)) {
            return isWildcardAllowlisted(key, allowlist, matchedAllowances);
        }
        matchedAllowances.add(key);
        return true;
    }

    private static boolean isWildcardAllowlisted(
            String key, Map<String, ArchitectureRuleAllowance> allowlist, Set<String> matchedAllowances) {
        for (String allowanceKey : allowlist.keySet()) {
            if (!allowanceKey.contains("*")) {
                continue;
            }
            String pattern = Pattern.quote(allowanceKey).replace("*", "\\E.*\\Q");
            if (Pattern.compile(pattern).matcher(key).matches()) {
                matchedAllowances.add(allowanceKey);
                return true;
            }
        }
        return false;
    }

    public static void assertBaseIdTypes(JavaClasses classes, String basePackage) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (isTestType(javaClass) || javaClass.getName().contains("$")) {
                continue;
            }
            if (!javaClass.getSimpleName().endsWith("Id")) {
                continue;
            }
            if (!matchesModuleSubdomainPackage(
                    javaClass.getPackageName(), basePackage + ".domain", ".model.valueobject")) {
                violations.add(javaClass.getName() + " must stay in domain.{domain}.model.valueobject");
                continue;
            }
            if (!javaClass.getModifiers().contains(JavaModifier.FINAL)) {
                violations.add(javaClass.getName() + " must be final");
            }
            if (!extendsBaseId(javaClass)) {
                violations.add(javaClass.getName() + " must extend a common Base*Id type");
            }
        }

        assertTrue("Strong ID types must be final Base*Id value objects: " + violations, violations.isEmpty());
    }

    public static void assertEntityPlacement(JavaClasses classes, String basePackage) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (isTestType(javaClass) || javaClass.getName().contains("$")) {
                continue;
            }
            if (!javaClass.getPackageName().contains(".entity")) {
                continue;
            }
            if (!matchesModuleSubdomainPackage(javaClass.getPackageName(), basePackage + ".domain", ".model.entity")) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue(
                "entity packages must only be com.thundax.kuzhambu.{module}.domain.{domain}.model.entity: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertDomainEnumPlacement(JavaClasses classes, String basePackage) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (isTestType(javaClass) || javaClass.getName().contains("$") || !javaClass.isEnum()) {
                continue;
            }
            if (!isPackageUnder(javaClass, basePackage + ".domain")) {
                continue;
            }
            if (!matchesModuleSubdomainPackage(javaClass.getPackageName(), basePackage + ".domain", ".model.enums")) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue(
                "domain enums must only be placed under com.thundax.kuzhambu.{module}.domain.{domain}.model.enums: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertDomainServiceSourcesUseRepositoryBoundary(
            Path sourceRoot, Collection<ArchitectureRuleAllowance> legacyAllowances) {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        Map<String, ArchitectureRuleAllowance> allowlist = architectureAllowlist(legacyAllowances);
        Set<String> matchedAllowances = new HashSet<String>();
        List<String> violations = new ArrayList<String>();

        if (!Files.exists(sourceRoot)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .forEach(path ->
                            collectDomainServiceSourceViolations(root, path, violations, allowlist, matchedAllowances));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan domain service sources under " + sourceRoot, e);
        }

        List<String> staleAllowances = allowlist.keySet().stream()
                .filter(key -> !matchedAllowances.contains(key))
                .toList();

        assertTrue(
                "DomainService is reserved for domain rules that coordinate repository-backed aggregate state. "
                        + "Domain services must be named *DomainService or *DomainServiceImpl, must live under "
                        + "domain/service or domain/service/impl, and concrete domain services must depend on a "
                        + "domain repository. Pure normalization, calculation, factory, policy, or support code "
                        + "must use a more specific helper role instead. Violations: "
                        + violations
                        + ". Stale allowances: "
                        + staleAllowances,
                violations.isEmpty() && staleAllowances.isEmpty());
    }

    public static void assertDomainServiceSourcesUseRepositoryBoundary(Path sourceRoot) {
        assertDomainServiceSourcesUseRepositoryBoundary(sourceRoot, List.of());
    }

    public static void assertRepositoryPlacement(JavaClasses classes, String basePackage) {
        assertSuffixPlacement(
                classes,
                basePackage + ".domain",
                ".repository",
                "Repository",
                true,
                "*Repository interfaces must be placed under com.thundax.kuzhambu.{module}.domain.{domain}.repository");
    }

    private static void collectDomainServiceSourceViolations(
            Path root,
            Path path,
            List<String> violations,
            Map<String, ArchitectureRuleAllowance> allowlist,
            Set<String> matchedAllowances) {
        String source = ArchitectureSourceSupport.readSourceWithoutComments(path);
        String typeName = sourceTypeName(source, path);
        String packageName = typeName.substring(0, typeName.lastIndexOf('.'));
        String simpleName = pathFileNameWithoutExtension(path);
        boolean servicePackage = isDomainServicePackage(packageName);
        boolean domainServiceName = simpleName.endsWith("DomainService");
        boolean domainServiceImplName = simpleName.endsWith("DomainServiceImpl");

        collectAdditionalDomainServiceDeclarationViolations(
                root, path, source, packageName, simpleName, violations, allowlist, matchedAllowances);

        boolean allowedDomainServiceShape = isAllowedDomainServiceShape(source, packageName, simpleName);
        if (servicePackage && !allowedDomainServiceShape) {
            collectAllowlistedViolation(
                    domainServiceShapeKey(typeName),
                    ArchitectureSourceSupport.repositoryPath(root, path)
                            + " is under domain service package but is not a *DomainService boundary",
                    violations,
                    allowlist,
                    matchedAllowances);
        }
        if (!servicePackage && (domainServiceName || domainServiceImplName)) {
            collectAllowlistedViolation(
                    domainServiceShapeKey(typeName),
                    ArchitectureSourceSupport.repositoryPath(root, path)
                            + " uses DomainService naming outside domain/service or domain/service/impl",
                    violations,
                    allowlist,
                    matchedAllowances);
        }
        if (servicePackage
                && (domainServiceName || domainServiceImplName)
                && allowedDomainServiceShape
                && isConcreteDomainServiceSource(source, simpleName)
                && !containsDomainRepositoryReference(declarationScopedSource(source, simpleName))) {
            collectAllowlistedViolation(
                    domainServiceRepositoryKey(typeName),
                    ArchitectureSourceSupport.repositoryPath(root, path)
                            + " is a concrete DomainService without a domain Repository dependency",
                    violations,
                    allowlist,
                    matchedAllowances);
        }
    }

    private static void collectAdditionalDomainServiceDeclarationViolations(
            Path root,
            Path path,
            String source,
            String packageName,
            String fileSimpleName,
            List<String> violations,
            Map<String, ArchitectureRuleAllowance> allowlist,
            Set<String> matchedAllowances) {
        Matcher matcher = TYPE_DECLARATION_NAME_PATTERN.matcher(sourceWithoutLiterals(source));
        while (matcher.find()) {
            String declaredName = matcher.group(1);
            if (declaredName.equals(fileSimpleName)) {
                continue;
            }
            collectDomainServiceDeclarationViolations(
                    root, path, source, packageName, declaredName, violations, allowlist, matchedAllowances);
        }
    }

    private static void collectDomainServiceDeclarationViolations(
            Path root,
            Path path,
            String source,
            String packageName,
            String declaredName,
            List<String> violations,
            Map<String, ArchitectureRuleAllowance> allowlist,
            Set<String> matchedAllowances) {
        boolean servicePackage = isDomainServicePackage(packageName);
        boolean domainServiceName = declaredName.endsWith("DomainService");
        boolean domainServiceImplName = declaredName.endsWith("DomainServiceImpl");
        String declaredTypeName = packageName + "." + declaredName;

        if (servicePackage && !isAllowedDomainServiceShape(source, packageName, declaredName)) {
            collectAllowlistedViolation(
                    domainServiceShapeKey(declaredTypeName),
                    ArchitectureSourceSupport.repositoryPath(root, path)
                            + " declares "
                            + declaredName
                            + " under domain service package but it is not a valid *DomainService boundary",
                    violations,
                    allowlist,
                    matchedAllowances);
        }
        if (!servicePackage && (domainServiceName || domainServiceImplName)) {
            collectAllowlistedViolation(
                    domainServiceShapeKey(declaredTypeName),
                    ArchitectureSourceSupport.repositoryPath(root, path)
                            + " declares "
                            + declaredName
                            + " outside domain/service or domain/service/impl",
                    violations,
                    allowlist,
                    matchedAllowances);
        }
        if (servicePackage
                && (domainServiceName || domainServiceImplName)
                && isAllowedDomainServiceShape(source, packageName, declaredName)
                && isConcreteDomainServiceSource(source, declaredName)
                && !containsDomainRepositoryReference(declarationScopedSource(source, declaredName))) {
            collectAllowlistedViolation(
                    domainServiceRepositoryKey(declaredTypeName),
                    ArchitectureSourceSupport.repositoryPath(root, path)
                            + " declares concrete "
                            + declaredName
                            + " without a domain Repository dependency",
                    violations,
                    allowlist,
                    matchedAllowances);
        }
    }

    private static boolean isAllowedDomainServiceShape(String source, String packageName, String simpleName) {
        return (packageName.endsWith(".domain.service")
                        && simpleName.endsWith("DomainService")
                        && isInterfaceDomainServiceSource(source, simpleName))
                || (packageName.endsWith(".domain.service.impl")
                        && simpleName.endsWith("DomainServiceImpl")
                        && isConcreteDomainServiceSource(source, simpleName)
                        && implementsExpectedDomainServiceInterface(source, packageName, simpleName));
    }

    private static boolean isDomainServicePackage(String packageName) {
        int servicePackageStart = packageName.indexOf(".domain.service");
        if (servicePackageStart < 0) {
            return false;
        }
        int servicePackageEnd = servicePackageStart + ".domain.service".length();
        return servicePackageEnd == packageName.length() || packageName.charAt(servicePackageEnd) == '.';
    }

    private static boolean isConcreteDomainServiceSource(String source, String simpleName) {
        return Pattern.compile("(?m)^\\s*(?:@[\\w.]+(?:\\([^\\n]*\\))?\\s*)*"
                        + "(?:(?:public|protected|private|static|final)\\s+)*class\\s+"
                        + Pattern.quote(simpleName)
                        + "\\b")
                .matcher(source)
                .find();
    }

    private static boolean isInterfaceDomainServiceSource(String source, String simpleName) {
        return Pattern.compile("(?m)^\\s*(?:@[\\w.]+(?:\\([^\\n]*\\))?\\s*)*"
                        + "(?:(?:public|protected|private|abstract|static|sealed|non-sealed|strictfp)\\s+)*"
                        + "interface\\s+"
                        + Pattern.quote(simpleName)
                        + "\\b")
                .matcher(source)
                .find();
    }

    private static String declarationScopedSource(String source, String simpleName) {
        String scanSource = sourceWithoutLiterals(source);
        Matcher declaration = Pattern.compile(
                        "\\b(?:class|interface|enum|record|@interface)\\s+" + Pattern.quote(simpleName) + "\\b")
                .matcher(scanSource);
        if (!declaration.find()) {
            return source;
        }
        int bodyStart = scanSource.indexOf('{', declaration.end());
        if (bodyStart < 0) {
            return source;
        }
        int bodyEnd = matchingClosingBraceEnd(scanSource, bodyStart);
        StringBuilder scopedSource = new StringBuilder();
        Matcher packageOrImport = Pattern.compile("(?m)^\\s*(?:package\\s+[^;]+;|import\\s+[^;]+;)\\s*")
                .matcher(source);
        while (packageOrImport.find()) {
            scopedSource.append(packageOrImport.group()).append('\n');
        }
        scopedSource.append(source, declaration.start(), bodyEnd);
        return sourceWithoutNestedTypeDeclarations(scopedSource.toString(), simpleName);
    }

    private static String sourceWithoutNestedTypeDeclarations(String source, String rootSimpleName) {
        String scanSource = sourceWithoutLiterals(source);
        Matcher declaration = TYPE_DECLARATION_NAME_PATTERN.matcher(scanSource);
        StringBuilder sourceWithoutNestedTypes = new StringBuilder(source);
        boolean rootDeclarationSeen = false;
        while (declaration.find()) {
            String declaredName = declaration.group(1);
            if (!rootDeclarationSeen && declaredName.equals(rootSimpleName)) {
                rootDeclarationSeen = true;
                continue;
            }
            if (!rootDeclarationSeen) {
                continue;
            }
            int bodyStart = scanSource.indexOf('{', declaration.end());
            if (bodyStart < 0) {
                continue;
            }
            int bodyEnd = matchingClosingBraceEnd(scanSource, bodyStart);
            for (int index = declaration.start(); index < bodyEnd; index++) {
                sourceWithoutNestedTypes.setCharAt(index, ' ');
            }
        }
        return sourceWithoutNestedTypes.toString();
    }

    private static int matchingClosingBraceEnd(String source, int openBraceIndex) {
        int depth = 0;
        for (int index = openBraceIndex; index < source.length(); index++) {
            char current = source.charAt(index);
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return index + 1;
                }
            }
        }
        return source.length();
    }

    private static boolean implementsExpectedDomainServiceInterface(
            String source, String packageName, String simpleName) {
        String expectedInterface = simpleName.substring(0, simpleName.length() - "Impl".length());
        String expectedPackage = packageName.substring(0, packageName.length() - ".impl".length());
        String expectedInterfaceName = expectedPackage + "." + expectedInterface;
        Matcher matcher = Pattern.compile(
                        "\\bclass\\s+" + Pattern.quote(simpleName) + "\\b[^\\{;]*\\bimplements\\s+([^\\{;]+)",
                        Pattern.DOTALL)
                .matcher(source);
        if (!matcher.find()) {
            return false;
        }
        Map<String, String> imports = importedTypes(source);
        Set<String> wildcardImports = wildcardImports(source);
        return topLevelTypeNames(matcher.group(1)).stream()
                .flatMap(typeName -> resolveTypeNames(typeName, packageName, imports, wildcardImports).stream())
                .anyMatch(expectedInterfaceName::equals);
    }

    private static List<String> topLevelTypeNames(String implementsClause) {
        List<String> typeNames = new ArrayList<String>();
        int depth = 0;
        int start = 0;
        for (int index = 0; index < implementsClause.length(); index++) {
            char current = implementsClause.charAt(index);
            if (current == '<') {
                depth++;
            } else if (current == '>') {
                depth = Math.max(0, depth - 1);
            } else if (current == ',' && depth == 0) {
                addTopLevelTypeName(typeNames, implementsClause.substring(start, index));
                start = index + 1;
            }
        }
        addTopLevelTypeName(typeNames, implementsClause.substring(start));
        return typeNames;
    }

    private static void addTopLevelTypeName(List<String> typeNames, String typeName) {
        String rawTypeName = typeName.trim();
        int genericStart = rawTypeName.indexOf('<');
        if (genericStart >= 0) {
            rawTypeName = rawTypeName.substring(0, genericStart).trim();
        }
        if (!rawTypeName.isEmpty()) {
            typeNames.add(rawTypeName);
        }
    }

    private static Map<String, String> importedTypes(String source) {
        Map<String, String> imports = new HashMap<String, String>();
        Matcher matcher = Pattern.compile("(?m)^\\s*import\\s+(?!static\\b)([\\w.]+)\\s*;")
                .matcher(source);
        while (matcher.find()) {
            String importedType = matcher.group(1);
            int packageSeparator = importedType.lastIndexOf('.');
            imports.put(importedType.substring(packageSeparator + 1), importedType);
        }
        return imports;
    }

    private static Set<String> wildcardImports(String source) {
        Set<String> imports = new HashSet<String>();
        Matcher matcher = Pattern.compile("(?m)^\\s*import\\s+(?!static\\b)([\\w.]+)\\.\\*\\s*;")
                .matcher(source);
        while (matcher.find()) {
            imports.add(matcher.group(1));
        }
        return imports;
    }

    private static Set<String> resolveTypeNames(
            String typeName, String packageName, Map<String, String> imports, Set<String> wildcardImports) {
        if (typeName.contains(".")) {
            return Set.of(typeName);
        }
        String importedType = imports.get(typeName);
        if (importedType != null) {
            return Set.of(importedType);
        }
        Set<String> resolvedNames = new HashSet<String>();
        for (String wildcardImport : wildcardImports) {
            resolvedNames.add(wildcardImport + "." + typeName);
        }
        resolvedNames.add(packageName + "." + typeName);
        return resolvedNames;
    }

    private static boolean containsDomainRepositoryReference(String source) {
        String sourceBody = sourceWithoutImportDeclarationsAndLiterals(source);
        Matcher repositoryImport = DOMAIN_REPOSITORY_IMPORT_PATTERN.matcher(source);
        while (repositoryImport.find()) {
            String repositoryName = repositoryImport.group(2);
            if (Pattern.compile("\\b" + Pattern.quote(repositoryName) + "\\b")
                    .matcher(sourceBody)
                    .find()) {
                return true;
            }
        }
        return Pattern.compile(
                        "\\bcom\\.thundax\\.kuzhambu\\.[\\w.]+\\.domain\\.[\\w.]+\\.repository\\.\\w+Repository\\b")
                .matcher(sourceBody)
                .find();
    }

    private static String sourceWithoutImportDeclarationsAndLiterals(String source) {
        String withoutImports =
                Pattern.compile("(?m)^\\s*import\\s+[^;]+;\\s*").matcher(source).replaceAll("\n");
        return sourceWithoutLiterals(withoutImports);
    }

    private static String sourceWithoutLiterals(String source) {
        Matcher matcher = Pattern.compile("(?s)\"\"\".*?\"\"\"|\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'")
                .matcher(source);
        StringBuilder strippedSource = new StringBuilder(source);
        while (matcher.find()) {
            for (int index = matcher.start(); index < matcher.end(); index++) {
                strippedSource.setCharAt(index, ' ');
            }
        }
        return strippedSource.toString();
    }

    public static String domainServiceShapeKey(String typeName) {
        return "DOMAIN_SERVICE_SHAPE:" + typeName;
    }

    public static String domainServiceRepositoryKey(String typeName) {
        return "DOMAIN_SERVICE_REPOSITORY:" + typeName;
    }

    public static void assertRepositoryImplPlacement(JavaClasses classes, String basePackage) {
        assertSuffixPlacement(
                classes,
                basePackage + ".infra",
                ".repository.impl",
                "RepositoryImpl",
                false,
                "*RepositoryImpl classes must be placed under com.thundax.kuzhambu.{module}.infra.{domain}.repository.impl");
    }

    public static void assertPersistenceMapperPlacement(JavaClasses classes, String basePackage) {
        assertSuffixPlacement(
                classes,
                basePackage + ".infra",
                ".persistence.mapper",
                "Mapper",
                null,
                "*Mapper interfaces must be placed under com.thundax.kuzhambu.{module}.infra.{domain}.persistence.mapper");
    }

    public static void assertPersistenceDataObjectPlacement(JavaClasses classes, String basePackage) {
        assertSuffixPlacement(
                classes,
                basePackage + ".infra",
                ".persistence.dataobject",
                "DO",
                false,
                "*DO classes must be placed under com.thundax.kuzhambu.{module}.infra.{domain}.persistence.dataobject");
    }

    public static void assertPersistenceAssemblerPlacement(JavaClasses classes, String basePackage) {
        assertSuffixPlacement(
                classes,
                basePackage + ".infra",
                ".persistence.assembler",
                "PersistenceAssembler",
                false,
                "*PersistenceAssembler classes must be placed under com.thundax.kuzhambu.{module}.infra.{domain}.persistence.assembler");
    }

    public static void assertPersistenceAssemblersDeclareStaticConversionMethods(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!javaClass.getSimpleName().endsWith("PersistenceAssembler")) {
                continue;
            }
            if (!hasPublicStaticMethod(javaClass, "toObject") || !hasPublicStaticMethod(javaClass, "toDomain")) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue(
                "*PersistenceAssembler must declare public static toObject/toDomain conversion methods: " + violations,
                violations.isEmpty());
    }

    public static void assertEntitySourcesDeclareOnlyRequiredAnnotations(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        if (!Files.exists(sourceRoot)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(NamingArchitectureRuleSupport::isEntitySource)
                    .filter(NamingArchitectureRuleSupport::violatesEntityAnnotations)
                    .map(path -> ArchitectureSourceSupport.repositoryPath(root, path))
                    .forEach(violations::add);
        }

        assertTrue(
                "Entity source must declare exactly @Getter, @Setter, @NoArgsConstructor and @AllArgsConstructor "
                        + "as class annotations: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertMapperSourcesDeclareOnlyMapperAnnotation(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        if (!Files.exists(sourceRoot)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(NamingArchitectureRuleSupport::isMapperSource)
                    .filter(NamingArchitectureRuleSupport::violatesMapperAnnotations)
                    .map(path -> ArchitectureSourceSupport.repositoryPath(root, path))
                    .forEach(violations::add);
        }

        assertTrue(
                "*Mapper source must declare exactly @Mapper as class annotation: " + violations, violations.isEmpty());
    }

    public static void assertDataObjectSourcesDeclareOnlyRequiredLombokAnnotations(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        if (!Files.exists(sourceRoot)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(NamingArchitectureRuleSupport::isDataObjectSource)
                    .filter(NamingArchitectureRuleSupport::violatesDataObjectLombokAnnotations)
                    .map(path -> ArchitectureSourceSupport.repositoryPath(root, path))
                    .forEach(violations::add);
        }

        assertTrue(
                "*DO source must declare exactly @Data, @NoArgsConstructor and @AllArgsConstructor as Lombok "
                        + "class annotations: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertConfigurationClassNames(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (isNestedClass(javaClass)) {
                continue;
            }
            boolean configuration = javaClass.isAnnotatedWith(CONFIGURATION_ANNOTATION);
            boolean configurationProperties = javaClass.isAnnotatedWith(CONFIGURATION_PROPERTIES_ANNOTATION);
            if (configuration && configurationProperties) {
                violations.add(javaClass.getName() + " must not declare both @Configuration and "
                        + "@ConfigurationProperties");
            }
            if (configurationProperties && !javaClass.getSimpleName().endsWith("Properties")) {
                violations.add(javaClass.getName() + " must end with Properties");
            }
            if (configuration
                    && (!javaClass.getSimpleName().endsWith("Configuration")
                            || javaClass.getSimpleName().endsWith("AutoConfiguration"))) {
                violations.add(javaClass.getName() + " must end with Configuration and must not end with "
                        + "AutoConfiguration");
            }
        }

        assertTrue(
                "@ConfigurationProperties classes must be *Properties; @Configuration classes must be "
                        + "*Configuration; one class must not declare both annotations: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertRepositoryInterfaceMethodNames(JavaClasses classes) {
        assertRepositoryInterfaceMethodNames(classes, List.of());
    }

    public static void assertRepositoryInterfaceMethodNames(
            JavaClasses classes, Collection<ArchitectureRuleAllowance> legacyAllowances) {
        Map<String, ArchitectureRuleAllowance> allowlist =
                exactRepositoryInterfaceMethodNameAllowlist(legacyAllowances);
        Set<String> matchedAllowances = new HashSet<String>();
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!isRepositoryInterface(javaClass)) {
                continue;
            }
            for (JavaMethod method : javaClass.getMethods()) {
                if (!isRepositoryPortMethodShape(method)) {
                    String methodName = method.getFullName();
                    String allowanceKey = repositoryInterfaceMethodNameAllowanceKey(methodName);
                    if (allowlist.containsKey(allowanceKey)) {
                        matchedAllowances.add(allowanceKey);
                        continue;
                    }
                    violations.add(methodName);
                }
            }
        }

        Set<String> staleAllowances = new HashSet<String>(allowlist.keySet());
        staleAllowances.removeAll(matchedAllowances);

        assertTrue(
                "Repository interface methods should use getByXxx/list/page/count/insert/update/deleteBy/batchXxx "
                        + "naming; content repositories may also use save/exists/open/delete. Violations: "
                        + violations
                        + ". Stale allowances: "
                        + staleAllowances,
                violations.isEmpty() && staleAllowances.isEmpty());
    }

    public static String repositoryInterfaceMethodNameAllowanceKey(String methodFullName) {
        int parameterStart = methodFullName.indexOf('(');
        String repositoryMethod = parameterStart < 0 ? methodFullName : methodFullName.substring(0, parameterStart);
        return "REPOSITORY_INTERFACE_METHOD_NAME:" + repositoryMethod;
    }

    public static List<ArchitectureRuleAllowance> legacyRepositoryInterfaceMethodNameAllowances(
            String... methodFullNames) {
        List<ArchitectureRuleAllowance> allowances = new ArrayList<ArchitectureRuleAllowance>();
        for (String methodFullName : methodFullNames) {
            allowances.add(
                    ArchitectureRuleAllowance.of(
                            repositoryInterfaceMethodNameAllowanceKey(methodFullName),
                            "Repository method retains a legacy verb outside the shared repository naming rule.",
                            "Rename the repository method to the supported verb and update all callers before removing this allowance."));
        }
        return allowances;
    }

    private static Map<String, ArchitectureRuleAllowance> exactRepositoryInterfaceMethodNameAllowlist(
            Collection<ArchitectureRuleAllowance> legacyAllowances) {
        Map<String, ArchitectureRuleAllowance> allowlist = new HashMap<String, ArchitectureRuleAllowance>();
        for (ArchitectureRuleAllowance allowance : legacyAllowances) {
            if (allowance.key().contains("*")) {
                throw new IllegalArgumentException(
                        "Repository interface method-name allowances must use exact repository-method keys: "
                                + allowance.key());
            }
            if (allowlist.put(allowance.key(), allowance) != null) {
                throw new IllegalArgumentException(
                        "Duplicate repository interface method-name allowance: " + allowance.key());
            }
        }
        return allowlist;
    }

    public static void assertRepositoryTypeNamesUseRepositorySuffix(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!isRepositoryPackage(javaClass)
                    || isTestType(javaClass)
                    || javaClass.getName().contains("$")) {
                continue;
            }
            if (javaClass.isInterface() && !javaClass.getSimpleName().endsWith("Repository")) {
                violations.add(javaClass.getName());
            }
            if (!javaClass.isInterface()
                    && javaClass.getPackageName().contains(".repository.impl")
                    && !javaClass.getSimpleName().endsWith("RepositoryImpl")) {
                violations.add(javaClass.getName());
            }
            if (javaClass.getSimpleName().endsWith("DAO")
                    || javaClass.getSimpleName().endsWith("DAOImpl")
                    || javaClass.getSimpleName().endsWith("Dao")
                    || javaClass.getSimpleName().endsWith("DaoImpl")) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue("Repository types must use Repository/RepositoryImpl suffixes: " + violations, violations.isEmpty());
    }

    public static void assertServiceAddMethodsReturnEntityId(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!isServiceInterface(javaClass)) {
                continue;
            }
            for (JavaMethod method : javaClass.getMethods()) {
                if ("add".equals(method.getName())
                        && !"com.thundax.kuzhambu.common.core.id.EntityId"
                                .equals(method.getRawReturnType().getName())) {
                    violations.add(method.getFullName());
                }
            }
        }

        assertTrue("Service add methods must return the created entity id: " + violations, violations.isEmpty());
    }

    public static void assertServiceInterfaceMethodsAreNotOverloaded(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!isServiceInterface(javaClass)) {
                continue;
            }
            Map<String, Integer> methodNameCounts = new HashMap<String, Integer>();
            for (JavaMethod method : javaClass.getMethods()) {
                Integer count = methodNameCounts.get(method.getName());
                methodNameCounts.put(method.getName(), count == null ? 1 : count + 1);
            }
            for (Map.Entry<String, Integer> entry : methodNameCounts.entrySet()) {
                if (entry.getValue() > 1) {
                    violations.add(javaClass.getName() + "#" + entry.getKey());
                }
            }
        }

        assertTrue(
                "Service interface methods must not be overloaded; express batch/by-condition/by-id/cascade "
                        + "semantics in the method name: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertUserServiceDoesNotExposeIdentityOrCredentialMethods(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();
        Set<String> prohibitedMethods = new LinkedHashSet<String>(
                Arrays.asList("getByLoginName", "getAccountLoginName", "getPasswordCredential", "updatePassword"));

        for (JavaClass javaClass : classes) {
            if (!isServiceInterface(javaClass) || !"UserService".equals(javaClass.getSimpleName())) {
                continue;
            }
            for (JavaMethod method : javaClass.getMethods()) {
                if (prohibitedMethods.contains(method.getName())) {
                    violations.add(method.getFullName());
                }
            }
        }

        assertTrue(
                "UserService must keep user principal and role boundaries; identity and credential methods belong "
                        + "to UserIdentityService/UserCredentialService: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertServiceQueryObjectsUnderServiceQueryPackage(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (isServiceQueryObject(javaClass) && !isInServiceQueryPackage(javaClass)) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue(
                "Service query objects must be placed under "
                        + "com.thundax.kuzhambu.{module}.application.query: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertServiceQueryObjectsDeclareNoSetters(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(NamingArchitectureRuleSupport::isServiceQuerySource)
                    .filter(NamingArchitectureRuleSupport::containsServiceQuerySetter)
                    .map(path -> ArchitectureSourceSupport.repositoryPath(root, path))
                    .forEach(violations::add);
        }

        assertTrue(
                "Service query objects must only define query fields; request-to-query conversion belongs in "
                        + "InterfaceAssembler, so service query source must not declare setXxx methods: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertServiceQueryObjectsDeclareOnlyRequiredAnnotations(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(NamingArchitectureRuleSupport::isServiceQuerySource)
                    .filter(NamingArchitectureRuleSupport::violatesServiceQueryAnnotations)
                    .map(path -> ArchitectureSourceSupport.repositoryPath(root, path))
                    .forEach(violations::add);
        }

        assertTrue(
                "Service query objects must declare exactly @Getter, @Setter, @NoArgsConstructor, "
                        + "@AllArgsConstructor as class annotations: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertInterfaceAssemblerPublicMethodsStatic(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!javaClass.getSimpleName().endsWith("InterfaceAssembler")) {
                continue;
            }
            for (JavaMethod method : javaClass.getMethods()) {
                if (method.getModifiers().contains(JavaModifier.PUBLIC)
                        && !method.getModifiers().contains(JavaModifier.STATIC)) {
                    violations.add(method.getFullName());
                }
            }
        }

        assertTrue("InterfaceAssembler public methods must be static: " + violations, violations.isEmpty());
    }

    public static void assertInterfaceAssemblersDoNotWrapEntityIdConversion(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!javaClass.getSimpleName().endsWith("InterfaceAssembler")) {
                continue;
            }
            for (JavaMethod method : javaClass.getMethods()) {
                if (method.getModifiers().contains(JavaModifier.PUBLIC) && "toEntityId".equals(method.getName())) {
                    violations.add(method.getFullName());
                }
            }
        }

        assertTrue("InterfaceAssembler must not wrap EntityId conversion: " + violations, violations.isEmpty());
    }

    private static List<MethodParameter> parseMethodParameters(String parameters, String source) {
        List<MethodParameter> result = new ArrayList<MethodParameter>();
        for (String parameter : splitTopLevel(parameters)) {
            MethodParameter parsed = parseMethodParameter(parameter, source);
            if (parsed != null) {
                result.add(parsed);
            }
        }
        return result;
    }

    private static List<String> splitTopLevel(String value) {
        List<String> parts = new ArrayList<String>();
        int depth = 0;
        int start = 0;
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (current == '<') {
                depth++;
            } else if (current == '>') {
                depth--;
            } else if (current == ',' && depth == 0) {
                parts.add(value.substring(start, index).trim());
                start = index + 1;
            }
        }
        String tail = value.substring(start).trim();
        if (!tail.isEmpty()) {
            parts.add(tail);
        }
        return parts;
    }

    private static MethodParameter parseMethodParameter(String parameter, String source) {
        String normalized = parameter.replace('\n', ' ').replaceAll("\\s+", " ").trim();
        if (normalized.isEmpty()) {
            return null;
        }
        boolean nonNullAnnotated = hasNonNullAnnotation(normalized, source);
        normalized = normalized.replaceAll("@[\\w.]+(?:\\([^)]*\\))?\\s*", "");
        normalized = normalized.replaceAll("\\bfinal\\s+", "");
        int separator = normalized.lastIndexOf(' ');
        if (separator < 0 || separator == normalized.length() - 1) {
            return null;
        }
        String type = compactType(normalized.substring(0, separator).replace("...", "[]"));
        String name = normalized.substring(separator + 1).trim();
        return new MethodParameter(type, name, nonNullAnnotated);
    }

    private static String methodHeader(String declaration) {
        int parameterStart = declaration.indexOf('(');
        return parameterStart < 0 ? declaration : declaration.substring(0, parameterStart);
    }

    private static boolean hasNonNullAnnotation(String value, String source) {
        if (Pattern.compile("@org\\.springframework\\.lang\\.NonNull\\b")
                .matcher(value)
                .find()) {
            return true;
        }
        return Pattern.compile("@NonNull\\b").matcher(value).find() && importsSpringNonNull(source);
    }

    private static boolean importsSpringNonNull(String source) {
        return Pattern.compile("(?m)^\\s*import\\s+org\\.springframework\\.lang\\.NonNull\\s*;")
                .matcher(source)
                .find();
    }

    private static boolean containsRequireNonNullGuard(String body, String parameterName) {
        return Pattern.compile("\\b(?:java\\.util\\.)?Objects\\.requireNonNull\\s*\\(\\s*"
                        + Pattern.quote(parameterName)
                        + "\\b")
                .matcher(body)
                .find();
    }

    private static String compactType(String value) {
        return value.replaceAll("\\s+", "");
    }

    private static boolean isBoundaryAssemblerSource(Path path) {
        String value = ArchitectureSourceSupport.normalizePath(path);
        String fileName = path.getFileName().toString();
        if (!fileName.endsWith("Assembler.java")) {
            return false;
        }
        if (value.contains("/interfaces/")
                && value.contains("/assembler/")
                && fileName.endsWith("InterfaceAssembler.java")) {
            return true;
        }
        if (value.contains("/application/")
                && value.contains("/facade/assembler/")
                && fileName.endsWith("FacadeAssembler.java")) {
            return true;
        }
        return value.contains("/application/") && value.contains("/assembler/");
    }

    private record MethodParameter(String type, String name, boolean nonNullAnnotated) {

        boolean referenceType() {
            return !PRIMITIVE_TYPES.contains(type);
        }
    }

    private static boolean isToolPackage(JavaClass javaClass) {
        String packageName = javaClass.getPackageName();
        return packageName.contains(".utils")
                || packageName.contains(".collection")
                || packageName.contains(".web.request")
                || packageName.contains(".web.response");
    }

    private static boolean isNestedClass(JavaClass javaClass) {
        return javaClass.getName().contains("$");
    }

    private static void collectMethodDeclarationViolations(Path root, Path path, List<String> violations) {
        Matcher matcher = METHOD_DECLARATION_PATTERN.matcher(ArchitectureSourceSupport.readSourceWithoutComments(path));
        String className = sourceClassName(path);
        while (matcher.find()) {
            String methodName = matcher.group(1);
            if (methodName.equals(className)) {
                continue;
            }
            violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + "#" + methodName);
        }
    }

    private static String sourceClassName(Path path) {
        String fileName = path.getFileName().toString();
        int extensionIndex = fileName.lastIndexOf('.');
        if (extensionIndex < 0) {
            return fileName;
        }
        return fileName.substring(0, extensionIndex);
    }

    private static void collectLayerTypeNameViolation(JavaClass javaClass, List<String> violations) {
        String packageName = javaClass.getPackageName();
        String simpleName = javaClass.getSimpleName();
        if (isDirectControllerPackage(packageName) && !simpleName.endsWith("Controller")) {
            violations.add(javaClass.getName());
        } else if (packageName.contains(".controller.request") && !simpleName.endsWith("Request")) {
            violations.add(javaClass.getName());
        } else if (packageName.contains(".controller.response") && !simpleName.endsWith("Response")) {
            violations.add(javaClass.getName());
        } else if (isServiceImplementation(javaClass) && !simpleName.endsWith("ServiceImpl")) {
            violations.add(javaClass.getName());
        } else if (isServiceInterfacePackage(javaClass) && !simpleName.endsWith("Service")) {
            violations.add(javaClass.getName());
        } else if (isRepositoryInterfacePackage(javaClass) && !simpleName.endsWith("Repository")) {
            violations.add(javaClass.getName());
        } else if (isRepositoryImplementation(javaClass) && !simpleName.endsWith("RepositoryImpl")) {
            violations.add(javaClass.getName());
        } else if (packageName.contains(".persistence.mapper") && !simpleName.endsWith("Mapper")) {
            violations.add(javaClass.getName());
        } else if (packageName.contains(".persistence.dataobject")
                && !simpleName.endsWith("DO")
                && !simpleName.endsWith("DataObject")) {
            violations.add(javaClass.getName());
        } else if (packageName.contains(".persistence.assembler") && !simpleName.endsWith("PersistenceAssembler")) {
            violations.add(javaClass.getName());
        } else if (isInterfaceAssemblerPackage(packageName) && !simpleName.endsWith("InterfaceAssembler")) {
            violations.add(javaClass.getName());
        } else if (packageName.contains(".application.query") && !simpleName.endsWith("Query")) {
            violations.add(javaClass.getName());
        }
    }

    private static boolean isDirectControllerPackage(String packageName) {
        return packageName.endsWith(".controller");
    }

    private static boolean isServiceImplementation(JavaClass javaClass) {
        return !javaClass.isInterface() && javaClass.getPackageName().contains(".service.impl");
    }

    private static boolean isServiceInterfacePackage(JavaClass javaClass) {
        String packageName = javaClass.getPackageName();
        return javaClass.isInterface() && packageName.endsWith(".service");
    }

    private static boolean isRepositoryInterfacePackage(JavaClass javaClass) {
        return javaClass.isInterface() && javaClass.getPackageName().contains(".repository");
    }

    private static boolean isRepositoryImplementation(JavaClass javaClass) {
        return !javaClass.isInterface() && javaClass.getPackageName().contains(".repository.impl");
    }

    private static boolean isInterfaceAssemblerPackage(String packageName) {
        return packageName.contains(".assembler") && !packageName.contains(".persistence.assembler");
    }

    private static boolean isRepositoryInterface(JavaClass javaClass) {
        return javaClass.isInterface()
                && javaClass.getSimpleName().endsWith("Repository")
                && javaClass.getPackageName().contains(".repository");
    }

    private static boolean isRepositoryPackage(JavaClass javaClass) {
        return javaClass.getPackageName().contains(".repository");
    }

    private static boolean isTestType(JavaClass javaClass) {
        return javaClass.getName().contains("Test");
    }

    private static boolean isPackageUnder(JavaClass javaClass, String packagePrefix) {
        return javaClass.getPackageName().equals(packagePrefix)
                || javaClass.getPackageName().startsWith(packagePrefix + ".");
    }

    private static boolean extendsBaseId(JavaClass javaClass) {
        Optional<JavaClass> superclass = javaClass.getRawSuperclass();
        if (!superclass.isPresent()) {
            return false;
        }
        JavaClass rawSuperclass = superclass.get();
        return rawSuperclass.getPackageName().equals("com.thundax.kuzhambu.common.core.id")
                && rawSuperclass.getSimpleName().startsWith("Base")
                && rawSuperclass.getSimpleName().endsWith("Id");
    }

    private static boolean isServiceInterface(JavaClass javaClass) {
        return javaClass.isInterface()
                && javaClass.getSimpleName().endsWith("Service")
                && javaClass.getPackageName().contains(".service");
    }

    private static boolean isRepositoryPortMethodShape(JavaMethod method) {
        String name = method.getName();
        if (isNonStandardIdsListName(name) || name.startsWith("find")) {
            return false;
        }
        return name.equals("count")
                || name.equals("list")
                || name.equals("page")
                || name.equals("save")
                || name.equals("exists")
                || name.equals("open")
                || name.equals("delete")
                || name.equals("deleteAll")
                || name.startsWith("getBy")
                || name.startsWith("max")
                || name.startsWith("list")
                || name.startsWith("count")
                || name.startsWith("insert")
                || name.startsWith("update")
                || name.startsWith("deleteBy")
                || name.startsWith("batch")
                || isRepositoryBusinessActionName(name);
    }

    private static boolean isNonStandardIdsListName(String name) {
        return name.endsWith("ByIds") && !name.equals("listByIds");
    }

    private static boolean isRepositoryBusinessActionName(String name) {
        return name.equals("active")
                || name.equals("canSend")
                || name.equals("deleteBusiness")
                || name.equals("deleteBusinessByBusiness")
                || name.equals("deleteMenuRole")
                || name.equals("deleteRoleMenu")
                || name.equals("deleteRoleUser")
                || name.equals("deleteUserRole")
                || name.equals("getContentById")
                || name.equals("getDictionaryRevision")
                || name.equals("getMultipartPart")
                || name.equals("getMultipartSessionByUploadId")
                || name.equals("getUidByToken")
                || name.equals("isChildOf")
                || name.equals("markSent")
                || name.equals("moveTreeNode")
                || name.equals("tokenExists")
                || name.equals("touch");
    }

    private static boolean isServiceQueryObject(JavaClass javaClass) {
        String simpleName = javaClass.getSimpleName();
        return simpleName.endsWith("Query") && !"Query".equals(simpleName);
    }

    private static boolean isInServiceQueryPackage(JavaClass javaClass) {
        return javaClass.getPackageName().contains(".application.")
                && javaClass.getPackageName().endsWith(".query");
    }

    private static boolean isServiceQuerySource(Path path) {
        String value = ArchitectureSourceSupport.normalizePath(path);
        return value.contains("/application/") && value.contains("/query/") && value.endsWith("Query.java");
    }

    private static boolean isApplicationCommandOrQuerySource(Path path) {
        String value = ArchitectureSourceSupport.normalizePath(path);
        return value.contains("/application/") && (value.endsWith("Command.java") || value.endsWith("Query.java"));
    }

    private static boolean isApplicationContractSource(Path path) {
        String value = ArchitectureSourceSupport.normalizePath(path);
        return value.endsWith("Command.java") || value.endsWith("Query.java") || value.endsWith("Result.java");
    }

    private static boolean isApplicationContractType(JavaClass javaClass) {
        String simpleName = javaClass.getSimpleName();
        return simpleName.endsWith("Command") || simpleName.endsWith("Query") || simpleName.endsWith("Result");
    }

    private static boolean isApplicationContractUnderDedicatedPackage(JavaClass javaClass) {
        String simpleName = javaClass.getSimpleName();
        String responsibilityPackage =
                simpleName.endsWith("Command") ? "command" : simpleName.endsWith("Query") ? "query" : "result";
        String[] segments = javaClass.getPackageName().split("\\.");
        int applicationIndex = Arrays.asList(segments).indexOf("application");
        if (applicationIndex < 0 || !responsibilityPackage.equals(segments[segments.length - 1])) {
            return false;
        }
        for (int index = applicationIndex + 1; index < segments.length - 1; index++) {
            if (APPLICATION_STRUCTURAL_PACKAGES.contains(segments[index])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isApplicationModuleSourceRoot(Path sourceRoot) {
        return Stream.iterate(sourceRoot.toAbsolutePath(), path -> path != null, Path::getParent)
                .map(Path::getFileName)
                .filter(fileName -> fileName != null)
                .map(Path::toString)
                .anyMatch(name -> name.endsWith("-application"));
    }

    private static boolean isApplicationContractUnderDedicatedPackage(Path path) {
        String value = ArchitectureSourceSupport.normalizePath(path);
        String suffix;
        String packageName;
        if (value.endsWith("Command.java")) {
            suffix = "Command.java";
            packageName = "command";
        } else if (value.endsWith("Query.java")) {
            suffix = "Query.java";
            packageName = "query";
        } else {
            suffix = "Result.java";
            packageName = "result";
        }
        String applicationMarker = "/application/";
        int applicationIndex = value.indexOf(applicationMarker);
        if (applicationIndex < 0 || !value.endsWith(suffix)) {
            return false;
        }

        String relativePath = value.substring(applicationIndex + applicationMarker.length());
        String[] segments = relativePath.split("/");
        if (segments.length < 2 || !packageName.equals(segments[segments.length - 2])) {
            return false;
        }
        for (int index = 0; index < segments.length - 2; index++) {
            if (APPLICATION_STRUCTURAL_PACKAGES.contains(segments[index])) {
                return false;
            }
        }
        return declaresPackageMatchingSourcePath(path);
    }

    private static boolean declaresPackageMatchingSourcePath(Path path) {
        String value = ArchitectureSourceSupport.normalizePath(path);
        String sourceMarker = "src/main/java/";
        int sourceIndex = value.indexOf(sourceMarker);
        if (sourceIndex < 0) {
            return false;
        }
        String relativePath = value.substring(sourceIndex + sourceMarker.length());
        int fileNameIndex = relativePath.lastIndexOf('/');
        if (fileNameIndex < 0) {
            return false;
        }
        String expectedPackage = relativePath.substring(0, fileNameIndex).replace('/', '.');
        Matcher matcher =
                PACKAGE_DECLARATION_PATTERN.matcher(ArchitectureSourceSupport.readSourceWithoutComments(path));
        return matcher.find() && expectedPackage.equals(matcher.group(1));
    }

    private static boolean isValueObjectIdSource(Path path) {
        String value = ArchitectureSourceSupport.normalizePath(path);
        return value.contains("/valueobject/") && value.endsWith("Id.java");
    }

    private static boolean containsStaticMethodDeclaration(Path path) {
        return STATIC_METHOD_DECLARATION_PATTERN
                .matcher(ArchitectureSourceSupport.readSourceWithoutComments(path))
                .find();
    }

    private static boolean containsServiceQuerySetter(Path path) {
        return SERVICE_QUERY_SETTER_DECLARATION_PATTERN
                .matcher(ArchitectureSourceSupport.readSourceWithoutComments(path))
                .find();
    }

    private static boolean violatesServiceQueryAnnotations(Path path) {
        String source = ArchitectureSourceSupport.readSourceWithoutComments(path);
        Matcher classDeclaration = SERVICE_QUERY_CLASS_DECLARATION_PATTERN.matcher(source);
        if (!classDeclaration.find()) {
            return true;
        }
        return !SERVICE_QUERY_REQUIRED_ANNOTATIONS.equals(classAnnotationSimpleNames(classDeclaration.group(1)));
    }

    private static boolean isEntitySource(Path path) {
        String value = ArchitectureSourceSupport.normalizePath(path);
        return value.contains("/domain/") && value.contains("/model/entity/") && value.endsWith(".java");
    }

    private static boolean violatesEntityAnnotations(Path path) {
        String source = ArchitectureSourceSupport.readSourceWithoutComments(path);
        Matcher classDeclaration = ENTITY_CLASS_DECLARATION_PATTERN.matcher(source);
        if (!classDeclaration.find()) {
            return true;
        }
        return !ENTITY_REQUIRED_ANNOTATIONS.equals(classAnnotationSimpleNames(classDeclaration.group(1)));
    }

    static Set<String> sourceClassAnnotationSimpleNames(Path path) {
        String source = ArchitectureSourceSupport.readSourceWithoutComments(path);
        Matcher classDeclaration = ENTITY_CLASS_DECLARATION_PATTERN.matcher(source);
        if (!classDeclaration.find()) {
            return new LinkedHashSet<String>();
        }
        return classAnnotationSimpleNames(classDeclaration.group(1));
    }

    private static Set<String> classAnnotationSimpleNames(String sourceBeforeClass) {
        Matcher annotation = SOURCE_ANNOTATION_PATTERN.matcher(sourceBeforeClass);
        Set<String> annotations = new LinkedHashSet<String>();
        while (annotation.find()) {
            annotations.add(annotation.group(1));
        }
        return annotations;
    }

    private static boolean matchesModuleSubdomainPackage(String packageName, String prefix, String suffix) {
        String expectedPrefix = prefix + ".";
        if (!packageName.startsWith(expectedPrefix) || !packageName.endsWith(suffix)) {
            return false;
        }
        String middle = packageName.substring(expectedPrefix.length(), packageName.length() - suffix.length());
        return middle.matches("[a-z][a-z0-9]*");
    }

    private static void assertSuffixPlacement(
            JavaClasses classes,
            String prefix,
            String suffix,
            String typeSuffix,
            Boolean interfaceType,
            String message) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (isTestType(javaClass) || javaClass.getName().contains("$")) {
                continue;
            }
            if (!javaClass.getSimpleName().endsWith(typeSuffix)) {
                continue;
            }
            if (interfaceType != null && javaClass.isInterface() != interfaceType.booleanValue()) {
                violations.add(javaClass.getName());
                continue;
            }
            if (!matchesModuleSubdomainPackage(javaClass.getPackageName(), prefix, suffix)) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue(message + ": " + violations, violations.isEmpty());
    }

    private static boolean hasPublicStaticMethod(JavaClass javaClass, String methodName) {
        for (JavaMethod method : javaClass.getMethods()) {
            if (methodName.equals(method.getName())
                    && method.getModifiers().contains(JavaModifier.PUBLIC)
                    && method.getModifiers().contains(JavaModifier.STATIC)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMapperSource(Path path) {
        String value = ArchitectureSourceSupport.normalizePath(path);
        return value.contains("/infra/") && value.contains("/persistence/mapper/") && value.endsWith("Mapper.java");
    }

    private static boolean violatesMapperAnnotations(Path path) {
        String source = ArchitectureSourceSupport.readSourceWithoutComments(path);
        Matcher interfaceDeclaration = INTERFACE_DECLARATION_PATTERN.matcher(source);
        if (!interfaceDeclaration.find()) {
            return true;
        }
        return !MAPPER_REQUIRED_ANNOTATIONS.equals(classAnnotationSimpleNames(interfaceDeclaration.group(1)));
    }

    private static boolean isDataObjectSource(Path path) {
        String value = ArchitectureSourceSupport.normalizePath(path);
        return value.contains("/infra/") && value.contains("/persistence/dataobject/") && value.endsWith("DO.java");
    }

    private static boolean violatesDataObjectLombokAnnotations(Path path) {
        String source = ArchitectureSourceSupport.readSourceWithoutComments(path);
        Matcher classDeclaration = ENTITY_CLASS_DECLARATION_PATTERN.matcher(source);
        if (!classDeclaration.find()) {
            return true;
        }
        Set<String> lombokAnnotations = new LinkedHashSet<String>();
        for (String annotation : classAnnotationSimpleNames(classDeclaration.group(1))) {
            if (DATA_OBJECT_LOMBOK_ANNOTATIONS.contains(annotation)) {
                lombokAnnotations.add(annotation);
            }
        }
        return !DATA_OBJECT_REQUIRED_LOMBOK_ANNOTATIONS.equals(lombokAnnotations);
    }
}
