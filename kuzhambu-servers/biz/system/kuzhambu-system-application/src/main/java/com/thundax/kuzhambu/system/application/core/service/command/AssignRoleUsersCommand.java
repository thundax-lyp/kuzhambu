package com.thundax.kuzhambu.system.application.core.service.command;

import com.thundax.kuzhambu.system.application.core.entity.valueobject.RoleId;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.UserId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignRoleUsersCommand {
    private RoleId roleId;
    private List<UserId> userIds;
}
