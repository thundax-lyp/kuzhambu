package com.thundax.kuzhambu.discovery.infra.search.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.discovery.infra.search.persistence.dataobject.SearchClickDO;
import java.util.Date;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SearchClickMapper extends BaseMapper<SearchClickDO> {

    @Select(
            """
            select count(1) from discovery_search_click
            where (#{createdAtStart} is null or created_at >= #{createdAtStart})
              and (#{createdAtEnd} is null or created_at <= #{createdAtEnd})
            """)
    Long countByCreatedAtRange(Date createdAtStart, Date createdAtEnd);
}
