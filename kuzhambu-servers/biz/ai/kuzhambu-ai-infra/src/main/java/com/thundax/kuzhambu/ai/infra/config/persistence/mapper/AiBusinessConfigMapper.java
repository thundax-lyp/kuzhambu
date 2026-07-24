package com.thundax.kuzhambu.ai.infra.config.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.AiBusinessConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AiBusinessConfigMapper extends BaseMapper<AiBusinessConfigDO> {

    @Select(
            """
            select * from ai_business_config
            where capability = #{capability}
            and enabled = 1
            order by priority asc, id asc
            limit 1
            """)
    AiBusinessConfigDO selectByCapability(String capability);
}
