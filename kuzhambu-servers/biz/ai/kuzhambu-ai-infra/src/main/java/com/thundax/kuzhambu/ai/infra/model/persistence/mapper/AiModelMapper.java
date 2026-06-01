package com.thundax.kuzhambu.ai.infra.model.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.ai.infra.model.persistence.dataobject.AiModelCheckRecordDO;
import com.thundax.kuzhambu.ai.infra.model.persistence.dataobject.AiModelDO;
import com.thundax.kuzhambu.ai.infra.model.persistence.dataobject.AiServiceConfigDO;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AiModelMapper extends BaseMapper<AiModelDO> {

    @Select("select * from ai_service_config where service_id = #{serviceId}")
    AiServiceConfigDO selectServiceConfigByServiceId(Long serviceId);

    @Select("select * from ai_service_config where service_role = #{serviceRole}")
    AiServiceConfigDO selectServiceConfigByRole(String serviceRole);

    @Insert(
            """
            insert into ai_service_config
                (service_id, service_role, api_source, base_url, encrypted_api_key, enabled,
                 status, last_checked_at, configured_at)
            values
                (#{serviceId}, #{serviceRole}, #{apiSource}, #{baseUrl}, #{encryptedApiKey}, #{enabled},
                 #{status}, #{lastCheckedAt}, #{configuredAt})
            """)
    int insertServiceConfig(AiServiceConfigDO dataObject);

    @Update(
            """
            update ai_service_config
            set api_source = #{apiSource},
                base_url = #{baseUrl},
                encrypted_api_key = #{encryptedApiKey},
                enabled = #{enabled},
                status = #{status},
                last_checked_at = #{lastCheckedAt},
                configured_at = #{configuredAt}
            where service_id = #{serviceId}
            """)
    int updateServiceConfig(AiServiceConfigDO dataObject);

    @Insert(
            """
            insert into ai_model_check_record
                (check_id, model_id, service_id, model_name, status, latency_ms,
                 error_type, error_message, checked_at)
            values
                (#{checkId}, #{modelId}, #{serviceId}, #{modelName}, #{status}, #{latencyMs},
                 #{errorType}, #{errorMessage}, #{checkedAt})
            """)
    int insertCheckRecord(AiModelCheckRecordDO dataObject);

    @Select("select * from ai_model_check_record where model_id = #{modelId} order by checked_at desc")
    List<AiModelCheckRecordDO> selectCheckRecordsByModelId(Long modelId);
}
