package com.thundax.kuzhambu.ai.infra.capability.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.ai.infra.capability.persistence.dataobject.AiActionStatusDO;
import com.thundax.kuzhambu.ai.infra.capability.persistence.dataobject.AiCapabilityDO;
import com.thundax.kuzhambu.ai.infra.capability.persistence.dataobject.AiCapabilityMappingDO;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AiCapabilityMapper extends BaseMapper<AiCapabilityDO> {

    @Select("select * from ai_capability_mapping where scope = #{scope} and capability = #{capability}")
    AiCapabilityMappingDO selectMapping(String scope, String capability);

    @Select(
            """
            <script>
            select * from ai_capability_mapping
            where 1 = 1
            <if test="scope != null and scope != ''">
                and scope = #{scope}
            </if>
            <if test="capability != null and capability != ''">
                and capability = #{capability}
            </if>
            <if test="enabled != null">
                and enabled = #{enabled}
            </if>
            order by scope asc, capability asc
            </script>
            """)
    List<AiCapabilityMappingDO> selectMappings(String scope, String capability, Boolean enabled);

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

    @Select(
            """
            <script>
            select * from ai_action_status
            where 1 = 1
            <if test="scope != null and scope != ''">
                and scope = #{scope}
            </if>
            <if test="capability != null and capability != ''">
                and capability = #{capability}
            </if>
            <if test="available != null">
                and available = #{available}
            </if>
            order by scope asc, capability asc
            </script>
            """)
    List<AiActionStatusDO> selectActionStatuses(String scope, String capability, Boolean available);

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
}
