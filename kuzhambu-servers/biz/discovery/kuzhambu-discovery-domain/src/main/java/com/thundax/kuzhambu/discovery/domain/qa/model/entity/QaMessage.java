package com.thundax.kuzhambu.discovery.domain.qa.model.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QaMessage {
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
