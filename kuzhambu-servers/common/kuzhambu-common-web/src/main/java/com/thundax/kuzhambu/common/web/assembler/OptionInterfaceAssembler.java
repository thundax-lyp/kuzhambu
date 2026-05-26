package com.thundax.kuzhambu.common.web.assembler;

import com.thundax.kuzhambu.common.web.response.OptionResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class OptionInterfaceAssembler {
    private OptionInterfaceAssembler() {}

    public static OptionResponse toOptionResponse(String value, String label) {
        return OptionResponse.builder().value(value).label(label).build();
    }

    public static <T> List<OptionResponse> toOptionResponseList(
            List<T> list, Function<T, String> valueReader, Function<T, String> labelReader) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<OptionResponse> result = new ArrayList<>(list.size());
        for (T item : list) {
            result.add(toOptionResponse(
                    item == null ? null : valueReader.apply(item), item == null ? null : labelReader.apply(item)));
        }
        return result;
    }
}
