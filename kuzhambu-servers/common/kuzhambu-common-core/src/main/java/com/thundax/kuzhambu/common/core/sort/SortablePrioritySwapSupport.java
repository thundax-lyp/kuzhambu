package com.thundax.kuzhambu.common.core.sort;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.ErrorCode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;

public final class SortablePrioritySwapSupport {

    private SortablePrioritySwapSupport() {}

    public static <T, I> void sort(
            List<I> orderedIds,
            List<T> currentItems,
            Function<T, I> itemIdExtractor,
            Function<I, Long> idValueExtractor,
            ToIntFunction<T> priorityExtractor,
            IntSupplier maxPrioritySupplier,
            PriorityUpdater<I> priorityUpdater) {
        if (orderedIds == null || orderedIds.isEmpty()) {
            throw sortEmptyInput();
        }
        if (currentItems == null || currentItems.isEmpty() || currentItems.size() != orderedIds.size()) {
            throw sortMissingId();
        }

        Map<Long, Integer> indexById = new HashMap<>(currentItems.size());
        Map<Long, Integer> priorityById = new HashMap<>(currentItems.size());
        List<I> currentOrderedIds = new ArrayList<>(currentItems.size());
        for (int i = 0; i < currentItems.size(); i++) {
            T item = currentItems.get(i);
            I itemId = item == null ? null : itemIdExtractor.apply(item);
            Long idValue = itemId == null ? null : idValueExtractor.apply(itemId);
            if (idValue == null || indexById.put(idValue, i) != null) {
                throw sortDbFailure();
            }
            priorityById.put(idValue, priorityExtractor.applyAsInt(item));
            currentOrderedIds.add(itemId);
        }

        HashSet<Long> orderedIdValues = new HashSet<>(orderedIds.size());
        for (I orderedId : orderedIds) {
            Long idValue = orderedId == null ? null : idValueExtractor.apply(orderedId);
            if (idValue == null || !indexById.containsKey(idValue) || !orderedIdValues.add(idValue)) {
                throw sortMissingId();
            }
        }
        if (orderedIdValues.size() != indexById.size()) {
            throw sortMissingId();
        }

        int temporaryPriority = maxPrioritySupplier.getAsInt() + 1;
        for (int i = 0; i < currentOrderedIds.size(); i++) {
            I targetId = orderedIds.get(i);
            I currentId = currentOrderedIds.get(i);
            if (targetId.equals(currentId)) {
                continue;
            }

            Long targetIdValue = idValueExtractor.apply(targetId);
            Long currentIdValue = idValueExtractor.apply(currentId);
            int targetIndex = indexById.get(targetIdValue);
            int currentPriority = priorityById.get(currentIdValue);
            int targetPriority = priorityById.get(targetIdValue);

            priorityUpdater.update(targetId, temporaryPriority++);
            priorityUpdater.update(currentId, targetPriority);
            priorityUpdater.update(targetId, currentPriority);

            priorityById.put(targetIdValue, currentPriority);
            priorityById.put(currentIdValue, targetPriority);
            currentOrderedIds.set(i, targetId);
            currentOrderedIds.set(targetIndex, currentId);
            indexById.put(targetIdValue, i);
            indexById.put(currentIdValue, targetIndex);
        }
    }

    @FunctionalInterface
    public interface PriorityUpdater<I> {

        void update(I id, int priority);
    }

    private static BizException sortEmptyInput() {
        return new BizException(
                ErrorCode.SORT_EMPTY_INPUT.getCode(),
                ErrorCode.SORT_EMPTY_INPUT.getMessageKey(),
                ErrorCode.SORT_EMPTY_INPUT.getMessage());
    }

    private static BizException sortMissingId() {
        return new BizException(
                ErrorCode.SORT_MISSING_ID.getCode(),
                ErrorCode.SORT_MISSING_ID.getMessageKey(),
                ErrorCode.SORT_MISSING_ID.getMessage());
    }

    private static BizException sortDbFailure() {
        return new BizException(
                ErrorCode.SORT_DB_FAILURE.getCode(),
                ErrorCode.SORT_DB_FAILURE.getMessageKey(),
                ErrorCode.SORT_DB_FAILURE.getMessage());
    }
}
