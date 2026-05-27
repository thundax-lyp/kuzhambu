package com.thundax.kuzhambu.classics.application.mingcustoms.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MingCustomsKeywordCommand {
    private Long customId;
    private String keyword;
    private int priority;
}
