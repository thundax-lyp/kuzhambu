package com.thundax.kuzhambu.classics.infra.sharing.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("classics_share_link")
public class ClassicsShareLinkDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String tokenHash;
    private String title;
    private String visibility;
    private String status;
    private String visibilityRiskStatus;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private Long accessCount;
}
