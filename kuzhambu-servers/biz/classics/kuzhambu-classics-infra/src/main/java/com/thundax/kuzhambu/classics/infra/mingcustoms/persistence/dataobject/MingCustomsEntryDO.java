package com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("classics_ming_customs_entry")
public class MingCustomsEntryDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String category;
    private String chapter;
    private String section;
    private String summary;
    private String contentFormat;
    private String content;
    private String originalExcerpts;
    private String visibility;
}
