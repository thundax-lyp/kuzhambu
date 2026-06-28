package com.thundax.kuzhambu.classics.facade.response;

import com.thundax.kuzhambu.classics.facade.dto.ClassicsPublicContentFacadeDto;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ClassicsPublicContentsFacadeResponse {

    private final List<ClassicsPublicContentFacadeDto> contents;

    @Builder
    private ClassicsPublicContentsFacadeResponse(List<ClassicsPublicContentFacadeDto> contents) {
        this.contents = contents;
    }
}
