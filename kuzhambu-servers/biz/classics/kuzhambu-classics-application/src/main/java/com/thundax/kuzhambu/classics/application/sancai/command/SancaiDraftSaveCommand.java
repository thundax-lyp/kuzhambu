package com.thundax.kuzhambu.classics.application.sancai.command;

import java.util.Date;
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
    private Date autosavedAt;
    private String draftJson;
}
