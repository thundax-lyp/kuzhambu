package com.thundax.kuzhambu.system.infra.audit.persistence.dataobject;

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
@TableName("system_audit_meta")
public class AuditMetaDO {
    @TableId(type = IdType.INPUT)
    private Long id;

    private String objectType;
    private String objectId;
    private Long version;
    private Long lastLogId;
    private String lastAction;
    private String lastOperatorType;
    private String lastOperatorId;
    private String lastOperatorName;
    private Date lastOperatedAt;
    private Long createdLogId;
    private Date createdAt;
}
