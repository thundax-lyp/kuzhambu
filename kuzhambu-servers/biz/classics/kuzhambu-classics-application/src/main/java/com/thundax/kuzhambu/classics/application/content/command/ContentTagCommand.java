package com.thundax.kuzhambu.classics.application.content.command;

import com.thundax.kuzhambu.classics.domain.common.codec.KnowledgeTagIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentTagIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentTagStatus;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContentTagCommand {
    private Long id;
    private ClassicsContentType contentType;
    private Long contentId;
    private Long tagId;
    private String tagNameSnapshot;
    private ClassicsContentSource source;
    private ClassicsContentTagStatus status;
    private int priority;

    public ClassicsContentTag toEntity() {
        return new ClassicsContentTag(
                ClassicsContentTagIdCodec.toDomain(id),
                contentType,
                ClassicsContentIdCodec.toDomain(contentId),
                KnowledgeTagIdCodec.toDomain(tagId),
                tagNameSnapshot,
                source,
                status,
                priority);
    }
}
