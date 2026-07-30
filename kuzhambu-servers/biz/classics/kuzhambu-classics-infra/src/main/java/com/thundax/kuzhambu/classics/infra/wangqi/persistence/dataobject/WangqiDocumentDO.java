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
@TableName("classics_wangqi_document")
public class WangqiDocumentDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String summary;
    private String contentFormat;
    private String content;
    private Instant documentTime;
    private Long storageObjectId;
    private String visibility;
    private Long currentVersionId;
    private Integer currentVersionNo;
    private Instant currentVersionedAt;
    private Instant contentUpdatedAt;
}
