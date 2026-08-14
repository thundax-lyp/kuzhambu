package com.thundax.kuzhambu.knowledge.domain.graph.helper;

import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphEdgeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphNodeKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.Map;
import java.util.TreeMap;

public final class GraphKeyHelper {

    private GraphKeyHelper() {}

    public static GraphNodeKey generateNodeKey(GraphNodeType nodeType, String name, String identityQualifier) {
        return generateNodeKey(
                nodeType, name, Map.of("identityQualifier", identityQualifier == null ? "" : identityQualifier));
    }

    public static GraphNodeKey generateNodeKey(GraphNodeType nodeType, String name, Map<String, String> keyFields) {
        if (nodeType == null || isBlank(name)) {
            return null;
        }
        String canonical = canonicalValues(nodeType.value(), name, normalizeQualifiers(keyFields));
        return new GraphNodeKey("node:" + nodeType.value() + ":" + sha256(canonical));
    }

    public static GraphEdgeKey generateEdgeKey(
            GraphNodeKey sourceNodeKey,
            GraphNodeKey targetNodeKey,
            String relationType,
            boolean directed,
            Map<String, String> keyQualifiers) {
        if (sourceNodeKey == null || targetNodeKey == null || isBlank(relationType)) {
            return null;
        }
        String source = sourceNodeKey.value();
        String target = targetNodeKey.value();
        if (!directed && source.compareTo(target) > 0) {
            String temporary = source;
            source = target;
            target = temporary;
        }
        String canonical = canonicalValues(
                relationType, Boolean.toString(directed), source, target, normalizeQualifiers(keyQualifiers));
        return new GraphEdgeKey("edge:" + normalize(relationType) + ":" + sha256(canonical));
    }

    private static String normalizeQualifiers(Map<String, String> keyQualifiers) {
        if (keyQualifiers == null || keyQualifiers.isEmpty()) {
            return "";
        }
        StringBuilder canonical = new StringBuilder();
        new TreeMap<>(keyQualifiers).forEach((key, value) -> {
            appendValue(canonical, key);
            appendValue(canonical, value);
        });
        return canonical.toString();
    }

    private static String canonicalValues(String... values) {
        StringBuilder canonical = new StringBuilder();
        for (String value : values) {
            appendValue(canonical, value);
        }
        return canonical.toString();
    }

    private static void appendValue(StringBuilder target, String value) {
        String normalized = normalize(value);
        target.append(normalized.length()).append(':').append(normalized);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(java.util.Locale.ROOT);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte item : digest) {
                result.append(String.format("%02x", item));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
