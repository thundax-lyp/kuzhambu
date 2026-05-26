package com.thundax.kuzhambu.biz.storage.service.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AbortMultipartUploadCommand {
    private String uploadId;
}
