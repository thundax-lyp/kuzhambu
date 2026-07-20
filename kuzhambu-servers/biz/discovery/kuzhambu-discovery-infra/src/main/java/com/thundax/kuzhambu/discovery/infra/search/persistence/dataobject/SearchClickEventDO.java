package com.thundax.kuzhambu.discovery.infra.search.persistence.dataobject;

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
@TableName("discovery_search_click_event")
public class SearchClickEventDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String searchClickEventId;
    private String searchEventId;
    private String contentDomain;
    private String contentType;
    private String contentId;
    private String contentTitle;
    private String resultGroupKey;
    private Integer resultRank;
    private Integer groupRank;
    private String targetPath;
    private String operatorType;
    private String operatorId;
    private String requestId;
    private String traceId;
    private Date createdAt;
}
