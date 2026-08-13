package com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedNodeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GraphPublishedNodeMapper extends BaseMapper<GraphPublishedNodeDO> {

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
