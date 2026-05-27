package com.thundax.kuzhambu.classics.infra.sharing.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("classics_share_target")
public class ClassicsShareTargetDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long shareLinkId;
    private String contentType;
    private Long contentId;
    private String titleSnapshot;
    private String contentSnapshotJson;
    private String contentVisibilitySnapshot;
    private String targetStatus;
    private Integer priority;
}
