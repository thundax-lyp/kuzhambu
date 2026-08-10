package com.thundax.kuzhambu.classics.application.sancai.assembler;

import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagCommand;
import com.thundax.kuzhambu.classics.application.content.support.SancaiEntryVersionSnapshot;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiShowcaseCommand;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentTagStatus;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiShowcase;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiShowcaseStatus;
import java.time.Instant;
import org.apache.commons.lang3.StringUtils;

public final class SancaiApplicationAssembler {

    private SancaiApplicationAssembler() {}

    public static SancaiShowcase toShowcase(SancaiShowcaseCommand command) {
        SancaiShowcase showcase = new SancaiShowcase();
        if (command == null) {
            return showcase;
        }
        showcase.setRequestedAt(command.requestedAt() == null ? Instant.now() : command.requestedAt());
        showcase.setStatus(command.status() == null ? SancaiShowcaseStatus.REQUESTED : command.status());
        showcase.setScopeJson(command.scopeJson());
        showcase.setScopeTitle(command.scopeTitle());
        showcase.setEntryCount(command.entryCount());
        showcase.setVisibilityRiskStatus(command.visibilityRiskStatus());
        return showcase;
    }

    public static ContentTagCommand toContentTagCommand(
            SancaiEntryVersionSnapshot.SancaiTagSnapshot snapshot, Long contentId) {
        return new ContentTagCommand(
                null,
                ClassicsContentType.SANCAI_ENTRY,
                contentId,
                snapshot.tagId(),
                snapshot.tagNameSnapshot(),
                parseSource(snapshot.source()),
                parseTagStatus(snapshot.status()));
    }

    public static ContentQaPairCommand toContentQaPairCommand(
            SancaiEntryVersionSnapshot.SancaiQaPairSnapshot snapshot, Long contentId) {
        return new ContentQaPairCommand(
                null,
                ClassicsContentType.SANCAI_ENTRY,
                contentId,
                snapshot.question(),
                snapshot.answer(),
                parseSource(snapshot.source()));
    }

    private static ClassicsContentSource parseSource(String value) {
        return StringUtils.isBlank(value) ? ClassicsContentSource.MANUAL : ClassicsContentSource.valueOf(value);
    }

    private static ClassicsContentTagStatus parseTagStatus(String value) {
        return StringUtils.isBlank(value) ? ClassicsContentTagStatus.ACTIVE : ClassicsContentTagStatus.valueOf(value);
    }
}
