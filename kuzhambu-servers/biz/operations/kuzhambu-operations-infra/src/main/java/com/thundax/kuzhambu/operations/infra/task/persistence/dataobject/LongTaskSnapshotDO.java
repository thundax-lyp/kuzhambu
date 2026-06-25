package com.thundax.kuzhambu.operations.infra.task.persistence.dataobject;

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
@TableName("operations_long_task_snapshot")
public class LongTaskSnapshotDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long snapshotId;
    private String sourceDomain;
    private String taskType;
    private String taskKey;
    private String taskStatus;
    private Integer totalCount;
    private Integer successCount;
    private Integer failedCount;
    private String failureReason;
    private Long requestedByUserId;
    private Date startedAt;
    private Date completedAt;
    private Date snapshotAt;
}
