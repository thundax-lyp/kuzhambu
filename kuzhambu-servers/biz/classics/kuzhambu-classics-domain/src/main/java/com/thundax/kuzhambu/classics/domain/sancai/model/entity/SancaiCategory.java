package com.thundax.kuzhambu.classics.domain.sancai.model.entity;

import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiCategoryType;
import com.thundax.kuzhambu.common.core.sort.Sortable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SancaiCategory implements Sortable {
    private Long id;
    private String title;
    private SancaiCategoryType categoryType;
    private int priority;
}
