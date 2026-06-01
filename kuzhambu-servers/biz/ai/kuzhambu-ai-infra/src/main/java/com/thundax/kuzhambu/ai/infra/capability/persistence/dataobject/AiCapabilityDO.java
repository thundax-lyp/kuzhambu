package com.thundax.kuzhambu.ai.infra.capability.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_capability")
public class AiCapabilityDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String capability;
    private String name;
    private String requiredTagsJson;
    private String outputMode;
    private Boolean enabled;
    private Integer priority;
}
