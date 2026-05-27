package com.thundax.kuzhambu.classics.infra.content.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("classics_content_version")
public class ClassicsContentVersionDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String contentType;
    private Long contentId;
    private Integer versionNo;
    private LocalDateTime versionedAt;
    private String snapshotJson;
    private String changeType;
    private String changeSummary;
}
