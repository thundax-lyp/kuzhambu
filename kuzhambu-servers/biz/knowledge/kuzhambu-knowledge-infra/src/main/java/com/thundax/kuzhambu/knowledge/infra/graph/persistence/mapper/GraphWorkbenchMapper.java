package com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper;

import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphCoreRelationPolicy;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GraphWorkbenchMapper {

    @Select("select count(*) from knowledge_graph_published_node where status = 'ACTIVE'")
    long countActiveNodes();

    @Select("select count(*) from knowledge_graph_published_edge where status = 'ACTIVE'")
    long countActiveEdges();

    @Select(
            """
            select count(*)
            from (
                select content_type, content_ref_id from knowledge_graph_published_node_material
                union
                select content_type, content_ref_id from knowledge_graph_published_edge_material
            ) covered_material
            """)
    long countCoveredMaterials();

    @Select(
            """
            <script>
            select count(*)
            from knowledge_graph_published_node n
            where n.status = 'ACTIVE'
              <if test="nodeType != null">
                and n.node_type = #{nodeType}
              </if>
              and not exists (
                  select 1
                  from knowledge_graph_published_edge e
                  where e.status = 'ACTIVE'
                    and (e.source_published_node_id = n.id or e.target_published_node_id = n.id)
              )
            </script>
            """)
    long countIsolatedNodes(@Param("nodeType") String nodeType);

    @Select(
            """
            <script>
            select count(*)
            from knowledge_graph_published_node n
            where n.status = 'ACTIVE'
              <if test="nodeType != null">
                and n.node_type = #{nodeType}
              </if>
              and (
                <foreach collection="policies" item="policy" separator=" or ">
                  (n.node_type = #{policy.nodeType} and not exists (
                    select 1 from knowledge_graph_published_edge e
                    where e.status = 'ACTIVE'
                      and e.relation_type in <foreach collection="policy.relationTypes" item="relationType" open="(" separator="," close=")">#{relationType}</foreach>
                      and (e.source_published_node_id = n.id or e.target_published_node_id = n.id)))
                </foreach>
              )
            </script>
            """)
    long countMissingCoreRelationNodes(
            @Param("nodeType") String nodeType, @Param("policies") List<GraphCoreRelationPolicy> policies);

    @Select(
            """
            select type, contentType, contentRefId, occurredAt, summary
            from (
                select
                  'PUBLICATION' as type,
                  m.content_type as contentType,
                  m.content_ref_id as contentRefId,
                  coalesce(r.completed_at, r.requested_at) as occurredAt,
                  concat('发布素材 ', m.content_title_snapshot, ' ', r.status) as summary
                from knowledge_graph_publish_record r
                join knowledge_graph_material m on m.id = r.material_id
                union all
                select
                  concat('GOVERNANCE_', o.operation_type) as type,
                  null as contentType,
                  null as contentRefId,
                  o.operated_at as occurredAt,
                  concat(o.target_type, '#', o.target_id, ' ', o.operation_type) as summary
                from knowledge_graph_governance_operation o
                union all
                select
                  'DELETION' as type,
                  c.content_type as contentType,
                  c.content_ref_id as contentRefId,
                  coalesce(c.completed_at, c.requested_at) as occurredAt,
                  concat('素材删除变更 ', c.status) as summary
                from knowledge_graph_material_deletion_change c
            ) activity
            order by occurredAt desc
            limit #{limit}
            """)
    List<ActivityRow> listRecentActivities(@Param("limit") int limit);

    @Select(
            """
            select count(*)
            from knowledge_graph_publication_preview_token
            where consumed_at is null
              and expires_at > cast(unix_timestamp(current_timestamp(3)) * 1000 as unsigned)
              and json_search(
                    snapshot_json,
                    'one',
                    'CONFLICT',
                    null,
                    '$.nodes[*].matchType',
                    '$.edges[*].matchType') is not null
            """)
    long countPendingPublicationConflicts();

    @Select(
            """
            <script>
            select n.*
            from knowledge_graph_published_node n
            where n.status = 'ACTIVE'
              <if test="nodeType != null">
                and n.node_type = #{nodeType}
              </if>
              and not exists (
                  select 1
                  from knowledge_graph_published_edge e
                  where e.status = 'ACTIVE'
                    and (e.source_published_node_id = n.id or e.target_published_node_id = n.id)
              )
            order by n.modified_at desc, n.id desc
            limit #{limit}
            </script>
            """)
    List<com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedNodeDO> listIsolatedNodes(
            @Param("nodeType") String nodeType, @Param("limit") int limit);

    @Select(
            """
            <script>
            select n.*
            from knowledge_graph_published_node n
            where n.status = 'ACTIVE'
              <if test="nodeType != null">
                and n.node_type = #{nodeType}
              </if>
              and (
                <foreach collection="policies" item="policy" separator=" or ">
                  (n.node_type = #{policy.nodeType} and not exists (
                    select 1 from knowledge_graph_published_edge e
                    where e.status = 'ACTIVE'
                      and e.relation_type in <foreach collection="policy.relationTypes" item="relationType" open="(" separator="," close=")">#{relationType}</foreach>
                      and (e.source_published_node_id = n.id or e.target_published_node_id = n.id)))
                </foreach>
              )
            order by n.modified_at desc, n.id desc
            limit #{limit}
            </script>
            """)
    List<com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedNodeDO>
            listMissingCoreRelationNodes(
                    @Param("nodeType") String nodeType,
                    @Param("limit") int limit,
                    @Param("policies") List<GraphCoreRelationPolicy> policies);

    @Select(
            """
            <script>
            select count(*)
            from (
                select 'NODE' as objectType, n.id as objectId
                from knowledge_graph_published_node n
                where n.status = 'ACTIVE'
                  <if test="keyword != null and keyword != ''">
                    and n.name like concat('%', #{keyword}, '%')
                  </if>
                  <if test="nodeType != null">
                    and n.node_type = #{nodeType}
                  </if>
                union all
                select 'EDGE' as objectType, e.id as objectId
                from knowledge_graph_published_edge e
                where e.status = 'ACTIVE'
                  <if test="keyword != null and keyword != ''">
                    and (e.edge_key like concat('%', #{keyword}, '%')
                         or e.relation_type like concat('%', #{keyword}, '%'))
                  </if>
                  <if test="relationType != null and relationType != ''">
                    and e.relation_type = #{relationType}
                  </if>
            ) search_hit
            </script>
            """)
    long countSearchHits(
            @Param("keyword") String keyword,
            @Param("nodeType") String nodeType,
            @Param("relationType") String relationType);

    @Select(
            """
            <script>
            select objectType, objectId
            from (
                select 'NODE' as objectType, n.id as objectId, n.modified_at as modified_at
                from knowledge_graph_published_node n
                where n.status = 'ACTIVE'
                  <if test="keyword != null and keyword != ''">
                    and n.name like concat('%', #{keyword}, '%')
                  </if>
                  <if test="nodeType != null">
                    and n.node_type = #{nodeType}
                  </if>
                union all
                select 'EDGE' as objectType, e.id as objectId, e.modified_at as modified_at
                from knowledge_graph_published_edge e
                where e.status = 'ACTIVE'
                  <if test="keyword != null and keyword != ''">
                    and (e.edge_key like concat('%', #{keyword}, '%')
                         or e.relation_type like concat('%', #{keyword}, '%'))
                  </if>
                  <if test="relationType != null and relationType != ''">
                    and e.relation_type = #{relationType}
                  </if>
            ) search_hit
            order by modified_at desc, objectId desc
            limit #{pageSize} offset #{offset}
            </script>
            """)
    List<SearchHitRow> searchHits(
            @Param("keyword") String keyword,
            @Param("nodeType") String nodeType,
            @Param("relationType") String relationType,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    @Getter
    @Setter
    class SearchHitRow {
        private String objectType;
        private Long objectId;
    }

    @Getter
    @Setter
    class ActivityRow {
        private String type;
        private String contentType;
        private Long contentRefId;
        private Instant occurredAt;
        private String summary;
    }
}
