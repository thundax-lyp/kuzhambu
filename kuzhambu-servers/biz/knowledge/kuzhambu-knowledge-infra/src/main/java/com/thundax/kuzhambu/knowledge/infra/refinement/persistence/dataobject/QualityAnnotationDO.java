package com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_quality_annotation")
public class QualityAnnotationDO {
    @TableId(type = IdType.INPUT)
    private Long id;

    private Long annotationId;
    private String objectType;
    private String objectKey;
    private String sourceContentType;
    private Long sourceContentId;
    private Long graphVersionId;
    private String annotationStatus;
    private String annotationLabel;
    private String comment;
    private Long createdBy;
    private Instant createdAt;
    private Long updatedBy;
    private Instant updatedAt;
}
