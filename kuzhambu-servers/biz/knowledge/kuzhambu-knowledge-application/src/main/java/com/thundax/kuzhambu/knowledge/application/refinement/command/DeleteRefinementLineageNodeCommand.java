package com.thundax.kuzhambu.knowledge.application.refinement.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteRefinementLineageNodeCommand {
    private Long refinementTaskId;
    private String nodeKey;
    private Long operatorId;
}
