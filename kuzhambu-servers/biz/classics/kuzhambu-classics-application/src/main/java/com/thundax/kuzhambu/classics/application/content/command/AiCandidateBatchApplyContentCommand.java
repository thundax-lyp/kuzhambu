package com.thundax.kuzhambu.classics.application.content.command;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AiCandidateBatchApplyContentCommand {

    private List<AiCandidateApplyContentCommand> items;

    public AiCandidateBatchApplyContentCommand(List<AiCandidateApplyContentCommand> items) {
        this.items = items;
    }
}
