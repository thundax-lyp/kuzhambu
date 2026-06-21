package com.thundax.kuzhambu.classics.infra.content.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.classics.infra.content.persistence.dataobject.ClassicsContentExportJobDO;
import java.util.Date;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ClassicsContentMapper extends BaseMapper<ClassicsContentExportJobDO> {

    @Update(
            """
            update classics_content_export_job
            set status = #{status},
                storage_object_id = #{storageObjectId},
                expires_at = #{expiresAt},
                item_count = #{itemCount},
                asset_count = #{assetCount}
            where id = #{id}
            """)
    int markExportJobCompleted(
            @Param("id") Long id,
            @Param("status") String status,
            @Param("storageObjectId") Long storageObjectId,
            @Param("expiresAt") Date expiresAt,
            @Param("itemCount") int itemCount,
            @Param("assetCount") int assetCount);

    @Update("update classics_content_export_job set status = #{status} where id = #{id}")
    int markExportJobStatus(@Param("id") Long id, @Param("status") String status);
}
