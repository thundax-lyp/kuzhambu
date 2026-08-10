package com.thundax.kuzhambu.classics.application.mingcustoms.command;

import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsContentFormat;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId;
import java.util.Set;

public record MingCustomsCommand(
        MingCustomsEntryId id,
        String title,
        String category,
        String chapter,
        String section,
        String summary,
        MingCustomsContentFormat contentFormat,
        String content,
        String originalExcerpts,
        Set<String> operatorPermissions) {
    public MingCustomsCommand(
            MingCustomsEntryId id,
            String title,
            String category,
            String chapter,
            String section,
            String summary,
            MingCustomsContentFormat contentFormat,
            String content,
            String originalExcerpts) {
        this(id, title, category, chapter, section, summary, contentFormat, content, originalExcerpts, null);
    }
}
