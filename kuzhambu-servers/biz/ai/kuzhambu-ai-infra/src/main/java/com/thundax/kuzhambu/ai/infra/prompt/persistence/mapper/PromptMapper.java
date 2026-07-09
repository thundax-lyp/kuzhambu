package com.thundax.kuzhambu.ai.infra.prompt.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.ai.infra.prompt.persistence.dataobject.PromptTemplateDO;
import com.thundax.kuzhambu.ai.infra.prompt.persistence.dataobject.PromptVariableDO;
import com.thundax.kuzhambu.ai.infra.prompt.persistence.dataobject.PromptVersionDO;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
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
                (prompt_version_id, template_id, version_no, message_templates_json,
                 variables_snapshot_json, output_schema_json, current_key, change_summary, registered_at)
            values
                (#{promptVersionId}, #{templateId}, #{versionNo}, #{messageTemplatesJson},
                 #{variablesSnapshotJson}, #{outputSchemaJson}, #{currentKey}, #{changeSummary}, #{registeredAt})
            """)
    int insertVersion(PromptVersionDO dataObject);

    @Select("select * from ai_prompt_version where current_key = concat(#{templateId}, ':current')")
    PromptVersionDO selectCurrentVersion(Long templateId);

    @Select("select * from ai_prompt_version where template_id = #{templateId} order by version_no desc")
    List<PromptVersionDO> selectVersions(Long templateId);

    @Update("update ai_prompt_version set current_key = null where template_id = #{templateId}")
    int clearCurrentVersion(Long templateId);

    @Update(
            """
            update ai_prompt_version
            set current_key = concat(#{templateId}, ':current')
            where template_id = #{templateId} and version_no = #{versionNo}
            """)
    int markCurrentVersion(@Param("templateId") Long templateId, @Param("versionNo") int versionNo);

    @Update("update ai_prompt_template set current_version_no = #{versionNo} where template_id = #{templateId}")
    int updateTemplateCurrentVersion(@Param("templateId") Long templateId, @Param("versionNo") int versionNo);

    @Select("select * from ai_prompt_variable where template_id = #{templateId} order by priority asc")
    List<PromptVariableDO> selectVariables(Long templateId);

    @Delete("delete from ai_prompt_variable where template_id = #{templateId}")
    int deleteVariables(Long templateId);

    @Insert(
            """
            insert into ai_prompt_variable
                (variable_id, template_id, variable_name, required, description, priority)
            values
                (#{variableId}, #{templateId}, #{variableName}, #{required}, #{description}, #{priority})
            """)
    int insertVariable(PromptVariableDO dataObject);
}
