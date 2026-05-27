package com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject;

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
@TableName("classics_sancai_entry_draft")
public class SancaiEntryDraftDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long entryId;
    private LocalDateTime autosavedAt;
    private String draftJson;
}
