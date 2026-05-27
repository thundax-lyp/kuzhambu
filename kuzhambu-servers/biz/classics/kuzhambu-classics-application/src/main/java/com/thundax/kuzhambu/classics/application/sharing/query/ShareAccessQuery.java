package com.thundax.kuzhambu.classics.application.sharing.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShareAccessQuery {
    private Long shareLinkId;
    private Long shareTargetId;
}
