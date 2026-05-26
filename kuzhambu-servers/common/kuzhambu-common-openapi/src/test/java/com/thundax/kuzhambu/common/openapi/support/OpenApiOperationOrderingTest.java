package com.thundax.kuzhambu.common.openapi.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

public class OpenApiOperationOrderingTest {

    @Test
    public void shouldSortPathsAlphabetically() {
        OpenAPI openAPI = new OpenAPI()
                .paths(new Paths()
                        .addPathItem("/z", new PathItem().get(new Operation()))
                        .addPathItem("/a", new PathItem().get(new Operation()))
                        .addPathItem("/s", new PathItem().get(new Operation())));

        OpenApiOperationOrdering.sort(openAPI);

        assertEquals("/a", new ArrayList<>(openAPI.getPaths().keySet()).get(0));
        assertEquals("/s", new ArrayList<>(openAPI.getPaths().keySet()).get(1));
        assertEquals("/z", new ArrayList<>(openAPI.getPaths().keySet()).get(2));
    }
}
