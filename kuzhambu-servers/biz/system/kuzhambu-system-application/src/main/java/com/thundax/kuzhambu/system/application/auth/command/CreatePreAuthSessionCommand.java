package com.thundax.kuzhambu.system.application.auth.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePreAuthSessionCommand {
    private int expiredSeconds;
}
