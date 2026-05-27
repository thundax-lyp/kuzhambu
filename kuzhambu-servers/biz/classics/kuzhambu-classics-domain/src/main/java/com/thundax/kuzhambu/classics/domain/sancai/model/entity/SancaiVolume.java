package com.thundax.kuzhambu.classics.domain.sancai.model.entity;

import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVolumeType;
import com.thundax.kuzhambu.common.core.sort.Sortable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SancaiVolume implements Sortable {
    private Long id;
    private Long categoryId;
    private String title;
    private SancaiVolumeType volumeType;
    private int priority;
}
