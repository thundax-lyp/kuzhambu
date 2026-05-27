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
    private static final Pattern API_TAGS_PATTERN = Pattern.compile("@Tag\\s*\\(\\s*(?:name\\s*=\\s*)?\"([^\"]+)\"");
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

    public static void assertAdminControllersDeclareRequiredClassAnnotations(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = controllerSources(sourceRoot)) {
            paths.filter(ApiAnnotationArchitectureRuleSupport::isAdminControllerSource)
                    .forEach(path -> collectAdminControllerClassAnnotationViolations(root, path, violations));
        }

        assertTrue(
                "Admin controllers must declare @Tag, @RequestMapping, @SysLogger/@IgnoreSysLogger, and "
                        + "@WrappedApiController/@IgnoreWrappedApiController: " + violations,
                violations.isEmpty());
    }

    public static void assertAdminControllerMethodsDeclareRequiredAnnotations(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = controllerSources(sourceRoot)) {
            paths.filter(ApiAnnotationArchitectureRuleSupport::isAdminControllerSource)
                    .forEach(path -> collectAdminControllerMethodAnnotationViolations(root, path, violations));
        }

        assertTrue(
                "Admin controller methods must declare @Operation, access annotation, @ApiImplicitParams, "
                        + "@SysLogger/@IgnoreSysLogger, and exactly one @PostMapping or @GetMapping: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertPostMappingMethodsUseRequestResponseShape(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = controllerSources(sourceRoot)) {
            paths.filter(ApiAnnotationArchitectureRuleSupport::isAdminControllerSource)
                    .forEach(path -> collectPostMappingShapeViolations(root, path, violations));
        }

        assertTrue(
                "PostMapping methods must use no parameter, a @Valid @RequestBody *Request/List<*Request> "
                        + "parameter, or multipart form parameters, and return void, Boolean, String, *Response, "
                        + "List<*Response>, or PageResponse<*Response>: " + violations,
                violations.isEmpty());
    }

    public static void assertOperationDeclaresAccessAnnotation(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = controllerSources(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectAccessAnnotationViolations(root, path, violations));
        }

        assertTrue("API methods must declare @HasPermission or @PublicApi: " + violations, violations.isEmpty());
    }

    public static void assertRestControllersDeclareRequestMapping(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = controllerSources(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectRequestMappingViolations(root, path, violations));
        }

        assertTrue("REST controllers must declare class-level @RequestMapping: " + violations, violations.isEmpty());
    }

    public static void assertRestControllerRequestMappingsUseApiResourcePath(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = controllerSources(sourceRoot)) {
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

        try (Stream<Path> paths = controllerSources(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectApiClassViolations(root, path, violations));
        }

        assertTrue("REST controllers must declare @Tag: " + violations, violations.isEmpty());
    }

    public static void assertApiTagsDoNotUseNumericPrefix(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = controllerSources(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectApiTagNumericPrefixViolations(root, path, violations));
        }

        assertTrue("API tags must not use numeric prefixes: " + violations, violations.isEmpty());
    }

    public static void assertMappedMethodsDeclareOperation(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = controllerSources(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectOperationViolations(root, path, violations));
        }

        assertTrue("Mapped controller methods must declare @Operation: " + violations, violations.isEmpty());
    }

    public static void assertMappedMethodsDeclareSingleHttpMapping(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = controllerSources(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectSingleHttpMappingViolations(root, path, violations));
        }

        assertTrue(
                "Mapped controller methods must declare exactly one HTTP mapping: " + violations, violations.isEmpty());
    }

    public static void assertMappedMethodsUsePostOrGetMapping(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = controllerSources(sourceRoot)) {
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

        try (Stream<Path> paths = controllerSources(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectJsonPostMappingViolations(root, path, violations));
        }

        assertTrue("JSON request methods must use @PostMapping: " + violations, violations.isEmpty());
    }

    public static void assertGetMappingMethodsReturnVoid(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = controllerSources(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectGetMappingReturnViolations(root, path, violations));
        }

        assertTrue("GET mapping methods must be non-JSON void responses: " + violations, violations.isEmpty());
    }

    public static void assertRequestBodyRequestParametersDeclareValid(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = controllerSources(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectRequestBodyValidViolations(root, path, violations));
        }

        assertTrue("RequestBody request parameters must declare @Valid: " + violations, violations.isEmpty());
    }

    public static void assertControllersDoNotCreateResponses(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = controllerSources(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectResponseConstructorViolations(root, path, violations));
        }

        assertTrue(
                "Controllers must create *Response through *InterfaceAssembler: " + violations, violations.isEmpty());
    }

    private static Stream<Path> controllerSources(Path sourceRoot) throws IOException {
        if (sourceRoot == null || !Files.exists(sourceRoot)) {
            return Stream.empty();
        }
        return Files.walk(sourceRoot);
    }

    private static void collectAdminControllerClassAnnotationViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSource(path);
        Matcher matcher = REST_CONTROLLER_CLASS_PATTERN.matcher(content);
        while (matcher.find()) {
            String annotations = matcher.group(1);
            String className = matcher.group(2);
            collectMissingAnnotation(root, path, className, annotations, "@Tag", violations);
            if (annotations.contains("@Tag") && !annotations.contains("description")) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " target=" + className
                        + " missing=@Tag.description");
            }
            collectMissingAnnotation(root, path, className, annotations, "@RequestMapping", violations);
            if (!annotations.contains("@IgnoreSysLogger")) {
                collectMissingAnnotation(root, path, className, annotations, "@SysLogger", violations);
            }
            if (!annotations.contains("@IgnoreWrappedApiController")) {
                collectMissingAnnotation(root, path, className, annotations, "@WrappedApiController", violations);
            }
        }
    }

    private static void collectAdminControllerMethodAnnotationViolations(
            Path root, Path path, List<String> violations) {
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
            if (!isMappedMethod(annotations)) {
                previousMethodEnd = matcher.end();
                continue;
            }
            collectMissingAnnotation(root, path, methodName, annotations, "@Operation", violations);
            collectMissingAnnotation(root, path, methodName, annotations, "@ApiImplicitParams", violations);
            if (!accessAnnotatedClass
                    && !annotations.contains("@PublicApi")
                    && !annotations.contains("@HasPermission")) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " method=" + methodName
                        + " missing=@HasPermission/@PublicApi");
            }
            if (!annotations.contains("@SysLogger") && !annotations.contains("@IgnoreSysLogger")) {
                collectMissingAnnotation(root, path, methodName, annotations, "@SysLogger", violations);
            }
            if (postOrGetMappingCount(annotations) != 1 || containsUnsupportedMethodMapping(annotations)) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " method=" + methodName
                        + " mapping=@PostMapping/@GetMapping");
            }
            previousMethodEnd = matcher.end();
        }
    }

    private static void collectPostMappingShapeViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSource(path);
        if (restControllerClassAnnotations(content).length() == 0) {
            return;
        }
        Matcher matcher = PUBLIC_METHOD_DECLARATION_PATTERN.matcher(content);
        int previousMethodEnd = restControllerClassEnd(content);
        while (matcher.find()) {
            String annotations = content.substring(previousMethodEnd, matcher.start());
            String methodName = matcher.group(1);
            if (!annotations.contains("@PostMapping")) {
                previousMethodEnd = matcher.end();
                continue;
            }
            int methodBodyStart = content.indexOf("{", matcher.end());
            if (methodBodyStart < 0) {
                previousMethodEnd = matcher.end();
                continue;
            }
            String signature = content.substring(matcher.start(), methodBodyStart);
            if (!hasPostRequestParameterShape(annotations, signature)) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " method=" + methodName
                        + " parameter=" + compact(signature));
            }
            if (!hasPostResponseShape(signature, methodName)) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " method=" + methodName
                        + " return=" + compact(signature));
            }
            previousMethodEnd = matcher.end();
        }
    }

    private static void collectMissingAnnotation(
            Path root, Path path, String targetName, String annotations, String annotation, List<String> violations) {
        if (!annotations.contains(annotation)) {
            violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " target=" + targetName + " missing="
                    + annotation);
        }
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
            if (annotations.contains("@Operation")
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
            if (containsRestControllerAnnotation(annotations) && !annotations.contains("@Tag")) {
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

    private static void collectOperationViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSource(path);
        if (restControllerClassAnnotations(content).length() == 0) {
            return;
        }
        Matcher matcher = PUBLIC_METHOD_DECLARATION_PATTERN.matcher(content);
        int previousMethodEnd = restControllerClassEnd(content);
        while (matcher.find()) {
            String annotations = content.substring(previousMethodEnd, matcher.start());
            String methodName = matcher.group(1);
            if (httpMappingCount(annotations) > 0 && !annotations.contains("@Operation")) {
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

    private static boolean isAdminControllerSource(Path path) {
        String normalized = path.toString().replace('\\', '/');
        return normalized.endsWith("Controller.java")
                && normalized.contains("/interfaces/admin/")
                && path.getParent() != null
                && "controller".equals(path.getParent().getFileName().toString());
    }

    private static boolean isMappedMethod(String annotations) {
        return httpMappingCount(annotations) > 0 || annotations.contains("@RequestMapping");
    }

    private static int postOrGetMappingCount(String annotations) {
        int count = 0;
        if (annotations.contains("@PostMapping")) {
            count++;
        }
        if (annotations.contains("@GetMapping")) {
            count++;
        }
        return count;
    }

    private static boolean containsUnsupportedMethodMapping(String annotations) {
        return annotations.contains("@RequestMapping")
                || annotations.contains("@PutMapping")
                || annotations.contains("@DeleteMapping")
                || annotations.contains("@PatchMapping");
    }

    private static boolean hasPostRequestParameterShape(String annotations, String signature) {
        String parameters = parameters(signature);
        if (parameters.length() == 0) {
            return true;
        }
        if (isMultipartParameterShape(annotations, parameters)) {
            return true;
        }
        if (containsTopLevelComma(parameters)) {
            return false;
        }
        return parameters.contains("Request") && parameters.contains("@Valid") && parameters.contains("@RequestBody");
    }

    private static boolean hasPostResponseShape(String signature, String methodName) {
        String returnType = returnType(signature, methodName);
        return "void".equals(returnType)
                || "Boolean".equals(returnType)
                || "String".equals(returnType)
                || returnType.endsWith("Response")
                || returnType.matches("List\\s*<\\s*\\w+Response\\s*>")
                || returnType.matches("PageResponse\\s*<\\s*\\w+Response\\s*>");
    }

    private static boolean isMultipartParameterShape(String annotations, String parameters) {
        return annotations.contains("MULTIPART_FORM_DATA_VALUE")
                && (parameters.contains("MultipartFile") || parameters.contains("UploadRequest"));
    }

    private static String parameters(String signature) {
        int start = signature.indexOf('(');
        int end = signature.lastIndexOf(')');
        if (start < 0 || end <= start) {
            return "";
        }
        return signature.substring(start + 1, end).trim();
    }

    private static String returnType(String signature, String methodName) {
        Matcher matcher = Pattern.compile("public\\s+(.+?)\\s+" + Pattern.quote(methodName) + "\\s*\\(")
                .matcher(signature);
        return matcher.find() ? matcher.group(1).replaceAll("\\s+", " ").trim() : "";
    }

    private static boolean containsTopLevelComma(String text) {
        int genericDepth = 0;
        int annotationDepth = 0;
        for (int index = 0; index < text.length(); index++) {
            char current = text.charAt(index);
            if (current == '<') {
                genericDepth++;
            } else if (current == '>') {
                genericDepth--;
            } else if (current == '(') {
                annotationDepth++;
            } else if (current == ')') {
                annotationDepth--;
            } else if (current == ',' && genericDepth == 0 && annotationDepth == 0) {
                return true;
            }
        }
        return false;
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", " ").trim();
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
