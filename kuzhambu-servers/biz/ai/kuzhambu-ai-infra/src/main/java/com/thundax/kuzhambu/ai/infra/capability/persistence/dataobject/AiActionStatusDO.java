package com.thundax.kuzhambu.ai.infra.capability.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_action_status")
public class AiActionStatusDO {

    private Long id;
    private Long actionStatusId;
    private String scope;
    private String capability;
    private Boolean available;
    private String unavailableReason;
    private Instant checkedAt;
}
