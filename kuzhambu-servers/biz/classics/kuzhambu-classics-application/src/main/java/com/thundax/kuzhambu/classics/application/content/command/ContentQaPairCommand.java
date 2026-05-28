package com.thundax.kuzhambu.classics.application.content.command;

import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentQaPairId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContentQaPairCommand {
    private Long id;
    private ClassicsContentType contentType;
    private Long contentId;
    private String question;
    private String answer;
    private ClassicsContentSource source;
    private int priority;

    public ClassicsContentQaPair toEntity() {
        return new ClassicsContentQaPair(
                ClassicsContentQaPairId.ofNullable(id),
                contentType,
                ClassicsContentIdCodec.toDomain(contentId),
                question,
                answer,
                source,
                priority);
    }
}
