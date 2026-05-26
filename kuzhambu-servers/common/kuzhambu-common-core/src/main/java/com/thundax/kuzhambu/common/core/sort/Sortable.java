package com.thundax.kuzhambu.common.core.sort;

/**
 * Contract for domain objects that maintain a server-side sort priority.
 */
public interface Sortable {

    int getPriority();

    void setPriority(int priority);
}
