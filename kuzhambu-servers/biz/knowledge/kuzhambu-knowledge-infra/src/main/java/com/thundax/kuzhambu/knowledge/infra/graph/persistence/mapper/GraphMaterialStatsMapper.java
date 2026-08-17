package com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialStatsDO;
import java.time.Instant;
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

    @Insert(
            """
            insert into knowledge_graph_material_stats (
                material_id, draft_node_count, draft_edge_count, published_node_count, published_edge_count,
                active_task_count, pending_review_task_count, failed_task_count, stats_revision, calculated_at)
            select
                m.id,
                coalesce(dn.draft_node_count, 0),
                coalesce(de.draft_edge_count, 0),
                coalesce(pn.published_node_count, 0),
                coalesce(pe.published_edge_count, 0),
                coalesce(t.active_task_count, 0),
                coalesce(t.pending_review_task_count, 0),
                coalesce(t.failed_task_count, 0),
                coalesce(s.stats_revision, 0) + 1,
                #{calculatedAt}
            from knowledge_graph_material m
            left join knowledge_graph_material_stats s on s.material_id = m.id
            left join (
                select material_id, count(*) as draft_node_count
                from knowledge_graph_material_node
                where material_id = #{materialId}
                group by material_id
            ) dn on dn.material_id = m.id
            left join (
                select material_id, count(*) as draft_edge_count
                from knowledge_graph_material_edge
                where material_id = #{materialId}
                group by material_id
            ) de on de.material_id = m.id
            left join (
                select m2.id as material_id, count(nm.published_node_id) as published_node_count
                from knowledge_graph_material m2
                left join knowledge_graph_published_node_material nm
                  on nm.content_type = m2.content_type and nm.content_ref_id = m2.content_ref_id
                where m2.id = #{materialId}
                group by m2.id
            ) pn on pn.material_id = m.id
            left join (
                select m2.id as material_id, count(em.published_edge_id) as published_edge_count
                from knowledge_graph_material m2
                left join knowledge_graph_published_edge_material em
                  on em.content_type = m2.content_type and em.content_ref_id = m2.content_ref_id
                where m2.id = #{materialId}
                group by m2.id
            ) pe on pe.material_id = m.id
            left join (
                select
                    material_id,
                    sum(case when execution_status in ('PENDING', 'RUNNING') then 1 else 0 end) as active_task_count,
                    sum(case when execution_status = 'SUCCEEDED' and disposition = 'PENDING' then 1 else 0 end)
                        as pending_review_task_count,
                    sum(case when execution_status = 'FAILED' then 1 else 0 end) as failed_task_count
                from knowledge_graph_extraction_task
                where material_id = #{materialId}
                group by material_id
            ) t on t.material_id = m.id
            where m.id = #{materialId}
            on duplicate key update
                draft_node_count = values(draft_node_count),
                draft_edge_count = values(draft_edge_count),
                published_node_count = values(published_node_count),
                published_edge_count = values(published_edge_count),
                active_task_count = values(active_task_count),
                pending_review_task_count = values(pending_review_task_count),
                failed_task_count = values(failed_task_count),
                stats_revision = knowledge_graph_material_stats.stats_revision + 1,
                calculated_at = values(calculated_at)
            """)
    int refresh(@Param("materialId") Long materialId, @Param("calculatedAt") Instant calculatedAt);
}
