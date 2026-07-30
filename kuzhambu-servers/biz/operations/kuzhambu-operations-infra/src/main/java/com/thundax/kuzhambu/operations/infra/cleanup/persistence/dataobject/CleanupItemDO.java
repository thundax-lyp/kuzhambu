package com.thundax.kuzhambu.operations.infra.cleanup.persistence.dataobject;

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
@TableName("operations_cleanup_item")
public class CleanupItemDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long cleanupItemId;
    private Long cleanupId;
    private String targetType;
    private Long targetId;
    private String itemStatus;
    private String failureReason;
    private Instant processedAt;
}
