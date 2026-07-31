package com.thundax.kuzhambu.classics.infra.publication.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.classics.infra.publication.persistence.dataobject.ClassicsPublicationJobDO;
import java.time.Instant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ClassicsPublicationJobMapper extends BaseMapper<ClassicsPublicationJobDO> {
    @Select(
            """
            select * from classics_publication_job
            where content_type = #{contentType} and content_id = #{contentId}
            for update
            """)
    ClassicsPublicationJobDO selectByContentForUpdate(
            @Param("contentType") String contentType, @Param("contentId") Long contentId);

    @Update(
            """
            update classics_publication_job
            set execution_token = #{token}, expires_at = #{expiresAt}
            where id = #{id} and job_result_status = 'RUNNING'
              and job_status != 'CONTENT_COMMITTED'
              and (
                (execution_token is null and expires_at is null and next_retry_at is null)
                or next_retry_at <= #{now}
                or expires_at <= #{now}
              )
            """)
    int claimExecution(
            @Param("id") Long id,
            @Param("token") String token,
            @Param("now") Instant now,
            @Param("expiresAt") Instant expiresAt);

    @Update(
            """
            update classics_publication_job
            set expires_at = #{expiresAt}, attempt_count = attempt_count + 1,
                started_at = coalesce(started_at, #{startedAt})
            where id = #{id} and execution_token = #{token} and job_result_status = 'RUNNING'
            """)
    int markThreadStarted(
            @Param("id") Long id,
            @Param("token") String token,
            @Param("startedAt") Instant startedAt,
            @Param("expiresAt") Instant expiresAt);

    @Update(
            """
            update classics_publication_job
            set job_status = #{nextStatus}, execution_token = null, expires_at = null,
                next_retry_at = null, attempt_count = 0, failure_reason = null,
                content_version_id = coalesce(#{contentVersionId}, content_version_id),
                content_version_no = coalesce(#{contentVersionNo}, content_version_no),
                es_document_id = coalesce(#{esDocumentId}, es_document_id),
                fastgpt_collection_id = coalesce(#{fastGptCollectionId}, fastgpt_collection_id),
                fastgpt_data_ids_json = coalesce(#{fastGptDataIdsJson}, fastgpt_data_ids_json),
                detail_json = #{detailJson}
            where id = #{id} and execution_token = #{token}
              and job_result_status = 'RUNNING' and job_status = #{expectedStatus}
            """)
    int advanceMilestone(
            @Param("id") Long id,
            @Param("token") String token,
            @Param("expectedStatus") String expectedStatus,
            @Param("nextStatus") String nextStatus,
            @Param("contentVersionId") Long contentVersionId,
            @Param("contentVersionNo") Integer contentVersionNo,
            @Param("esDocumentId") String esDocumentId,
            @Param("fastGptCollectionId") String fastGptCollectionId,
            @Param("fastGptDataIdsJson") String fastGptDataIdsJson,
            @Param("detailJson") String detailJson);

    @Update(
            """
            update classics_publication_job
            set fastgpt_collection_id = #{fastGptCollectionId}
            where id = #{id} and execution_token = #{token}
              and job_result_status = 'RUNNING' and job_status = #{expectedStatus}
              and fastgpt_collection_id is null
            """)
    int bindFastGptCollection(
            @Param("id") Long id,
            @Param("token") String token,
            @Param("expectedStatus") String expectedStatus,
            @Param("fastGptCollectionId") String fastGptCollectionId);

    @Update(
            """
            update classics_publication_job
            set execution_token = null, expires_at = null, next_retry_at = #{nextRetryAt},
                failure_reason = #{failureReason}, detail_json = #{detailJson}
            where id = #{id} and execution_token = #{token} and job_result_status = 'RUNNING'
            """)
    int releaseForRetry(
            @Param("id") Long id,
            @Param("token") String token,
            @Param("nextRetryAt") Instant nextRetryAt,
            @Param("failureReason") String failureReason,
            @Param("detailJson") String detailJson);

    @Update(
            """
            update classics_publication_job
            set job_result_status = 'FAILED', execution_token = null, expires_at = null,
                next_retry_at = null, finished_at = #{finishedAt},
                failure_reason = #{failureReason}, detail_json = #{detailJson},
                es_cleanup_status = case when es_document_id is null then es_cleanup_status else 'PENDING' end,
                fastgpt_cleanup_status =
                    case when fastgpt_collection_id is null then fastgpt_cleanup_status else 'PENDING' end
            where id = #{id} and execution_token = #{token}
              and job_result_status = 'RUNNING' and attempt_count >= max_attempts
            """)
    int markTerminalFailure(
            @Param("id") Long id,
            @Param("token") String token,
            @Param("finishedAt") Instant finishedAt,
            @Param("failureReason") String failureReason,
            @Param("detailJson") String detailJson);

    @Update(
            """
            update classics_publication_job
            set es_cleanup_status = 'RUNNING', es_cleanup_token = #{token},
                es_cleanup_expires_at = #{expiresAt}
            where id = #{id} and es_document_id is not null
              and (
                es_cleanup_status in ('PENDING', 'FAILED')
                or (es_cleanup_status = 'RUNNING' and es_cleanup_expires_at <= #{now})
              )
            """)
    int claimEsCleanup(
            @Param("id") Long id,
            @Param("token") String token,
            @Param("now") Instant now,
            @Param("expiresAt") Instant expiresAt);

    @Update(
            """
            update classics_publication_job
            set es_cleanup_status = 'SUCCEEDED', es_document_id = null,
                es_cleanup_token = null, es_cleanup_expires_at = null
            where id = #{id} and es_cleanup_status = 'RUNNING' and es_cleanup_token = #{token}
            """)
    int completeEsCleanup(@Param("id") Long id, @Param("token") String token);

    @Update(
            """
            update classics_publication_job
            set es_cleanup_status = 'FAILED', es_cleanup_token = null,
                es_cleanup_expires_at = null, detail_json = #{detailJson}
            where id = #{id} and es_cleanup_status = 'RUNNING' and es_cleanup_token = #{token}
            """)
    int failEsCleanup(@Param("id") Long id, @Param("token") String token, @Param("detailJson") String detailJson);

    @Update(
            """
            update classics_publication_job
            set fastgpt_cleanup_status = 'RUNNING', fastgpt_cleanup_token = #{token},
                fastgpt_cleanup_expires_at = #{expiresAt}
            where id = #{id} and fastgpt_collection_id is not null
              and (
                fastgpt_cleanup_status in ('PENDING', 'FAILED')
                or (fastgpt_cleanup_status = 'RUNNING' and fastgpt_cleanup_expires_at <= #{now})
              )
            """)
    int claimFastGptCleanup(
            @Param("id") Long id,
            @Param("token") String token,
            @Param("now") Instant now,
            @Param("expiresAt") Instant expiresAt);

    @Update(
            """
            update classics_publication_job
            set fastgpt_cleanup_status = 'SUCCEEDED', fastgpt_collection_id = null,
                fastgpt_data_ids_json = null, fastgpt_cleanup_token = null,
                fastgpt_cleanup_expires_at = null
            where id = #{id} and fastgpt_cleanup_status = 'RUNNING'
              and fastgpt_cleanup_token = #{token}
            """)
    int completeFastGptCleanup(@Param("id") Long id, @Param("token") String token);

    @Update(
            """
            update classics_publication_job
            set fastgpt_cleanup_status = 'FAILED', fastgpt_cleanup_token = null,
                fastgpt_cleanup_expires_at = null, detail_json = #{detailJson}
            where id = #{id} and fastgpt_cleanup_status = 'RUNNING'
              and fastgpt_cleanup_token = #{token}
            """)
    int failFastGptCleanup(@Param("id") Long id, @Param("token") String token, @Param("detailJson") String detailJson);
}
