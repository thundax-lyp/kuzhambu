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

    public ClassicsContentQaPair toEntity() {
        ClassicsContentQaPair qaPair = new ClassicsContentQaPair();
        qaPair.setId(ClassicsContentQaPairId.ofNullable(id));
        qaPair.setContentType(contentType);
        qaPair.setContentId(ClassicsContentIdCodec.toDomain(contentId));
        qaPair.setQuestion(question);
        qaPair.setAnswer(answer);
        qaPair.setSource(source);
        return qaPair;
    }
}
