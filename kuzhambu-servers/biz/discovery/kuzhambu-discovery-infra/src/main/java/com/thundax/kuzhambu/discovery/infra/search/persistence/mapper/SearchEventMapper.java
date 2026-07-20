package com.thundax.kuzhambu.discovery.infra.search.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.discovery.infra.search.persistence.dataobject.SearchEventDO;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SearchEventMapper extends BaseMapper<SearchEventDO> {

    @Select(
            """
            select * from discovery_search_event
            where (#{createdAtStart} is null or created_at >= #{createdAtStart})
              and (#{createdAtEnd} is null or created_at <= #{createdAtEnd})
            order by created_at desc
            """)
    List<SearchEventDO> selectByCreatedAtRange(
            @Param("createdAtStart") Date createdAtStart, @Param("createdAtEnd") Date createdAtEnd);
}
