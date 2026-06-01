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
@TableName("ai_model_check_record")
public class AiModelCheckRecordDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long checkId;
    private Long modelId;
    private Long serviceId;
    private String modelName;
    private String status;
    private Integer latencyMs;
    private String errorType;
    private String errorMessage;
    private Instant checkedAt;
}
