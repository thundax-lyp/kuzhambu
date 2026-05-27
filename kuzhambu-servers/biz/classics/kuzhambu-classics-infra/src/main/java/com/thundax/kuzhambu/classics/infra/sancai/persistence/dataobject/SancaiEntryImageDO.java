package com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("classics_sancai_entry_image")
public class SancaiEntryImageDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long entryId;
    private Long storageObjectId;
    private String imageType;
    private String title;
    private Boolean currentUsed;
    private Integer priority;
}
