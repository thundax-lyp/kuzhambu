package com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("discovery_qa_message_source")
public class QaSourceDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long sourceId;
    private String sourceBusinessId;
    private Long messageId;
    private String contentType;
    private Long contentId;
    private String knowledgeBase;
    private String titleSnapshot;
    private String locationLabel;
    private String snippet;
    private String sourcePath;
    private Integer sourceRank;
    private BigDecimal score;
    private String sourceStatus;
    private Date referencedAt;
}
