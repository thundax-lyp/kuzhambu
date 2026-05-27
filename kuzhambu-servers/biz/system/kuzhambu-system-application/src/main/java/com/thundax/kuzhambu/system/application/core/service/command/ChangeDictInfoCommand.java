package com.thundax.kuzhambu.system.application.core.service.command;

import com.thundax.kuzhambu.system.domain.core.model.valueobject.DictId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeDictInfoCommand {
    private DictId id;
    private String type;
    private String label;
    private String value;
    private String remarks;
}
