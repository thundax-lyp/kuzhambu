package com.thundax.kuzhambu.ai.infra.prompt.persistence.mapper;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PromptMapper extends BaseMapper<PromptMapper.PromptTemplateDO> {

    @Select("select * from ai_prompt_template where scope = #{scope} and capability = #{capability}")
    PromptTemplateDO selectTemplateByScope(String scope, String capability);

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
    int markCurrentVersion(Long templateId, int versionNo);

    @Update("update ai_prompt_template set current_version_no = #{versionNo} where template_id = #{templateId}")
    int updateTemplateCurrentVersion(Long templateId, int versionNo);

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

    @Data
    @TableName("ai_prompt_template")
    class PromptTemplateDO {

        @TableId(type = IdType.AUTO)
        private Long id;

        private Long templateId;
        private String scope;
        private String capability;
        private String name;
        private String description;
        private String status;
        private Integer currentVersionNo;
        private Instant registeredAt;
    }

    @Data
    class PromptVersionDO {

        private Long id;
        private Long promptVersionId;
        private Long templateId;
        private Integer versionNo;
        private String messageTemplatesJson;
        private String variablesSnapshotJson;
        private String outputSchemaJson;
        private String currentKey;
        private String changeSummary;
        private Instant registeredAt;
    }

    @Data
    class PromptVariableDO {

        private Long id;
        private Long variableId;
        private Long templateId;
        private String variableName;
        private Boolean required;
        private String description;
        private Integer priority;
    }
}
