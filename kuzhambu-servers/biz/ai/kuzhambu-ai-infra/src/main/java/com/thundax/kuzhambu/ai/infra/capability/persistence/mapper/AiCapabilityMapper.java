package com.thundax.kuzhambu.ai.infra.capability.persistence.mapper;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AiCapabilityMapper extends BaseMapper<AiCapabilityMapper.AiCapabilityDO> {

    @Select("select * from ai_capability_mapping where scope = #{scope} and capability = #{capability}")
    AiCapabilityMappingDO selectMapping(String scope, String capability);

    @Select("select * from ai_capability_mapping where model_id = #{modelId}")
    List<AiCapabilityMappingDO> selectMappingsByModelId(Long modelId);

    @Insert(
            """
            insert into ai_capability_mapping
                (mapping_id, scope, capability, model_id, enabled, configured_at)
            values
                (#{mappingId}, #{scope}, #{capability}, #{modelId}, #{enabled}, #{configuredAt})
            """)
    int insertMapping(AiCapabilityMappingDO dataObject);

    @Update(
            """
            update ai_capability_mapping
            set scope = #{scope},
                capability = #{capability},
                model_id = #{modelId},
                enabled = #{enabled},
                configured_at = #{configuredAt}
            where mapping_id = #{mappingId}
            """)
    int updateMapping(AiCapabilityMappingDO dataObject);

    @Select("select * from ai_action_status where scope = #{scope} and capability = #{capability}")
    AiActionStatusDO selectActionStatus(String scope, String capability);

    @Insert(
            """
            insert into ai_action_status
                (action_status_id, scope, capability, available, unavailable_reason, checked_at)
            values
                (#{actionStatusId}, #{scope}, #{capability}, #{available}, #{unavailableReason}, #{checkedAt})
            """)
    int insertActionStatus(AiActionStatusDO dataObject);

    @Update(
            """
            update ai_action_status
            set available = #{available},
                unavailable_reason = #{unavailableReason},
                checked_at = #{checkedAt}
            where action_status_id = #{actionStatusId}
            """)
    int updateActionStatus(AiActionStatusDO dataObject);

    @Data
    @TableName("ai_capability")
    class AiCapabilityDO {

        @TableId(type = IdType.AUTO)
        private Long id;

        private String capability;
        private String name;
        private String requiredTagsJson;
        private String outputMode;
        private Boolean enabled;
        private Integer priority;
    }

    @Data
    class AiCapabilityMappingDO {

        private Long id;
        private Long mappingId;
        private String scope;
        private String capability;
        private Long modelId;
        private Boolean enabled;
        private Instant configuredAt;
    }

    @Data
    class AiActionStatusDO {

        private Long id;
        private Long actionStatusId;
        private String scope;
        private String capability;
        private Boolean available;
        private String unavailableReason;
        private Instant checkedAt;
    }
}
