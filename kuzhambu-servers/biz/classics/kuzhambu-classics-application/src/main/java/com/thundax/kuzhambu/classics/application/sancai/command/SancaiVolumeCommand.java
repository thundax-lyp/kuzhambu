package com.thundax.kuzhambu.classics.application.sancai.command;

import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVolumeType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SancaiVolumeCommand {
    private Long id;
    private Long categoryId;
    private String title;
    private SancaiVolumeType volumeType;
    private Integer priority;
}
