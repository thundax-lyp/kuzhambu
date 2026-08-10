package com.thundax.kuzhambu.classics.application.sancai.command;

import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVolumeType;

public record SancaiVolumeCommand(
        Long id, Long categoryId, String title, SancaiVolumeType volumeType, Integer priority) {}
