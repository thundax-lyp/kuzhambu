package com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaSessionDO;
import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface QaSessionMapper extends BaseMapper<QaSessionDO> {

    @Select(
            """
            select * from discovery_qa_session
            where (#{openedAtStart} is null or opened_at >= #{openedAtStart})
              and (#{openedAtEnd} is null or opened_at <= #{openedAtEnd})
            order by opened_at desc
            """)
    List<QaSessionDO> selectByOpenedAtRange(
            @Param("openedAtStart") Instant openedAtStart, @Param("openedAtEnd") Instant openedAtEnd);

    @Update(
            """
            update discovery_qa_session
            set status = 'REMOVED',
                removed_at = #{removedAt}
            where id = #{id}
              and removed_at is null
            """)
    int markRemoved(@Param("id") Long id, @Param("removedAt") Instant removedAt);
}
