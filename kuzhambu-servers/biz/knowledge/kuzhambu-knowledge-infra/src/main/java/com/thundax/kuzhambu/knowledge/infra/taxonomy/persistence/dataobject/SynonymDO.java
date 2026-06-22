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
@TableName("knowledge_synonym")
public class SynonymDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long synonymId;
    private String term;
    private String synonym;
    private String status;
}
