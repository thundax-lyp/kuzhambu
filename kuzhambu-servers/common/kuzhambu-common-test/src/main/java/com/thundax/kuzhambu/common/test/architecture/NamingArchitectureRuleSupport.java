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
    private static final Pattern SERVICE_QUERY_CLASS_DECLARATION_PATTERN =
            Pattern.compile("(?s)(.*?)\\bpublic\\s+class\\s+\\w+Query\\b");
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

    public static void assertDaoInterfaceMethodNames(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!isDaoInterface(javaClass)) {
                continue;
            }
            for (JavaMethod method : javaClass.getMethods()) {
                if (!isDaoPortMethodShape(method)) {
                    violations.add(method.getFullName());
                }
            }
        }

        assertTrue(
                "DAO interface methods should use getById/getByXxx/list/listByIds/page/count/deleteById/batchXxx "
                        + "naming: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertDaoTypeNamesUseDaoSuffix(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!isDaoPackage(javaClass)
                    || isTestType(javaClass)
                    || javaClass.getName().contains("$")) {
                continue;
            }
            if (javaClass.isInterface() && !javaClass.getSimpleName().endsWith("Dao")) {
                violations.add(javaClass.getName());
            }
            if (!javaClass.isInterface()
                    && javaClass.getPackageName().contains(".persistence.dao")
                    && !javaClass.getSimpleName().endsWith("DaoImpl")) {
                violations.add(javaClass.getName());
            }
            if (javaClass.getSimpleName().endsWith("DAO")
                    || javaClass.getSimpleName().endsWith("DAOImpl")) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue("DAO types must use Dao/DaoImpl suffixes: " + violations, violations.isEmpty());
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
        } else if (isDaoInterfacePackage(javaClass) && !simpleName.endsWith("Dao")) {
            violations.add(javaClass.getName());
        } else if (isDaoImplementation(javaClass) && !simpleName.endsWith("DaoImpl")) {
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

    private static boolean isDaoInterfacePackage(JavaClass javaClass) {
        return javaClass.isInterface() && javaClass.getPackageName().contains(".dao");
    }

    private static boolean isDaoImplementation(JavaClass javaClass) {
        return !javaClass.isInterface() && javaClass.getPackageName().contains(".persistence.dao");
    }

    private static boolean isInterfaceAssemblerPackage(String packageName) {
        return packageName.contains(".assembler") && !packageName.contains(".persistence.assembler");
    }

    private static boolean isDaoInterface(JavaClass javaClass) {
        return javaClass.isInterface()
                && javaClass.getSimpleName().endsWith("Dao")
                && javaClass.getPackageName().contains(".dao");
    }

    private static boolean isDaoPackage(JavaClass javaClass) {
        return javaClass.getPackageName().contains(".dao");
    }

    private static boolean isTestType(JavaClass javaClass) {
        return javaClass.getName().contains("Test");
    }

    private static boolean isPackageUnder(JavaClass javaClass, String packagePrefix) {
        return javaClass.getPackageName().equals(packagePrefix)
                || javaClass.getPackageName().startsWith(packagePrefix + ".");
    }

    private static boolean isServiceInterface(JavaClass javaClass) {
        return javaClass.isInterface()
                && javaClass.getSimpleName().endsWith("Service")
                && javaClass.getPackageName().contains(".service");
    }

    private static boolean isDaoPortMethodShape(JavaMethod method) {
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
                || isDaoBusinessActionName(name);
    }

    private static boolean isNonStandardIdsListName(String name) {
        return name.endsWith("ByIds") && !name.equals("listByIds");
    }

    private static boolean isDaoBusinessActionName(String name) {
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
        return javaClass.getPackageName().contains(".application.query");
    }

    private static boolean isServiceQuerySource(Path path) {
        String value = ArchitectureSourceSupport.normalizePath(path);
        return value.contains("/biz/") && value.contains("/application/query/") && value.endsWith("Query.java");
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
        Matcher annotation = SOURCE_ANNOTATION_PATTERN.matcher(classDeclaration.group(1));
        Set<String> annotations = new LinkedHashSet<String>();
        while (annotation.find()) {
            annotations.add(annotation.group(1));
        }
        return !SERVICE_QUERY_REQUIRED_ANNOTATIONS.equals(annotations);
    }
}
