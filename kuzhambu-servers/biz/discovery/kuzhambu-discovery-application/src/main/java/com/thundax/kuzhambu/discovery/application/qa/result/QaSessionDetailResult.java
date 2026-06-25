package com.thundax.kuzhambu.discovery.application.qa.result;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QaSessionDetailResult extends QaSessionResult {
    private List<QaMessageResult> messages;
}
