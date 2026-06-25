package com.thundax.kuzhambu.operations.infra.health.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("operations_health_check")
public class HealthCheckDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long checkId;
    private String component;
    private String healthStatus;
    private Integer latencyMs;
    private String message;
    private Date checkedAt;
}
