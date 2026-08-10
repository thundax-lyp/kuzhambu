package com.thundax.kuzhambu.classics.infra.sancai.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject.SancaiShowcaseDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SancaiShowcaseMapper extends BaseMapper<SancaiShowcaseDO> {
    @Update(
            """
            update classics_sancai_showcase
            set status = #{status},
                storage_object_id = #{storageObjectId},
                entry_count = #{entryCount}
            where id = #{id}
            """)
    int updateShowcaseCompleted(
            @Param("id") Long id,
            @Param("status") String status,
            @Param("storageObjectId") Long storageObjectId,
            @Param("entryCount") int entryCount);

    @Update("update classics_sancai_showcase set status = #{status} where id = #{id}")
    int markShowcaseStatus(@Param("id") Long id, @Param("status") String status);
}
