package com.thundax.kuzhambu.classics.application.sancai.command;

import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVolumeId;
import java.util.List;

public record SancaiVolumeSortCommand(List<SancaiVolumeId> orderedIds) {}
