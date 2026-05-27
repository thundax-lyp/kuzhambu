package com.thundax.kuzhambu.classics.application.sancai.command;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SancaiDraftSaveCommand {
    private Long entryId;
    private LocalDateTime autosavedAt;
    private String draftJson;
}
