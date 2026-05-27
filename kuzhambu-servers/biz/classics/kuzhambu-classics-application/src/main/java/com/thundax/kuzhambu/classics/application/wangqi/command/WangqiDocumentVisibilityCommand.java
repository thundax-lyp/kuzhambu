package com.thundax.kuzhambu.classics.application.wangqi.command;

import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WangqiDocumentVisibilityCommand {
    private Long id;
    private WangqiDocumentVisibility visibility;
}
