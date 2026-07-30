package com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject;

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
@TableName("discovery_qa_message")
public class QaMessageDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;
    private String role;
    private String content;
    private String answerStatus;
    private String model;
    private Integer contextTurnCount;
    private String failureReason;
    private String providerChatId;
    private String finishReason;
    private Instant sentAt;
    private Instant answeredAt;
}
