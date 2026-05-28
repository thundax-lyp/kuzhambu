package com.thundax.kuzhambu.classics.domain.sharing.model.entity;

import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareAccessResult;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareAccessRecordId;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareLinkId;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareTargetId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassicsShareAccessRecord {
    private ClassicsShareAccessRecordId id;
    private ClassicsShareLinkId shareLinkId;
    private ClassicsShareTargetId shareTargetId;
    private Date accessedAt;
    private ClassicsShareAccessResult accessResult;
    private String clientSnapshot;
}
