package com.thundax.kuzhambu.classics.application.content.command;

import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
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
        return new ClassicsContentQaPair(id, contentType, contentId, question, answer, source, priority);
    }
}
