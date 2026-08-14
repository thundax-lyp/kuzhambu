package com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialDeletionTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GraphMaterialDeletionTaskMapper extends BaseMapper<GraphMaterialDeletionTaskDO> {

    @Update(
            """
            update knowledge_graph_material_deletion_task
            set status = #{row.status},
                progress = #{row.progress},
                failure_reason = #{row.failureReason},
                result_summary_json = #{row.resultSummaryJson},
                completed_at = #{row.completedAt},
                lock_version = lock_version + 1
            where id = #{row.id}
              and lock_version = #{expectedLockVersion}
            """)
    int updateIfLockVersion(
            @Param("row") GraphMaterialDeletionTaskDO row, @Param("expectedLockVersion") Long expectedLockVersion);
}
