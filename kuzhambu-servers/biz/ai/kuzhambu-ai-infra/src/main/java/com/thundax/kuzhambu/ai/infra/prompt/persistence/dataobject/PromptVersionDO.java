package com.thundax.kuzhambu.ai.infra.prompt.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_prompt_version")
public class PromptVersionDO {

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
