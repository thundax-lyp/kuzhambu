package com.thundax.kuzhambu.ai.infra.config.persistence.dataobject;

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
@TableName("ai_business_config")
public class AiBusinessConfigDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String capability;
    private Long promptTemplateId;
    private Long modelId;
    private String defaultParamsJson;
    private Boolean enabled;
    private Instant configuredAt;
}
