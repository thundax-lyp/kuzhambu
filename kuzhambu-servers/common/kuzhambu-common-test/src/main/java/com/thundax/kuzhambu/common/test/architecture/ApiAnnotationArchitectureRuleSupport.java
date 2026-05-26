package com.thundax.kuzhambu.common.test.architecture;

import static com.thundax.kuzhambu.common.test.architecture.ArchitectureAssertions.assertTrue;

import com.tngtech.archunit.lang.ArchRule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class ApiAnnotationArchitectureRuleSupport {

    private static final String[] METHOD_MAPPING_ANNOTATIONS = {
        "@GetMapping", "@PostMapping", "@PutMapping", "@DeleteMapping", "@PatchMapping"
    };
    private static final String[] REST_CONTROLLER_ANNOTATIONS = {"@RestController", "@WrappedApiController"};

    private static final Pattern REST_CONTROLLER_CLASS_PATTERN = Pattern.compile(
            "((?:@[A-Za-z0-9_.]+(?:\\([^)]*\\))?\\s+)*)public\\s+class\\s+([A-Za-z0-9_]+Controller)\\b");
    private static final Pattern PUBLIC_METHOD_DECLARATION_PATTERN =
            Pattern.compile("public\\s+[^{;]+\\s+([A-Za-z0-9_]+)\\s*\\(");
    private static final Pattern API_TAGS_PATTERN = Pattern.compile("\\s*\\([^)]*tags\\s*=\\s*\"([^\"]+)\"");
    private static final Pattern API_TAG_NUMERIC_PREFIX_PATTERN = Pattern.compile("^\\d+(?:-\\d+)*\\.\\s*");
    private static final Pattern REQUEST_MAPPING_VALUE_PATTERN =
            Pattern.compile("@RequestMapping\\s*\\(\\s*(?:value\\s*=\\s*)?\"([^\"]+)\"");
    private static final Pattern RESPONSE_CONSTRUCTOR_PATTERN =
            Pattern.compile("new\\s+([A-Za-z0-9_]+Response)\\s*\\(");

    private ApiAnnotationArchitectureRuleSupport() {}

    public static ArchRule requestClassAnnotationsRequired(String basePackage) {
        return ModelAnnotationArchitectureRuleSupport.requestClassAnnotationsRequired(basePackage);
    }

    public static ArchRule responseClassAnnotationsRequired(String basePackage) {
        return ModelAnnotationArchitectureRuleSupport.responseClassAnnotationsRequired(basePackage);
    }

    public static void assertApiOperationDeclaresAccessAnnotation(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectAccessAnnotationViolations(root, path, violations));
        }

        assertTrue("API methods must declare @HasPermission or @PublicApi: " + violations, violations.isEmpty());
    }

    public static void assertRestControllersDeclareRequestMapping(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectRequestMappingViolations(root, path, violations));
        }

        assertTrue("REST controllers must declare class-level @RequestMapping: " + violations, violations.isEmpty());
    }

    public static void assertRestControllerRequestMappingsUseApiResourcePath(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectRequestMappingPathViolations(root, path, violations));
        }

        assertTrue(
                "REST controller request mappings must use /api/{domain}/{resource}: " + violations,
                violations.isEmpty());
    }

    public static void assertRestControllersDeclareApi(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectApiClassViolations(root, path, violations));
        }

        assertTrue("REST controllers must declare : " + violations, violations.isEmpty());
    }

    public static void assertApiTagsDoNotUseNumericPrefix(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectApiTagNumericPrefixViolations(root, path, violations));
        }

        assertTrue("API tags must not use numeric prefixes: " + violations, violations.isEmpty());
    }

    public static void assertMappedMethodsDeclareApiOperation(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectApiOperationViolations(root, path, violations));
        }

        assertTrue("Mapped controller methods must declare : " + violations, violations.isEmpty());
    }

    public static void assertMappedMethodsDeclareSingleHttpMapping(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectSingleHttpMappingViolations(root, path, violations));
        }

        assertTrue(
                "Mapped controller methods must declare exactly one HTTP mapping: " + violations, violations.isEmpty());
    }

    public static void assertMappedMethodsUsePostOrGetMapping(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectPostOrGetMappingViolations(root, path, violations));
        }

        assertTrue(
                "Mapped controller methods must use @PostMapping or @GetMapping only: " + violations,
                violations.isEmpty());
    }

    public static void assertJsonRequestMethodsUsePostMapping(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectJsonPostMappingViolations(root, path, violations));
        }

        assertTrue("JSON request methods must use @PostMapping: " + violations, violations.isEmpty());
    }

    public static void assertGetMappingMethodsReturnVoid(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectGetMappingReturnViolations(root, path, violations));
        }

        assertTrue("GET mapping methods must be non-JSON void responses: " + violations, violations.isEmpty());
    }

    public static void assertRequestBodyRequestParametersDeclareValid(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectRequestBodyValidViolations(root, path, violations));
        }

        assertTrue("RequestBody request parameters must declare @Valid: " + violations, violations.isEmpty());
    }

    public static void assertControllersDoNotCreateResponses(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectResponseConstructorViolations(root, path, violations));
        }

        assertTrue(
                "Controllers must create *Response through *InterfaceAssembler: " + violations, violations.isEmpty());
    }

    private static void collectAccessAnnotationViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSource(path);
        String classAnnotations = restControllerClassAnnotations(content);
        if (classAnnotations.length() == 0) {
            return;
        }
        boolean accessAnnotatedClass =
                classAnnotations.contains("@PublicApi") || classAnnotations.contains("@HasPermission");
        Matcher matcher = PUBLIC_METHOD_DECLARATION_PATTERN.matcher(content);
        int previousMethodEnd = restControllerClassEnd(content);
        while (matcher.find()) {
            String annotations = content.substring(previousMethodEnd, matcher.start());
            String methodName = matcher.group(1);
            if (annotations.contains("")
                    && !accessAnnotatedClass
                    && !annotations.contains("@PublicApi")
                    && !annotations.contains("@HasPermission")) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " method=" + methodName);
            }
            previousMethodEnd = matcher.end();
        }
    }

    private static void collectRequestMappingViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSource(path);
        Matcher matcher = REST_CONTROLLER_CLASS_PATTERN.matcher(content);
        while (matcher.find()) {
            String annotations = matcher.group(1);
            String className = matcher.group(2);
            if (containsRestControllerAnnotation(annotations) && !annotations.contains("@RequestMapping")) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " class=" + className);
            }
        }
    }

    private static void collectApiClassViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSource(path);
        Matcher matcher = REST_CONTROLLER_CLASS_PATTERN.matcher(content);
        while (matcher.find()) {
            String annotations = matcher.group(1);
            String className = matcher.group(2);
            if (containsRestControllerAnnotation(annotations) && !annotations.contains("")) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " class=" + className);
            }
        }
    }

    private static void collectRequestMappingPathViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSource(path);
        String annotations = restControllerClassAnnotations(content);
        if (annotations.length() == 0) {
            return;
        }
        Matcher matcher = REQUEST_MAPPING_VALUE_PATTERN.matcher(annotations);
        String mapping = matcher.find() ? matcher.group(1) : "<missing>";
        if (!isApiResourcePath(mapping)) {
            String className = path.getFileName().toString().replace(".java", "");
            violations.add(
                    ArchitectureSourceSupport.repositoryPath(root, path) + " class=" + className + " path=" + mapping);
        }
    }

    private static void collectApiTagNumericPrefixViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSource(path);
        String classAnnotations = restControllerClassAnnotations(content);
        if (classAnnotations.length() == 0) {
            return;
        }
        Matcher matcher = API_TAGS_PATTERN.matcher(classAnnotations);
        while (matcher.find()) {
            String tag = matcher.group(1);
            if (API_TAG_NUMERIC_PREFIX_PATTERN.matcher(tag).find()) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " tag=" + tag);
            }
        }
    }

    private static void collectApiOperationViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSource(path);
        if (restControllerClassAnnotations(content).length() == 0) {
            return;
        }
        Matcher matcher = PUBLIC_METHOD_DECLARATION_PATTERN.matcher(content);
        int previousMethodEnd = restControllerClassEnd(content);
        while (matcher.find()) {
            String annotations = content.substring(previousMethodEnd, matcher.start());
            String methodName = matcher.group(1);
            if (httpMappingCount(annotations) > 0 && !annotations.contains("")) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " method=" + methodName);
            }
            previousMethodEnd = matcher.end();
        }
    }

    private static void collectSingleHttpMappingViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSource(path);
        if (restControllerClassAnnotations(content).length() == 0) {
            return;
        }
        Matcher matcher = PUBLIC_METHOD_DECLARATION_PATTERN.matcher(content);
        int previousMethodEnd = restControllerClassEnd(content);
        while (matcher.find()) {
            String annotations = content.substring(previousMethodEnd, matcher.start());
            String methodName = matcher.group(1);
            int mappingCount = httpMappingCount(annotations);
            if (mappingCount > 1) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " method=" + methodName);
            }
            previousMethodEnd = matcher.end();
        }
    }

    private static void collectPostOrGetMappingViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSource(path);
        if (restControllerClassAnnotations(content).length() == 0) {
            return;
        }
        Matcher matcher = PUBLIC_METHOD_DECLARATION_PATTERN.matcher(content);
        int previousMethodEnd = restControllerClassEnd(content);
        while (matcher.find()) {
            String annotations = content.substring(previousMethodEnd, matcher.start());
            String methodName = matcher.group(1);
            if (annotations.contains("@RequestMapping")
                    || annotations.contains("@PutMapping")
                    || annotations.contains("@DeleteMapping")
                    || annotations.contains("@PatchMapping")) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " method=" + methodName);
            }
            previousMethodEnd = matcher.end();
        }
    }

    private static void collectJsonPostMappingViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSource(path);
        if (restControllerClassAnnotations(content).length() == 0) {
            return;
        }
        Matcher matcher = PUBLIC_METHOD_DECLARATION_PATTERN.matcher(content);
        int previousMethodEnd = restControllerClassEnd(content);
        while (matcher.find()) {
            String annotations = content.substring(previousMethodEnd, matcher.start());
            String methodName = matcher.group(1);
            int methodBodyStart = content.indexOf("{", matcher.end());
            if (methodBodyStart < 0) {
                continue;
            }
            String signature = content.substring(matcher.start(), methodBodyStart);
            if (signature.contains("@RequestBody") && !annotations.contains("@PostMapping")) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " method=" + methodName);
            }
            previousMethodEnd = matcher.end();
        }
    }

    private static void collectGetMappingReturnViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSource(path);
        if (restControllerClassAnnotations(content).length() == 0) {
            return;
        }
        Matcher matcher = PUBLIC_METHOD_DECLARATION_PATTERN.matcher(content);
        int previousMethodEnd = restControllerClassEnd(content);
        while (matcher.find()) {
            String annotations = content.substring(previousMethodEnd, matcher.start());
            String methodName = matcher.group(1);
            String declaration = content.substring(matcher.start(), matcher.end());
            if (annotations.contains("@GetMapping") && !declaration.startsWith("public void ")) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " method=" + methodName);
            }
            previousMethodEnd = matcher.end();
        }
    }

    private static void collectRequestBodyValidViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSource(path);
        if (restControllerClassAnnotations(content).length() == 0) {
            return;
        }
        Matcher matcher = PUBLIC_METHOD_DECLARATION_PATTERN.matcher(content);
        while (matcher.find()) {
            String methodName = matcher.group(1);
            int methodBodyStart = content.indexOf("{", matcher.end());
            if (methodBodyStart < 0) {
                continue;
            }
            String signature = content.substring(matcher.start(), methodBodyStart);
            if (signature.contains("@RequestBody") && signature.contains("Request") && !signature.contains("@Valid")) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " method=" + methodName);
            }
        }
    }

    private static void collectResponseConstructorViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSourceWithoutComments(path);
        if (restControllerClassAnnotations(content).length() == 0) {
            return;
        }
        Matcher matcher = RESPONSE_CONSTRUCTOR_PATTERN.matcher(content);
        while (matcher.find()) {
            violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " response=" + matcher.group(1));
        }
    }

    private static int httpMappingCount(String annotations) {
        int count = 0;
        for (String annotation : METHOD_MAPPING_ANNOTATIONS) {
            if (annotations.contains(annotation)) {
                count++;
            }
        }
        return count;
    }

    private static String restControllerClassAnnotations(String content) {
        Matcher matcher = REST_CONTROLLER_CLASS_PATTERN.matcher(content);
        if (matcher.find() && containsRestControllerAnnotation(matcher.group(1))) {
            return matcher.group(1);
        }
        return "";
    }

    private static int restControllerClassEnd(String content) {
        Matcher matcher = REST_CONTROLLER_CLASS_PATTERN.matcher(content);
        if (matcher.find() && containsRestControllerAnnotation(matcher.group(1))) {
            return matcher.end();
        }
        return 0;
    }

    private static boolean containsRestControllerAnnotation(String annotations) {
        for (String annotation : REST_CONTROLLER_ANNOTATIONS) {
            if (annotations.contains(annotation)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isApiResourcePath(String path) {
        if (path == null || !path.startsWith("/api/")) {
            return false;
        }
        String[] segments = path.split("/");
        return segments.length >= 4 && segments[2].length() > 0 && segments[3].length() > 0;
    }
}
