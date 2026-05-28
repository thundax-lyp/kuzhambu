package com.thundax.kuzhambu.classics.application.sancai.command;

import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryRefinementStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisualAssetStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SancaiEntrySaveCommand {
    private Long id;
    private Long volumeId;
    private String title;
    private String originalText;
    private String translationText;
    private String summary;
    private SancaiEntryLifecycleStatus lifecycleStatus;
    private SancaiEntryVisibility visibility;
    private SancaiEntryTranslationStatus translationStatus;
    private SancaiEntryImageStatus imageStatus;
    private SancaiEntryVisualAssetStatus visualAssetStatus;
    private SancaiEntryRefinementStatus refinementStatus;
}
