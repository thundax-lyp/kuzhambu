package com.thundax.kuzhambu.operations.infra.backup.persistence.dataobject;

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
@TableName("operations_backup")
public class BackupDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long backupId;
    private String backupType;
    private String backupStatus;
    private Long storageObjectId;
    private String fileName;
    private Long fileSizeBytes;
    private String checksum;
    private String failureReason;
    private Long requesterUserId;
    private Date startedAt;
    private Date completedAt;
    private Date expiresAt;
}
