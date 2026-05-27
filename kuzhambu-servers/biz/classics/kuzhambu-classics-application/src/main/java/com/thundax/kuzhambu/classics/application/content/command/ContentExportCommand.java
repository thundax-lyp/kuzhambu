package com.thundax.kuzhambu.classics.application.content.command;

import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportFormat;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportKind;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportScopeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContentExportCommand {
    private ClassicsExportKind exportKind;
    private ClassicsContentType contentType;
    private ClassicsExportFormat exportFormat;
    private ClassicsExportScopeType scopeType;
    private String scopeJson;
    private LocalDateTime requestedAt;
    private LocalDateTime expiresAt;
    private ClassicsExportStatus status;
    private Long storageObjectId;
    private int itemCount;
    private int assetCount;
    private SancaiVisibilityRiskStatus visibilityRiskStatus;
    private boolean contentChanged;

    public ClassicsContentExportJob toEntity() {
        return new ClassicsContentExportJob(
                null,
                exportKind,
                contentType,
                exportFormat,
                scopeType,
                scopeJson,
                requestedAt,
                expiresAt,
                status,
                storageObjectId,
                itemCount,
                assetCount,
                visibilityRiskStatus,
                contentChanged);
    }
}
