package com.thundax.kuzhambu.discovery.application.qa.result;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QaMessageResult {
    private Long id;
    private Long sessionId;
    private String role;
    private String content;
    private String messageStatus;
    private Integer contextTurnCount;
    private String failureReason;
    private Date sentAt;
    private Date answeredAt;

    public Long getMessageId() {
        return id;
    }

    public void setMessageId(Long messageId) {
        this.id = messageId;
    }
}
