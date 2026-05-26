package com.thundax.kuzhambu.common.web.assembler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.common.web.response.OptionResponse;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class OptionInterfaceAssemblerTest {

    @Test
    public void shouldConvertPlainValueAndLabelToOptionResponse() {
        OptionResponse response = OptionInterfaceAssembler.toOptionResponse("ENABLED", "启用");

        assertEquals("ENABLED", response.getValue());
        assertEquals("启用", response.getLabel());
    }

    @Test
    public void shouldConvertListToOptionResponses() {
        List<FixtureOption> source =
                Arrays.asList(new FixtureOption("ENABLED", "启用"), new FixtureOption("DISABLED", "禁用"));

        List<OptionResponse> responses =
                OptionInterfaceAssembler.toOptionResponseList(source, FixtureOption::getValue, FixtureOption::getLabel);

        assertEquals(2, responses.size());
        assertEquals("ENABLED", responses.get(0).getValue());
        assertEquals("禁用", responses.get(1).getLabel());
    }

    @Test
    public void shouldReturnEmptyListWhenSourceListIsNull() {
        assertTrue(OptionInterfaceAssembler.toOptionResponseList(null, FixtureOption::getValue, FixtureOption::getLabel)
                .isEmpty());
    }

    private static final class FixtureOption {
        private final String value;
        private final String label;

        private FixtureOption(String value, String label) {
            this.value = value;
            this.label = label;
        }

        private String getValue() {
            return value;
        }

        private String getLabel() {
            return label;
        }
    }
}
