package com.thundax.kuzhambu.classics.domain.sharing.model.entity;

import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareAccessResult;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassicsShareAccessRecord {
    private Long id;
    private Long shareLinkId;
    private Long shareTargetId;
    private LocalDateTime accessedAt;
    private ClassicsShareAccessResult accessResult;
    private String clientSnapshot;
}
