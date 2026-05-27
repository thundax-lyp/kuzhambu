package com.thundax.kuzhambu.system.application.core.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateDictCommand {
    private String type;
    private String label;
    private String value;
    private String remarks;
}
