package com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.dataobject;

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
@TableName("knowledge_tag")
public class TagDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long tagId;
    private String name;
    private Long categoryId;
    private String description;
    private String status;
    private String source;
    private String reviewStatus;
    private String reviewNote;
    private Date createdAt;
    private Date reviewedAt;
    private Long mergedToTagId;
}
