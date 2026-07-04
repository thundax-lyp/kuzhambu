package com.thundax.kuzhambu.discovery.application.qa.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteQaSessionCommand {
    private Long sessionId;
    private String ownerType;
    private String ownerId;
    private Boolean adminOperation;
}
