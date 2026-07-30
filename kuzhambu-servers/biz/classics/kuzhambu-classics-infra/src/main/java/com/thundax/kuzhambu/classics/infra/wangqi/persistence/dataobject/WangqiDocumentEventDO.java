package com.thundax.kuzhambu.classics.infra.wangqi.persistence.dataobject;

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
@TableName("classics_wangqi_document_event")
public class WangqiDocumentEventDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long documentId;
    private String title;
    private Instant occurredAt;
    private String occurredLabel;
    private String summary;
    private Integer priority;
}
