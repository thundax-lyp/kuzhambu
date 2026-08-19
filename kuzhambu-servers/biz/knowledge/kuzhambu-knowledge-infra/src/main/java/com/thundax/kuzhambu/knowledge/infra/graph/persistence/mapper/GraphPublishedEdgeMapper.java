package com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedEdgeDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GraphPublishedEdgeMapper extends BaseMapper<GraphPublishedEdgeDO> {

    @Select(
            """
            select *
            from knowledge_graph_published_edge
            where status = 'ACTIVE'
            order by modified_at desc, id desc
            limit #{limit}
            """)
    List<GraphPublishedEdgeDO> listRecentlyUpdated(@Param("limit") int limit);

    @Select(
            """
            <script>
            select *
            from (
                select *
                from knowledge_graph_published_edge
                where source_published_node_id in
                <foreach collection="nodeIds" item="nodeId" open="(" separator="," close=")">
                    #{nodeId}
                </foreach>
                and status = 'ACTIVE'
                <if test="afterEdgeId != null">
                    and id &gt; #{afterEdgeId}
                </if>
                union
                select *
                from knowledge_graph_published_edge
                where target_published_node_id in
                <foreach collection="nodeIds" item="nodeId" open="(" separator="," close=")">
                    #{nodeId}
                </foreach>
                and status = 'ACTIVE'
                <if test="afterEdgeId != null">
                    and id &gt; #{afterEdgeId}
                </if>
            ) incident_edges
            order by id asc
            limit #{limit}
            </script>
            """)
    List<GraphPublishedEdgeDO> listOneHopEdges(
            @Param("nodeIds") List<Long> nodeIds, @Param("afterEdgeId") Long afterEdgeId, @Param("limit") int limit);

    @Update(
            """
            update knowledge_graph_published_edge
            set edge_key = #{row.edgeKey},
                source_published_node_id = #{row.sourcePublishedNodeId},
                target_published_node_id = #{row.targetPublishedNodeId},
                relation_type = #{row.relationType},
                source = #{row.source},
                qualifiers_json = #{row.qualifiersJson},
                status = #{row.status},
                modified_at = #{row.modifiedAt},
                lock_version = lock_version + 1
            where id = #{row.id}
              and lock_version = #{expectedLockVersion}
            """)
    int updateIfLockVersion(
            @Param("row") GraphPublishedEdgeDO row, @Param("expectedLockVersion") Long expectedLockVersion);
}
