package com.thundax.kuzhambu.discovery.infra.search.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.discovery.infra.search.persistence.dataobject.SearchClickEventDO;
import java.util.Date;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SearchClickEventMapper extends BaseMapper<SearchClickEventDO> {

    @Select(
            """
            select count(1) from discovery_search_click_event
            where (#{createdAtStart} is null or created_at >= #{createdAtStart})
              and (#{createdAtEnd} is null or created_at <= #{createdAtEnd})
            """)
    Long countByCreatedAtRange(@Param("createdAtStart") Date createdAtStart, @Param("createdAtEnd") Date createdAtEnd);
}
