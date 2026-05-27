package com.thundax.kuzhambu.common.test.architecture;

import static com.thundax.kuzhambu.common.test.architecture.ArchitectureAssertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class RepositoryArchitectureRuleSupport {

    private static final Set<String> SERVER_GROUPS = new HashSet<String>(Arrays.asList("common", "biz", "starter"));
    private static final Set<String> DOMAIN_LAYERS =
            new HashSet<String>(Arrays.asList("interface", "application", "domain", "infra"));
    private static final Set<String> STARTER_MODULES =
            new HashSet<String>(Arrays.asList("kuzhambu-admin-starter", "kuzhambu-portal-starter"));
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("(?m)^\\s*package\\s+([A-Za-z0-9_.]+)\\s*;");

    private RepositoryArchitectureRuleSupport() {}

    public static void assertServerModuleLayout() throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        Path servers = root.resolve("kuzhambu-servers");
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.list(servers)) {
            paths.filter(Files::isDirectory)
                    .filter(path -> !"target".equals(path.getFileName().toString()))
                    .forEach(path -> {
                        String name = path.getFileName().toString();
                        if (!SERVER_GROUPS.contains(name)) {
                            violations.add("kuzhambu-servers/" + name);
                        }
                    });
        }

        assertTrue("Server module groups must be common, biz, starter: " + violations, violations.isEmpty());
    }

    public static void assertMavenModuleNames() throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        assertCommonModuleNames(root, violations);
        assertDomainModuleNames(root, violations);
        assertStarterModuleNames(root, violations);

        assertTrue("Maven modules must follow repository naming rules: " + violations, violations.isEmpty());
    }

    public static void assertJavaPackagesMatchModuleLayout() throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(root.resolve("kuzhambu-servers"))) {
            paths.filter(path -> path.getFileName().toString().endsWith(".java"))
                    .filter(path ->
                            ArchitectureSourceSupport.normalizePath(path).contains("/src/main/java/"))
                    .forEach(path -> collectPackageLayoutViolations(root, path, violations));
        }

        assertTrue("Java package declarations must match module layout: " + violations, violations.isEmpty());
    }

    public static void assertMavenDependenciesStayInsideAllowedBoundaries() throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(root.resolve("kuzhambu-servers"))) {
            paths.filter(path -> "pom.xml".equals(path.getFileName().toString()))
                    .filter(path ->
                            !ArchitectureSourceSupport.normalizePath(path).contains("/target/"))
                    .forEach(path -> collectPomDependencyViolations(root, path, violations));
        }

        assertTrue(
                "Maven dependencies must stay inside allowed module boundaries: " + violations, violations.isEmpty());
    }

    private static void assertCommonModuleNames(Path root, List<String> violations) throws IOException {
        Path common = root.resolve("kuzhambu-servers/common");
        try (Stream<Path> paths = Files.list(common)) {
            paths.filter(Files::isDirectory)
                    .filter(path -> !"target".equals(path.getFileName().toString()))
                    .forEach(path -> {
                        String name = path.getFileName().toString();
                        if (!name.startsWith("kuzhambu-common-")) {
                            violations.add(ArchitectureSourceSupport.repositoryPath(root, path));
                        }
                    });
        }
    }

    private static void assertDomainModuleNames(Path root, List<String> violations) throws IOException {
        Path biz = root.resolve("kuzhambu-servers/biz");
        try (Stream<Path> domains = Files.list(biz)) {
            domains.filter(Files::isDirectory)
                    .filter(path -> !"target".equals(path.getFileName().toString()))
                    .forEach(domainPath -> collectDomainModuleNameViolations(root, domainPath, violations));
        }
    }

    private static void assertStarterModuleNames(Path root, List<String> violations) throws IOException {
        Path starter = root.resolve("kuzhambu-servers/starter");
        try (Stream<Path> paths = Files.list(starter)) {
            paths.filter(Files::isDirectory)
                    .filter(path -> !"target".equals(path.getFileName().toString()))
                    .forEach(path -> {
                        String name = path.getFileName().toString();
                        if (!STARTER_MODULES.contains(name)) {
                            violations.add(ArchitectureSourceSupport.repositoryPath(root, path));
                        }
                    });
        }
    }

    private static void collectDomainModuleNameViolations(Path root, Path domainPath, List<String> violations) {
        String domain = domainPath.getFileName().toString();
        try (Stream<Path> modules = Files.list(domainPath)) {
            modules.filter(Files::isDirectory)
                    .filter(path -> !"target".equals(path.getFileName().toString()))
                    .forEach(modulePath -> {
                        String name = modulePath.getFileName().toString();
                        String prefix = "kuzhambu-" + domain + "-";
                        if (!name.startsWith(prefix) || !DOMAIN_LAYERS.contains(name.substring(prefix.length()))) {
                            violations.add(ArchitectureSourceSupport.repositoryPath(root, modulePath));
                        }
                    });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void collectPackageLayoutViolations(Path root, Path source, List<String> violations) {
        String packageName = packageName(source);
        String normalized = ArchitectureSourceSupport.normalizePath(root.relativize(source));
        String expectedPrefix = expectedPackagePrefix(normalized);
        if (expectedPrefix.length() > 0 && !packageName.startsWith(expectedPrefix)) {
            violations.add(normalized + " package=" + packageName + " expected=" + expectedPrefix);
        }
    }

    private static String expectedPackagePrefix(String path) {
        String[] segments = path.split("/");
        if (segments.length < 7 || !"kuzhambu-servers".equals(segments[0])) {
            return "";
        }
        if ("common".equals(segments[1]) && segments[2].startsWith("kuzhambu-common-")) {
            String capability =
                    segments[2].substring("kuzhambu-common-".length()).replace("-", "");
            return "com.thundax.kuzhambu.common." + capability;
        }
        if ("biz".equals(segments[1]) && segments.length > 3) {
            String domain = segments[2];
            String module = segments[3];
            String prefix = "kuzhambu-" + domain + "-";
            if (module.startsWith(prefix)) {
                String layer = module.substring(prefix.length());
                return "interface".equals(layer)
                        ? "com.thundax.kuzhambu." + domain + ".interfaces"
                        : "com.thundax.kuzhambu." + domain + "." + layer;
            }
        }
        if ("starter".equals(segments[1]) && segments[2].startsWith("kuzhambu-")) {
            if ("kuzhambu-admin-starter".equals(segments[2])) {
                return "com.thundax.kuzhambu.starter.admin";
            }
            if ("kuzhambu-portal-starter".equals(segments[2])) {
                return "com.thundax.kuzhambu.starter.portal";
            }
        }
        return "";
    }

    private static String packageName(Path source) {
        Matcher matcher = PACKAGE_PATTERN.matcher(ArchitectureSourceSupport.readSource(source));
        return matcher.find() ? matcher.group(1) : "";
    }

    private static void collectPomDependencyViolations(Path root, Path pom, List<String> violations) {
        PomModel model = readPom(pom);
        boolean commonModule = ArchitectureSourceSupport.normalizePath(root.relativize(pom))
                .startsWith("kuzhambu-servers/common/kuzhambu-common-");
        boolean starterModule =
                ArchitectureSourceSupport.normalizePath(root.relativize(pom)).startsWith("kuzhambu-servers/starter/");
        for (Dependency dependency : model.dependencies) {
            if (!"com.thundax".equals(dependency.groupId)) {
                continue;
            }
            if (commonModule
                    && dependency.artifactId.startsWith("kuzhambu-")
                    && !dependency.artifactId.startsWith("kuzhambu-common")) {
                violations.add(
                        ArchitectureSourceSupport.repositoryPath(root, pom) + " dependency=" + dependency.artifactId);
            }
            if (!starterModule && STARTER_MODULES.contains(dependency.artifactId)) {
                violations.add(
                        ArchitectureSourceSupport.repositoryPath(root, pom) + " dependency=" + dependency.artifactId);
            }
        }
    }

    private static PomModel readPom(Path pom) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document document = factory.newDocumentBuilder().parse(pom.toFile());
            NodeList dependencyNodes = document.getElementsByTagNameNS("*", "dependency");
            PomModel model = new PomModel();
            for (int index = 0; index < dependencyNodes.getLength(); index++) {
                Node node = dependencyNodes.item(index);
                if (node instanceof Element) {
                    Element dependency = (Element) node;
                    model.dependencies.add(new Dependency(text(dependency, "groupId"), text(dependency, "artifactId")));
                }
            }
            return model;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String text(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagNameNS("*", tagName);
        return nodes.getLength() == 0 ? "" : nodes.item(0).getTextContent().trim();
    }

    private static final class PomModel {
        private final List<Dependency> dependencies = new ArrayList<Dependency>();
    }

    private static final class Dependency {
        private final String groupId;
        private final String artifactId;

        private Dependency(String groupId, String artifactId) {
            this.groupId = groupId;
            this.artifactId = artifactId;
        }
    }
}
