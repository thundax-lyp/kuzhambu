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
@TableName("knowledge_tag_category")
public class TagCategoryDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long categoryId;
    private String name;
    private String description;
    private Integer priority;
    private String status;
}
