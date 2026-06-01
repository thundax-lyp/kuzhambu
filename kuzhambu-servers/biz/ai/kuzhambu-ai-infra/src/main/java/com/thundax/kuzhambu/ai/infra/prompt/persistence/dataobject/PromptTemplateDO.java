package com.thundax.kuzhambu.ai.infra.prompt.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_prompt_template")
public class PromptTemplateDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long templateId;
    private String scope;
    private String capability;
    private String name;
    private String description;
    private String status;
    private Integer currentVersionNo;
    private Instant registeredAt;
}
