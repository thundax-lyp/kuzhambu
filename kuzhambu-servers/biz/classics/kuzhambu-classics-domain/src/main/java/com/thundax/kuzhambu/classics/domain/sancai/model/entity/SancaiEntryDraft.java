package com.thundax.kuzhambu.classics.domain.sancai.model.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SancaiEntryDraft {
    private Long id;
    private Long entryId;
    private LocalDateTime autosavedAt;
    private String draftJson;
}
