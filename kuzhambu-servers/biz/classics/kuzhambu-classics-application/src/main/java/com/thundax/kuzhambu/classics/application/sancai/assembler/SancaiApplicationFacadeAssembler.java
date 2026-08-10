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
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class SancaiApplicationFacadeAssembler {

    private SancaiApplicationFacadeAssembler() {}

    @NonNull
    public static SancaiShowcase toShowcase(@NonNull SancaiShowcaseCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        SancaiShowcase showcase = new SancaiShowcase();
        showcase.setRequestedAt(command.requestedAt() == null ? Instant.now() : command.requestedAt());
        showcase.setStatus(command.status() == null ? SancaiShowcaseStatus.REQUESTED : command.status());
        showcase.setScopeJson(command.scopeJson());
        showcase.setScopeTitle(command.scopeTitle());
        showcase.setEntryCount(command.entryCount());
        showcase.setVisibilityRiskStatus(command.visibilityRiskStatus());
        return showcase;
    }

    @NonNull
    public static ContentTagCommand toContentTagCommand(
            @NonNull SancaiEntryVersionSnapshot.SancaiTagSnapshot snapshot, @NonNull Long contentId) {
        Objects.requireNonNull(snapshot, "snapshot must not be null");
        Objects.requireNonNull(contentId, "contentId must not be null");
        return new ContentTagCommand(
                null,
                ClassicsContentType.SANCAI_ENTRY,
                contentId,
                snapshot.tagId(),
                snapshot.tagNameSnapshot(),
                parseSource(snapshot.source()),
                parseTagStatus(snapshot.status()));
    }

    @NonNull
    public static ContentQaPairCommand toContentQaPairCommand(
            @NonNull SancaiEntryVersionSnapshot.SancaiQaPairSnapshot snapshot, @NonNull Long contentId) {
        Objects.requireNonNull(snapshot, "snapshot must not be null");
        Objects.requireNonNull(contentId, "contentId must not be null");
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
