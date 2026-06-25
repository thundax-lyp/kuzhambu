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
@TableName("discovery_qa_message")
public class QaMessageDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long messageId;
    private Long sessionId;
    private String role;
    private String content;
    private String messageStatus;
    private Integer contextTurnCount;
    private String failureReason;
    private Date sentAt;
    private Date answeredAt;
}
