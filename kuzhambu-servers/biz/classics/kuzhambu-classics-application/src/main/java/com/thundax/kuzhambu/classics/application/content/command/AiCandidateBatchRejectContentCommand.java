package com.thundax.kuzhambu.classics.application.content.command;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AiCandidateBatchRejectContentCommand {

    private List<Item> items;
    private String errorType;
    private String errorMessage;

    public AiCandidateBatchRejectContentCommand(List<Item> items, String errorType, String errorMessage) {
        this.items = items;
        this.errorType = errorType;
        this.errorMessage = errorMessage;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Item {
        private Long candidateId;
        private ClassicsContentType contentType;
        private Long contentId;
        private Long objectId;
        private String capability;
    }
}
