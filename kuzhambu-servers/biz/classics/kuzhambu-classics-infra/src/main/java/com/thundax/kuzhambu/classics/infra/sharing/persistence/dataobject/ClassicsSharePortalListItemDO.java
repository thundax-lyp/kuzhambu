package com.thundax.kuzhambu.classics.infra.sharing.persistence.dataobject;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassicsSharePortalListItemDO {
    private Long shareLinkId;
    private String shareToken;
    private String shareTitle;
    private Date issuedAt;
    private Date expiresAt;
    private String contentType;
    private Long contentId;
    private Long contentVersionId;
    private Integer contentVersionNo;
    private String titleSnapshot;
    private String contentVisibilitySnapshot;
    private String targetStatus;
    private Integer priority;
}
