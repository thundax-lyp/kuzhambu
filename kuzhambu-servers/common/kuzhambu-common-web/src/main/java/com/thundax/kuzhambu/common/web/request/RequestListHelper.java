package com.thundax.kuzhambu.common.web.request;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RequestListHelper {

    private RequestListHelper() {}

    public static <T> List<T> present(List<T> sourceList) {
        return Optional.ofNullable(sourceList)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static <T> List<T> presentUnique(
            List<T> sourceList, String parameterName, Function<String, ? extends RuntimeException> exceptionFactory) {
        List<T> presentList = present(sourceList);
        if (presentList.isEmpty() || new HashSet<>(presentList).size() != presentList.size()) {
            throw exceptionFactory.apply(parameterName);
        }
        return presentList;
    }

    public static <T> List<T> presentUnique(List<T> sourceList) {
        return presentUnique(sourceList, "sourceList", IllegalArgumentException::new);
    }

    public static <S, T> List<T> map(List<S> sourceList, Function<S, T> mappingFunction) {
        return present(sourceList).stream()
                .map(mappingFunction)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
