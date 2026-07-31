package com.thundax.kuzhambu.classics.infra.publication.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("classics_publication_job")
public class ClassicsPublicationJobDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String jobType;
    private String contentType;
    private Long contentId;
    private String contentTitleSnapshot;
    private Instant contentDeletedAt;
    private String sourceLifecycleStatus;
    private String targetLifecycleStatus;
    private Long contentVersionId;
    private Integer contentVersionNo;
    private String jobStatus;
    private String jobResultStatus;
    private String executionToken;
    private Instant expiresAt;
    private Instant nextRetryAt;
    private Integer attemptCount;
    private Integer maxAttempts;
    private String esDocumentId;

    @TableField("fastgpt_collection_id")
    private String fastGptCollectionId;

    @TableField("fastgpt_data_ids_json")
    private String fastGptDataIdsJson;

    private String esCleanupStatus;
    private String esCleanupToken;
    private Instant esCleanupExpiresAt;

    @TableField("fastgpt_cleanup_status")
    private String fastGptCleanupStatus;

    @TableField("fastgpt_cleanup_token")
    private String fastGptCleanupToken;

    @TableField("fastgpt_cleanup_expires_at")
    private Instant fastGptCleanupExpiresAt;

    private String detailJson;
    private Instant requestedAt;
    private Instant startedAt;
    private Instant finishedAt;
    private String failureReason;
}
