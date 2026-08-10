package com.thundax.kuzhambu.classics.application.sancai.command;

import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryRefinementStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisualAssetStatus;

public record SancaiEntryCommand(
        Long id,
        Long volumeId,
        String title,
        String originalText,
        String translationText,
        String summary,
        SancaiEntryLifecycleStatus lifecycleStatus,
        SancaiEntryTranslationStatus translationStatus,
        SancaiEntryImageStatus imageStatus,
        SancaiEntryVisualAssetStatus visualAssetStatus,
        SancaiEntryRefinementStatus refinementStatus) {}
