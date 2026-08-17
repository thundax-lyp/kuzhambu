package com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialStatsDO;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GraphMaterialStatsMapper extends BaseMapper<GraphMaterialStatsDO> {

    @Select(
            """
            <script>
            select *
            from knowledge_graph_material_stats
            where material_id in
            <foreach collection="materialIds" item="materialId" open="(" separator="," close=")">
              #{materialId}
            </foreach>
            </script>
            """)
    List<GraphMaterialStatsDO> selectByMaterialIds(@Param("materialIds") List<Long> materialIds);

    @Insert(
            """
            insert into knowledge_graph_material_stats (
                material_id, draft_node_count, draft_edge_count, published_node_count, published_edge_count,
                active_task_count, pending_review_task_count, failed_task_count, stats_revision, calculated_at)
            values (
                #{row.materialId}, #{row.draftNodeCount}, #{row.draftEdgeCount}, #{row.publishedNodeCount}, #{row.publishedEdgeCount},
                #{row.activeTaskCount}, #{row.pendingReviewTaskCount}, #{row.failedTaskCount}, #{row.statsRevision}, #{row.calculatedAt})
            on duplicate key update
                draft_node_count = values(draft_node_count),
                draft_edge_count = values(draft_edge_count),
                published_node_count = values(published_node_count),
                published_edge_count = values(published_edge_count),
                active_task_count = values(active_task_count),
                pending_review_task_count = values(pending_review_task_count),
                failed_task_count = values(failed_task_count),
                stats_revision = values(stats_revision),
                calculated_at = values(calculated_at)
            """)
    int upsert(@org.apache.ibatis.annotations.Param("row") GraphMaterialStatsDO row);
}
