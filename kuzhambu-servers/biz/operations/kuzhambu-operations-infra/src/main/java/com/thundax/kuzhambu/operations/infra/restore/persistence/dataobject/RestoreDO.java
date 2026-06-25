package com.thundax.kuzhambu.operations.infra.restore.persistence.dataobject;

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
@TableName("operations_restore")
public class RestoreDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long restoreId;
    private Long backupId;
    private Long preRestoreBackupId;
    private String restoreStatus;
    private Boolean writeBlockEnabled;
    private String failureReason;
    private Long requesterUserId;
    private Date startedAt;
    private Date completedAt;
}
