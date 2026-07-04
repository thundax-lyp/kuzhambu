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
@TableName("discovery_qa_session")
public class QaSessionDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long sessionId;
    private String ownerType;
    private String ownerId;
    private String knowledgeBaseName;
    private String title;
    private String scope;
    private String contextMode;
    private String contextContentType;
    private Long contextContentId;
    private String status;
    private Date openedAt;
    private Date lastMessageAt;
    private Date removedAt;
}
