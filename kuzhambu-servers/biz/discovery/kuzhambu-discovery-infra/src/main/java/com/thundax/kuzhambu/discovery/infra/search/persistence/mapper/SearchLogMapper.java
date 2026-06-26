package com.thundax.kuzhambu.discovery.infra.search.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.discovery.infra.search.persistence.dataobject.SearchLogDO;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SearchLogMapper extends BaseMapper<SearchLogDO> {

    @Select(
            """
            select * from discovery_search_log
            where (#{createdAtStart} is null or created_at >= #{createdAtStart})
              and (#{createdAtEnd} is null or created_at <= #{createdAtEnd})
            order by created_at desc
            """)
    List<SearchLogDO> selectByCreatedAtRange(Date createdAtStart, Date createdAtEnd);
}
