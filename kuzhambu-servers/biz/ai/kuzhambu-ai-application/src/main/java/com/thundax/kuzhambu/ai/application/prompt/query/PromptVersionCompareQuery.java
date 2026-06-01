package com.thundax.kuzhambu.ai.application.prompt.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PromptVersionCompareQuery {

    private Long templateId;
    private int leftVersionNo;
    private int rightVersionNo;
}
