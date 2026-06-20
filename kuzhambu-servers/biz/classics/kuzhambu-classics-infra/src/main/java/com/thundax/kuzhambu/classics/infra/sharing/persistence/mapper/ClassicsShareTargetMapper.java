package com.thundax.kuzhambu.classics.infra.sharing.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.infra.sharing.persistence.dataobject.ClassicsSharePortalListItemDO;
import com.thundax.kuzhambu.classics.infra.sharing.persistence.dataobject.ClassicsShareTargetDO;
import java.util.Date;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ClassicsShareTargetMapper extends BaseMapper<ClassicsShareTargetDO> {
    @Select({
        "<script>",
        "select",
        "  t.share_link_id as shareLinkId,",
        "  l.title as shareTitle,",
        "  l.issued_at as issuedAt,",
        "  l.expires_at as expiresAt,",
        "  t.content_type as contentType,",
        "  t.content_id as contentId,",
        "  t.content_version_id as contentVersionId,",
        "  t.content_version_no as contentVersionNo,",
        "  t.title_snapshot as titleSnapshot,",
        "  t.content_visibility_snapshot as contentVisibilitySnapshot,",
        "  t.target_status as targetStatus,",
        "  t.priority as priority",
        "from classics_share_target t",
        "inner join classics_share_link l on l.id = t.share_link_id",
        "where l.visibility = #{visibility}",
        "  and l.status = #{linkStatus}",
        "  and t.target_status in (#{targetStatus}, #{legacyTargetStatus})",
        "  and (l.expires_at is null or l.expires_at &gt; now())",
        "<if test='contentType != null and contentType != \"\"'>",
        "  and t.content_type = #{contentType}",
        "</if>",
        "<if test='title != null and title != \"\"'>",
        "  and t.title_snapshot like concat('%', #{title}, '%')",
        "</if>",
        "<if test='issuedAfter != null'>",
        "  and l.issued_at &gt;= #{issuedAfter}",
        "</if>",
        "<if test='issuedBefore != null'>",
        "  and l.issued_at &lt;= #{issuedBefore}",
        "</if>",
        "order by l.issued_at desc, t.priority asc",
        "</script>"
    })
    Page<ClassicsSharePortalListItemDO> pagePortalShares(
            Page<ClassicsSharePortalListItemDO> page,
            @Param("visibility") String visibility,
            @Param("linkStatus") String linkStatus,
            @Param("targetStatus") String targetStatus,
            @Param("legacyTargetStatus") String legacyTargetStatus,
            @Param("contentType") String contentType,
            @Param("title") String title,
            @Param("issuedAfter") Date issuedAfter,
            @Param("issuedBefore") Date issuedBefore);
}
