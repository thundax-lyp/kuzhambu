package com.thundax.kuzhambu.biz.core.service.query;

import com.thundax.kuzhambu.biz.core.entity.enums.RoleStatus;
import com.thundax.kuzhambu.biz.core.entity.valueobject.RoleId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleQuery {
    private RoleId id;
    private RoleStatus status;
}
