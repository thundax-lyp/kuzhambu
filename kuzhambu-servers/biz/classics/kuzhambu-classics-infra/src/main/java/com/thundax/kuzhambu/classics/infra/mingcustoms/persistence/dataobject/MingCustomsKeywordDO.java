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
@TableName("classics_ming_customs_keyword")
public class MingCustomsKeywordDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long customId;
    private String keyword;
    private Integer priority;
}
