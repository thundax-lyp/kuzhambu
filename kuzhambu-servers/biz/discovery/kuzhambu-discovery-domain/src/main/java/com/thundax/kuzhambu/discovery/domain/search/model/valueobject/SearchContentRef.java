package com.thundax.kuzhambu.discovery.domain.search.model.valueobject;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SearchContentRef implements Serializable {

    private String contentDomain;
    private String contentType;
    private String contentId;
}
