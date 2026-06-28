package com.thundax.kuzhambu.classics.facade.response;

import com.thundax.kuzhambu.classics.facade.dto.ClassicsPublicContentFacadeDto;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ClassicsPublicContentFacadeResponse {

    private final ClassicsPublicContentFacadeDto content;

    @Builder
    private ClassicsPublicContentFacadeResponse(ClassicsPublicContentFacadeDto content) {
        this.content = content;
    }
}
