package com.thundax.kuzhambu.classics.application.sancai.command;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SancaiDraftCommand {
    private Long entryId;
    private Instant autosavedAt;
    private String draftJson;
}
