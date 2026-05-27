package com.thundax.kuzhambu.classics.infra.sharing.persistence.dataobject;

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
@TableName("classics_share_access_record")
public class ClassicsShareAccessRecordDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shareLinkId;
    private Long shareTargetId;
    private LocalDateTime accessedAt;
    private String accessResult;
    private String clientSnapshot;
}
