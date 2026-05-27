package com.thundax.kuzhambu.classics.application.wangqi.command;

import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiContentFormat;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WangqiDocumentSaveCommand {
    private Long id;
    private String title;
    private String summary;
    private WangqiContentFormat contentFormat;
    private String content;
    private LocalDateTime documentTime;
    private Long storageObjectId;
    private WangqiDocumentVisibility visibility;
}
