package com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject;

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
@TableName("discovery_qa_session_export")
public class QaSessionExportDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long exportId;
    private Long sessionId;
    private String format;
    private Long storageObjectId;
    private String exportStatus;
    private String failureReason;
    private Long requesterUserId;
    private Date requestedAt;
    private Date completedAt;
}
