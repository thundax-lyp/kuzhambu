package com.thundax.kuzhambu.classics.application.sancai.command;

import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiCategoryType;

public record SancaiCategoryCommand(Long id, String title, SancaiCategoryType categoryType, Integer priority) {}
