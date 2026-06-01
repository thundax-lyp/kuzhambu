package com.thundax.kuzhambu.ai.infra.model.persistence.dataobject;

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
@TableName("ai_model")
public class AiModelDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long modelId;
    private Long serviceId;
    private String modelName;
    private String displayName;
    private String capabilityTagsJson;
    private String defaultParamsJson;
    private String description;
    private Boolean enabled;
    private Instant registeredAt;
}
