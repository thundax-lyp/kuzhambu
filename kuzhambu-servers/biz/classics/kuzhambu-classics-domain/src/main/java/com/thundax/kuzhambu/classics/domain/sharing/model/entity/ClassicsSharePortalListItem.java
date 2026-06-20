package com.thundax.kuzhambu.classics.domain.sharing.model.entity;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareTargetStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsSharedContentVisibility;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareLinkId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassicsSharePortalListItem {
    private ClassicsShareLinkId shareLinkId;
    private String shareToken;
    private String shareTitle;
    private Date issuedAt;
    private Date expiresAt;
    private ClassicsContentType contentType;
    private ClassicsContentId contentId;
    private ClassicsContentVersionId contentVersionId;
    private Integer contentVersionNo;
    private String titleSnapshot;
    private ClassicsSharedContentVisibility contentVisibilitySnapshot;
    private ClassicsShareTargetStatus targetStatus;
    private int priority;
}
