package com.thundax.kuzhambu.ai.infra.config.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.PromptTemplateDO;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.PromptVariableDO;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.PromptVersionDO;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PromptMapper extends BaseMapper<PromptTemplateDO> {

    @Select("select * from ai_prompt_template where scope = #{scope} and capability = #{capability}")
    PromptTemplateDO selectTemplateByScope(@Param("scope") String scope, @Param("capability") String capability);

    @Insert(
            """
            insert into ai_prompt_version
                (template_id, version_no, message_templates_json,
                 variables_snapshot_json, output_schema_json, change_summary, registered_at)
            values
                (#{templateId}, #{versionNo}, #{messageTemplatesJson},
                 #{variablesSnapshotJson}, #{outputSchemaJson}, #{changeSummary}, #{registeredAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertVersion(PromptVersionDO dataObject);

    @Select(
            """
            select v.*
            from ai_prompt_version v
            join ai_prompt_template t on t.id = v.template_id
            where v.template_id = #{templateId} and v.version_no = t.current_version_no
            """)
    PromptVersionDO selectCurrentVersion(Long templateId);

    @Select("select * from ai_prompt_version where template_id = #{templateId} order by version_no desc")
    List<PromptVersionDO> selectVersions(Long templateId);

    @Update(
            """
            update ai_prompt_template t
            set t.current_version_no = #{versionNo}
            where t.id = #{templateId}
              and exists (
                  select 1
                  from ai_prompt_version v
                  where v.template_id = #{templateId} and v.version_no = #{versionNo}
              )
            """)
    int markCurrentVersion(@Param("templateId") Long templateId, @Param("versionNo") int versionNo);

    @Select("select * from ai_prompt_variable where template_id = #{templateId} order by priority asc")
    List<PromptVariableDO> selectVariables(Long templateId);

    @Delete("delete from ai_prompt_variable where template_id = #{templateId}")
    int deleteVariables(Long templateId);

    @Insert(
            """
            insert into ai_prompt_variable
                (template_id, variable_name, required, description, priority)
            values
                (#{templateId}, #{variableName}, #{required}, #{description}, #{priority})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertVariable(PromptVariableDO dataObject);
}
