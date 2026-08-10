package com.thundax.kuzhambu.classics.application.content.assembler;

import com.thundax.kuzhambu.classics.application.content.command.ContentExportCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagCommand;
import com.thundax.kuzhambu.classics.domain.common.codec.KnowledgeTagIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentQaPairIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentTagIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;

public final class ClassicsContentApplicationAssembler {

    private ClassicsContentApplicationAssembler() {}

    public static ClassicsContentTag toTag(ContentTagCommand command) {
        if (command == null) {
            return null;
        }
        ClassicsContentTag tag = new ClassicsContentTag();
        tag.setId(ClassicsContentTagIdCodec.toDomain(command.id()));
        tag.setContentType(command.contentType());
        tag.setContentId(ClassicsContentIdCodec.toDomain(command.contentId()));
        tag.setTagId(KnowledgeTagIdCodec.toDomain(command.tagId()));
        tag.setTagNameSnapshot(command.tagNameSnapshot());
        tag.setSource(command.source());
        tag.setStatus(command.status());
        return tag;
    }

    public static ClassicsContentQaPair toQaPair(ContentQaPairCommand command) {
        if (command == null) {
            return null;
        }
        ClassicsContentQaPair qaPair = new ClassicsContentQaPair();
        qaPair.setId(ClassicsContentQaPairIdCodec.toDomain(command.id()));
        qaPair.setContentType(command.contentType());
        qaPair.setContentId(ClassicsContentIdCodec.toDomain(command.contentId()));
        qaPair.setQuestion(command.question());
        qaPair.setAnswer(command.answer());
        qaPair.setSource(command.source());
        return qaPair;
    }

    public static ClassicsContentExportJob toExportJob(ContentExportCommand command) {
        if (command == null) {
            return null;
        }
        return new ClassicsContentExportJob(
                null,
                command.exportKind(),
                command.contentType(),
                command.exportFormat(),
                command.scopeType(),
                command.scopeJson(),
                command.requestedAt(),
                command.expiresAt(),
                command.status(),
                command.storageObjectId(),
                command.itemCount(),
                command.assetCount(),
                command.visibilityRiskStatus(),
                command.contentChanged());
    }
}
