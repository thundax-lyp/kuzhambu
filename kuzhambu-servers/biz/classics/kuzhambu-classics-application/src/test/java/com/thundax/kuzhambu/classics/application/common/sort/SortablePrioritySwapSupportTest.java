package com.thundax.kuzhambu.classics.application.common.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class SortablePrioritySwapSupportTest {

    @Test
    void sortShouldSwapPrioritiesToMatchOrderedIds() {
        List<Item> items = List.of(new Item(Id.of(1L), 10), new Item(Id.of(2L), 20), new Item(Id.of(3L), 30));
        Map<Id, Item> itemById = items.stream().collect(Collectors.toMap(Item::id, Function.identity()));

        SortablePrioritySwapSupport.sort(
                List.of(Id.of(3L), Id.of(1L), Id.of(2L)),
                items,
                Item::id,
                Id::value,
                Item::priority,
                () -> 30,
                (id, priority) -> itemById.get(id).setPriority(priority));

        assertEquals(20, itemById.get(Id.of(1L)).priority());
        assertEquals(30, itemById.get(Id.of(2L)).priority());
        assertEquals(10, itemById.get(Id.of(3L)).priority());
    }

    @Test
    void sortShouldRejectDuplicateOrderedIds() {
        List<Item> items = List.of(new Item(Id.of(1L), 10), new Item(Id.of(2L), 20), new Item(Id.of(3L), 30));

        BizException exception = assertThrows(
                BizException.class,
                () -> SortablePrioritySwapSupport.sort(
                        List.of(Id.of(1L), Id.of(1L), Id.of(2L)),
                        items,
                        Item::id,
                        Id::value,
                        Item::priority,
                        () -> 30,
                        (id, priority) -> {}));

        assertEquals(ErrorCode.SORT_MISSING_ID.getCode(), exception.getCode());
    }

    @Test
    void sortShouldRejectMissingOrderedIds() {
        List<Item> items = List.of(new Item(Id.of(1L), 10), new Item(Id.of(2L), 20));

        BizException exception = assertThrows(
                BizException.class,
                () -> SortablePrioritySwapSupport.sort(
                        List.of(Id.of(1L), Id.of(3L)),
                        items,
                        Item::id,
                        Id::value,
                        Item::priority,
                        () -> 20,
                        (id, priority) -> {}));

        assertEquals(ErrorCode.SORT_MISSING_ID.getCode(), exception.getCode());
    }

    private record Id(Long value) {

        static Id of(Long value) {
            return new Id(value);
        }
    }

    private static final class Item {

        private final Id id;
        private int priority;

        private Item(Id id, int priority) {
            this.id = id;
            this.priority = priority;
        }

        private Id id() {
            return id;
        }

        private int priority() {
            return priority;
        }

        private void setPriority(int priority) {
            this.priority = priority;
        }
    }
}
