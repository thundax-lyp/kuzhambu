package com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper;

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
              and not exists (
                  select 1
                  from knowledge_graph_published_edge e
                  where e.status = 'ACTIVE'
                    and e.relation_type in ('RELATED_TO', 'PART_OF', 'LOCATED_IN')
                    and (e.source_published_node_id = n.id or e.target_published_node_id = n.id)
              )
            </script>
            """)
    long countMissingCoreRelationNodes(@Param("nodeType") String nodeType);

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
              and not exists (
                  select 1
                  from knowledge_graph_published_edge e
                  where e.status = 'ACTIVE'
                    and e.relation_type in ('RELATED_TO', 'PART_OF', 'LOCATED_IN')
                    and (e.source_published_node_id = n.id or e.target_published_node_id = n.id)
              )
            order by n.modified_at desc, n.id desc
            limit #{limit}
            </script>
            """)
    List<com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedNodeDO>
            listMissingCoreRelationNodes(@Param("nodeType") String nodeType, @Param("limit") int limit);

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
}
