package com.thundax.kuzhambu.common.core.tree;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;

public final class TreeNodeListHelper {

    private TreeNodeListHelper() {}

    public static <T, ID> List<T> remove(
            @NonNull List<T> nodeList, @NonNull TreeNodeSupport<T, ID> support, @NonNull Set<ID> excludeIds) {
        Set<ID> ids =
                new HashSet<>(nodeList.stream().map(node -> support.getId(node)).collect(Collectors.toList()));

        int size = 0;
        while (size != nodeList.size()) {
            size = nodeList.size();

            Iterator<T> iterator = nodeList.iterator();
            while (iterator.hasNext()) {
                T node = iterator.next();
                ID id = support.getId(node);
                if (excludeIds.contains(id)) {
                    iterator.remove();
                    ids.remove(id);

                } else {
                    ID parentId = support.getParentId(node);
                    if (!support.isRoot(node) && !ids.contains(parentId)) {
                        iterator.remove();
                        ids.remove(id);
                    }
                }
            }
        }

        return nodeList;
    }

    public interface TreeNodeSupport<T, ID> {

        ID getId(T node);

        ID getParentId(T node);

        boolean isRoot(T node);
    }
}
