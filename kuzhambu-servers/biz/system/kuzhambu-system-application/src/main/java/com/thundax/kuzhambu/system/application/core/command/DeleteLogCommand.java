package com.thundax.kuzhambu.system.application.core.command;

import com.thundax.kuzhambu.system.application.core.query.LogQuery;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteLogCommand {
    private LogQuery query;
}
