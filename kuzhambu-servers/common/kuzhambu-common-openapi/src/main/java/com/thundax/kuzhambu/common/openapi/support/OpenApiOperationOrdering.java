package com.thundax.kuzhambu.common.openapi.support;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public final class OpenApiOperationOrdering {

    private OpenApiOperationOrdering() {}

    public static void sort(OpenAPI openAPI) {
        if (openAPI == null || openAPI.getPaths() == null || openAPI.getPaths().isEmpty()) {
            return;
        }
        TreeMap<String, PathItem> sortedPaths = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        sortedPaths.putAll(openAPI.getPaths());
        openAPI.getPaths().clear();
        for (Map.Entry<String, PathItem> entry : sortedPaths.entrySet()) {
            openAPI.getPaths().addPathItem(entry.getKey(), sortOperations(entry.getValue()));
        }
    }

    private static PathItem sortOperations(PathItem pathItem) {
        if (pathItem == null || pathItem.readOperationsMap().isEmpty()) {
            return pathItem;
        }
        TreeMap<PathItem.HttpMethod, Operation> sortedOperations = new TreeMap<>(Comparator.comparing(Enum::name));
        sortedOperations.putAll(pathItem.readOperationsMap());
        sortedOperations.forEach(pathItem::operation);
        return pathItem;
    }
}
