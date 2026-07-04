package com.thundax.kuzhambu.classics.facade.response;

import com.thundax.kuzhambu.classics.facade.dto.ClassicsQaKnowledgeFacadeDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassicsQaKnowledgeFacadeResponse {

    private final ClassicsQaKnowledgeFacadeDto knowledge;
}
