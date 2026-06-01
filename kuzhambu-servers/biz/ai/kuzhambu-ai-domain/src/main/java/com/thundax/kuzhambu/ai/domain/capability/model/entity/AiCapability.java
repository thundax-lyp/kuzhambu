package com.thundax.kuzhambu.ai.domain.capability.model.entity;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiCapability {

    private Long id;
    private String capability;
    private String name;
    private List<String> requiredTags = new ArrayList<>();
    private String outputMode;
    private boolean enabled = true;
    private int priority;

    public boolean isSatisfiedBy(List<String> modelTags) {
        if (!enabled) {
            return false;
        }
        if (requiredTags == null || requiredTags.isEmpty()) {
            return true;
        }
        return modelTags != null && modelTags.containsAll(requiredTags);
    }
}
