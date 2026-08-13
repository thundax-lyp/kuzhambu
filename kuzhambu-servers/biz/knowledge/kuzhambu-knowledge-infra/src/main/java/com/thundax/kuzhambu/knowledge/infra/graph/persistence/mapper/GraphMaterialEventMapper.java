package com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialEventDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GraphMaterialEventMapper extends BaseMapper<GraphMaterialEventDO> {

    @Update(
            """
            update knowledge_graph_material_event
            set status = #{row.status},
                changed_at = #{row.changedAt},
                lock_version = lock_version + 1
            where id = #{row.id}
              and lock_version = #{expectedLockVersion}
            """)
    int updateIfLockVersion(
            @Param("row") GraphMaterialEventDO row, @Param("expectedLockVersion") Long expectedLockVersion);
}
