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
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("批量应用候选参数为空");
        }
        this.items = items;
    }
}
