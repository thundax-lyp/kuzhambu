package com.thundax.kuzhambu.classics.application.sharing.query;

import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareLinkId;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareTargetId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShareAccessQuery {
    private ClassicsShareLinkId shareLinkId;
    private ClassicsShareTargetId shareTargetId;
}
