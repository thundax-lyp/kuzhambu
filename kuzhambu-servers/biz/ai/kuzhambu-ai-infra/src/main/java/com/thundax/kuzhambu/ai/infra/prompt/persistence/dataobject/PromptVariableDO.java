package com.thundax.kuzhambu.ai.infra.prompt.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_prompt_variable")
public class PromptVariableDO {

    private Long id;
    private Long variableId;
    private Long templateId;
    private String variableName;
    private Boolean required;
    private String description;
    private Integer priority;
}
