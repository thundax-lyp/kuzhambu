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
@TableName("ai_service_config")
public class AiServiceConfigDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long serviceId;
    private String serviceRole;
    private String apiSource;
    private String baseUrl;
    private String encryptedApiKey;
    private Boolean enabled;
    private String status;
    private Instant lastCheckedAt;
    private Instant configuredAt;
}
