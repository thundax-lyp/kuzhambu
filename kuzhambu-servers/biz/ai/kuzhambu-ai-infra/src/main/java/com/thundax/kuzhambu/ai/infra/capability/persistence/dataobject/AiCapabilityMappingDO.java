package com.thundax.kuzhambu.ai.infra.capability.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_capability_mapping")
public class AiCapabilityMappingDO {

    private Long id;
    private Long mappingId;
    private String scope;
    private String capability;
    private Long modelId;
    private Boolean enabled;
    private Instant configuredAt;
}
