package com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_tag_alias")
public class TagAliasDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long aliasId;
    private Long tagId;
    private String name;
    private String source;
}
