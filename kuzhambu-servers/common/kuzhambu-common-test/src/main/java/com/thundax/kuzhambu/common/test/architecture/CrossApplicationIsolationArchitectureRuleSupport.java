package com.thundax.kuzhambu.common.test.architecture;

import static com.thundax.kuzhambu.common.test.architecture.ArchitectureAssertions.assertTrue;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CrossApplicationIsolationArchitectureRuleSupport {

    private static final Pattern APPLICATION_PACKAGE_PATTERN =
            Pattern.compile("^com\\.thundax\\.kuzhambu\\.([a-z]+)\\.application(?:\\..+)?$");

    private static final Map<String, Set<String>> LEGACY_CROSS_APPLICATION_ALLOWLIST =
            legacyCrossApplicationAllowlist();

    private CrossApplicationIsolationArchitectureRuleSupport() {}

    public static void assertNoUnexpectedCrossApplicationDependency(JavaClasses classes, String currentDomain) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!javaClass.getPackageName().startsWith("com.thundax.kuzhambu." + currentDomain + ".application.")) {
                continue;
            }
            for (Dependency dependency : javaClass.getDirectDependenciesFromSelf()) {
                String providerDomain = applicationDomainOf(dependency.getTargetClass());
                if (providerDomain == null || providerDomain.equals(currentDomain)) {
                    continue;
                }
                if (isAllowlisted(currentDomain, providerDomain)) {
                    continue;
                }
                violations.add(javaClass.getName() + " -> "
                        + dependency.getTargetClass().getName());
            }
        }

        assertTrue(
                "Application layer must not depend on other business application modules unless allowlisted: "
                        + violations,
                violations.isEmpty());
    }

    private static boolean isAllowlisted(String consumerDomain, String providerDomain) {
        Set<String> providers = LEGACY_CROSS_APPLICATION_ALLOWLIST.get(consumerDomain);
        return providers != null && providers.contains(providerDomain);
    }

    private static String applicationDomainOf(JavaClass javaClass) {
        Matcher matcher = APPLICATION_PACKAGE_PATTERN.matcher(javaClass.getPackageName());
        return matcher.matches() ? matcher.group(1) : null;
    }

    private static Map<String, Set<String>> legacyCrossApplicationAllowlist() {
        Map<String, Set<String>> allowlist = new HashMap<String, Set<String>>();
        put(allowlist, "operations", "storage");
        put(allowlist, "operations", "discovery");
        return allowlist;
    }

    private static void put(Map<String, Set<String>> allowlist, String consumerDomain, String providerDomain) {
        allowlist.computeIfAbsent(consumerDomain, key -> new HashSet<String>()).add(providerDomain);
    }
}
