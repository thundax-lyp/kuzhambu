package com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphGovernanceImpactTokenDO;
import java.time.Instant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GraphGovernanceImpactTokenMapper extends BaseMapper<GraphGovernanceImpactTokenDO> {

    @Update(
            """
            update knowledge_graph_governance_impact_token
            set consumed_at = #{consumedAt}
            where token = #{token}
              and consumed_at is null
              and expires_at > #{consumedAt}
            """)
    int consumeIfAvailable(@Param("token") String token, @Param("consumedAt") Instant consumedAt);
}
