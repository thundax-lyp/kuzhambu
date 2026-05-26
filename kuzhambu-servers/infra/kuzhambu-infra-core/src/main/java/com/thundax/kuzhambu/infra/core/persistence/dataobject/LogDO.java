package com.thundax.kuzhambu.infra.core.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_log")
public class LogDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long userId;

    private String type;

    private Date logDate;

    private String title;

    private String remoteAddr;

    private String userAgent;

    private String method;

    private String requestUri;

    private String requestParams;
}
