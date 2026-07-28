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
        tag.setId(ClassicsContentTagIdCodec.toDomain(command.getId()));
        tag.setContentType(command.getContentType());
        tag.setContentId(ClassicsContentIdCodec.toDomain(command.getContentId()));
        tag.setTagId(KnowledgeTagIdCodec.toDomain(command.getTagId()));
        tag.setTagNameSnapshot(command.getTagNameSnapshot());
        tag.setSource(command.getSource());
        tag.setStatus(command.getStatus());
        return tag;
    }

    public static ClassicsContentQaPair toQaPair(ContentQaPairCommand command) {
        if (command == null) {
            return null;
        }
        ClassicsContentQaPair qaPair = new ClassicsContentQaPair();
        qaPair.setId(ClassicsContentQaPairIdCodec.toDomain(command.getId()));
        qaPair.setContentType(command.getContentType());
        qaPair.setContentId(ClassicsContentIdCodec.toDomain(command.getContentId()));
        qaPair.setQuestion(command.getQuestion());
        qaPair.setAnswer(command.getAnswer());
        qaPair.setSource(command.getSource());
        return qaPair;
    }

    public static ClassicsContentExportJob toExportJob(ContentExportCommand command) {
        if (command == null) {
            return null;
        }
        return new ClassicsContentExportJob(
                null,
                command.getExportKind(),
                command.getContentType(),
                command.getExportFormat(),
                command.getScopeType(),
                command.getScopeJson(),
                command.getRequestedAt(),
                command.getExpiresAt(),
                command.getStatus(),
                command.getStorageObjectId(),
                command.getItemCount(),
                command.getAssetCount(),
                command.getVisibilityRiskStatus(),
                command.isContentChanged());
    }
}
