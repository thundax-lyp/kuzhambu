package com.thundax.kuzhambu.classics.application.mingcustoms.command;

import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsContentFormat;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsVisibility;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MingCustomsCommand {
    private MingCustomsEntryId id;
    private String title;
    private String category;
    private String chapter;
    private String section;
    private String summary;
    private MingCustomsContentFormat contentFormat;
    private String content;
    private String originalExcerpts;
    private MingCustomsVisibility visibility;
    private Set<String> operatorPermissions;

    public MingCustomsCommand(
            MingCustomsEntryId id,
            String title,
            String category,
            String chapter,
            String section,
            String summary,
            MingCustomsContentFormat contentFormat,
            String content,
            String originalExcerpts,
            MingCustomsVisibility visibility) {
        this(
                id,
                title,
                category,
                chapter,
                section,
                summary,
                contentFormat,
                content,
                originalExcerpts,
                visibility,
                null);
    }
}
