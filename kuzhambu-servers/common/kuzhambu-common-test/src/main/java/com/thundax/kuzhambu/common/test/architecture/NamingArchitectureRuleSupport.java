package com.thundax.kuzhambu.common.test.architecture;

import static com.thundax.kuzhambu.common.test.architecture.ArchitectureAssertions.assertTrue;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
    private static final Set<String> SERVICE_QUERY_REQUIRED_ANNOTATIONS =
            new LinkedHashSet<String>(Arrays.asList("Getter", "Setter", "NoArgsConstructor", "AllArgsConstructor"));
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

    public static void assertRepositoryPlacement(JavaClasses classes, String basePackage) {
        assertSuffixPlacement(
                classes,
                basePackage + ".domain",
                ".repository",
                "Repository",
                true,
                "*Repository interfaces must be placed under com.thundax.kuzhambu.{module}.domain.{domain}.repository");
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
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!isRepositoryInterface(javaClass)) {
                continue;
            }
            for (JavaMethod method : javaClass.getMethods()) {
                if (!isRepositoryPortMethodShape(method)) {
                    violations.add(method.getFullName());
                }
            }
        }

        assertTrue(
                "Repository interface methods should use getById/getByXxx/list/listByIds/page/count/deleteById/batchXxx "
                        + "naming: "
                        + violations,
                violations.isEmpty());
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
        return value.contains("/biz/")
                && value.contains("/application/")
                && value.contains("/query/")
                && value.endsWith("Query.java");
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
