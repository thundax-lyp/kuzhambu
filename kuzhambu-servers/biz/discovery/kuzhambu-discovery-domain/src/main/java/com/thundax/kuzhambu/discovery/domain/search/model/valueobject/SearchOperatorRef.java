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
public class SearchOperatorRef implements Serializable {

    private String operatorType;
    private String operatorId;
}
