package com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedAdjacencyDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedNodeDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GraphPublishedNodeMapper extends BaseMapper<GraphPublishedNodeDO> {

    @Select(
            """
            <script>
            select count(1)
            from knowledge_graph_published_node s
            left join knowledge_graph_published_edge p
              on s.id = p.source_published_node_id
              <if test="relationType != null and relationType != ''">
              and p.relation_type = #{relationType}
              </if>
              <if test="relationStatus != null and relationStatus != ''">
              and p.status = #{relationStatus}
              </if>
              <if test="relationSource != null and relationSource != ''">
              and p.source = #{relationSource}
              </if>
              <if test="(objectKeyword != null and objectKeyword != '')
                      or (objectType != null and objectType != '')
                      or (objectStatus != null and objectStatus != '')
                      or (objectSource != null and objectSource != '')">
              and exists (
                  select 1
                  from knowledge_graph_published_node object_filter
                  where object_filter.id = p.target_published_node_id
                  <if test="objectKeyword != null and objectKeyword != ''">
                  and object_filter.name like concat('%', #{objectKeyword}, '%')
                  </if>
                  <if test="objectType != null and objectType != ''">
                  and object_filter.node_type = #{objectType}
                  </if>
                  <if test="objectStatus != null and objectStatus != ''">
                  and object_filter.status = #{objectStatus}
                  </if>
                  <if test="objectSource != null and objectSource != ''">
                  and object_filter.source = #{objectSource}
                  </if>
              )
              </if>
            left join knowledge_graph_published_node o
              on p.target_published_node_id = o.id
            where 1 = 1
              <if test="subjectNodeId != null">
              and s.id = #{subjectNodeId}
              </if>
              <if test="subjectKeyword != null and subjectKeyword != ''">
              and s.name like concat('%', #{subjectKeyword}, '%')
              </if>
              <if test="subjectType != null and subjectType != ''">
              and s.node_type = #{subjectType}
              </if>
              <if test="subjectStatus != null and subjectStatus != ''">
              and s.status = #{subjectStatus}
              </if>
              <if test="subjectSource != null and subjectSource != ''">
              and s.source = #{subjectSource}
              </if>
              <if test="includeIsolated == false">
              and p.id is not null
              </if>
            </script>
            """)
    long countAdjacency(
            @Param("subjectNodeId") Long subjectNodeId,
            @Param("subjectKeyword") String subjectKeyword,
            @Param("subjectType") String subjectType,
            @Param("subjectStatus") String subjectStatus,
            @Param("subjectSource") String subjectSource,
            @Param("relationType") String relationType,
            @Param("relationStatus") String relationStatus,
            @Param("relationSource") String relationSource,
            @Param("objectKeyword") String objectKeyword,
            @Param("objectType") String objectType,
            @Param("objectStatus") String objectStatus,
            @Param("objectSource") String objectSource,
            @Param("includeIsolated") boolean includeIsolated);

    @Select(
            """
            <script>
            select s.id as subjectId,
                   s.node_key as subjectNodeKey,
                   s.node_type as subjectNodeType,
                   s.name as subjectName,
                   s.source as subjectSource,
                   s.status as subjectStatus,
                   s.modified_at as subjectModifiedAt,
                   s.lock_version as subjectLockVersion,
                   p.id as relationId,
                   p.edge_key as relationEdgeKey,
                   p.source_published_node_id as relationSourcePublishedNodeId,
                   p.target_published_node_id as relationTargetPublishedNodeId,
                   p.relation_type as relationType,
                   p.source as relationSource,
                   p.qualifiers_json as relationQualifiersJson,
                   p.status as relationStatus,
                   p.modified_at as relationModifiedAt,
                   p.lock_version as relationLockVersion,
                   o.id as objectId,
                   o.node_key as objectNodeKey,
                   o.node_type as objectNodeType,
                   o.name as objectName,
                   o.source as objectSource,
                   o.status as objectStatus,
                   o.modified_at as objectModifiedAt,
                   o.lock_version as objectLockVersion
            from knowledge_graph_published_node s
            left join knowledge_graph_published_edge p
              on s.id = p.source_published_node_id
              <if test="relationType != null and relationType != ''">
              and p.relation_type = #{relationType}
              </if>
              <if test="relationStatus != null and relationStatus != ''">
              and p.status = #{relationStatus}
              </if>
              <if test="relationSource != null and relationSource != ''">
              and p.source = #{relationSource}
              </if>
              <if test="(objectKeyword != null and objectKeyword != '')
                      or (objectType != null and objectType != '')
                      or (objectStatus != null and objectStatus != '')
                      or (objectSource != null and objectSource != '')">
              and exists (
                  select 1
                  from knowledge_graph_published_node object_filter
                  where object_filter.id = p.target_published_node_id
                  <if test="objectKeyword != null and objectKeyword != ''">
                  and object_filter.name like concat('%', #{objectKeyword}, '%')
                  </if>
                  <if test="objectType != null and objectType != ''">
                  and object_filter.node_type = #{objectType}
                  </if>
                  <if test="objectStatus != null and objectStatus != ''">
                  and object_filter.status = #{objectStatus}
                  </if>
                  <if test="objectSource != null and objectSource != ''">
                  and object_filter.source = #{objectSource}
                  </if>
              )
              </if>
            left join knowledge_graph_published_node o
              on p.target_published_node_id = o.id
            where 1 = 1
              <if test="subjectNodeId != null">
              and s.id = #{subjectNodeId}
              </if>
              <if test="subjectKeyword != null and subjectKeyword != ''">
              and s.name like concat('%', #{subjectKeyword}, '%')
              </if>
              <if test="subjectType != null and subjectType != ''">
              and s.node_type = #{subjectType}
              </if>
              <if test="subjectStatus != null and subjectStatus != ''">
              and s.status = #{subjectStatus}
              </if>
              <if test="subjectSource != null and subjectSource != ''">
              and s.source = #{subjectSource}
              </if>
              <if test="includeIsolated == false">
              and p.id is not null
              </if>
            order by s.modified_at desc, s.id desc, p.id asc
            limit #{offset}, #{limit}
            </script>
            """)
    List<GraphPublishedAdjacencyDO> listAdjacency(
            @Param("subjectNodeId") Long subjectNodeId,
            @Param("subjectKeyword") String subjectKeyword,
            @Param("subjectType") String subjectType,
            @Param("subjectStatus") String subjectStatus,
            @Param("subjectSource") String subjectSource,
            @Param("relationType") String relationType,
            @Param("relationStatus") String relationStatus,
            @Param("relationSource") String relationSource,
            @Param("objectKeyword") String objectKeyword,
            @Param("objectType") String objectType,
            @Param("objectStatus") String objectStatus,
            @Param("objectSource") String objectSource,
            @Param("includeIsolated") boolean includeIsolated,
            @Param("offset") int offset,
            @Param("limit") int limit);

    @Update(
            """
            update knowledge_graph_published_node
            set node_key = #{row.nodeKey},
                node_type = #{row.nodeType},
                name = #{row.name},
                source = #{row.source},
                status = #{row.status},
                modified_at = #{row.modifiedAt},
                lock_version = lock_version + 1
            where id = #{row.id}
              and lock_version = #{expectedLockVersion}
            """)
    int updateIfLockVersion(
            @Param("row") GraphPublishedNodeDO row, @Param("expectedLockVersion") Long expectedLockVersion);
}
