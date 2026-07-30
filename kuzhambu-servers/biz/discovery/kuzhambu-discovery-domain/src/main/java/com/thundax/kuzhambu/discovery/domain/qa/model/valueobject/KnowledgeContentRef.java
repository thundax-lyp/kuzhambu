package com.thundax.kuzhambu.discovery.domain.qa.model.valueobject;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeContentRef implements Serializable {

    private String contentType;
    private Long contentId;
}
