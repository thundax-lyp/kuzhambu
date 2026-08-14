package com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialDeletionChangeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GraphMaterialDeletionChangeMapper extends BaseMapper<GraphMaterialDeletionChangeDO> {

    @Update(
            """
            update knowledge_graph_material_deletion_change
            set decision = #{row.decision},
                status = #{row.status},
                result_summary_json = #{row.resultSummaryJson},
                completed_at = #{row.completedAt},
                lock_version = lock_version + 1
            where id = #{row.id}
              and lock_version = #{expectedLockVersion}
            """)
    int updateIfLockVersion(
            @Param("row") GraphMaterialDeletionChangeDO row, @Param("expectedLockVersion") Long expectedLockVersion);
}
