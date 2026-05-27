package com.thundax.kuzhambu.system.interfaces.admin.core.controller;

import com.thundax.kuzhambu.system.application.auth.entity.PrincipalIdentity;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalType;
import com.thundax.kuzhambu.system.application.auth.entity.valueobject.PreAuthSessionId;
import com.thundax.kuzhambu.system.application.auth.entity.valueobject.PreAuthSessionToken;
import com.thundax.kuzhambu.system.application.auth.entity.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.application.auth.service.PreAuthSessionService;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalIdentityService;
import com.thundax.kuzhambu.system.application.auth.service.query.PreAuthSessionValueQuery;
import com.thundax.kuzhambu.system.application.auth.service.query.PrincipalIdentityQuery;
import com.thundax.kuzhambu.system.application.core.entity.User;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.UserIdCodec;
import com.thundax.kuzhambu.system.application.core.service.CurrentUserService;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeCurrentUserAvatarCommand;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeCurrentUserInfoCommand;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeCurrentUserPasswordCommand;
import com.thundax.kuzhambu.system.application.core.service.command.RemoveCurrentUserAvatarCommand;
import com.thundax.kuzhambu.system.application.core.service.query.CurrentUserQuery;
import com.thundax.kuzhambu.common.core.crypto.Sm2Crypto;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.system.interfaces.admin.auth.security.CurrentUserResolver;
import com.thundax.kuzhambu.system.interfaces.admin.core.aop.annotation.SysLogger;
import com.thundax.kuzhambu.system.interfaces.admin.core.assembler.PersonalInterfaceAssembler;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.PersonalAvatarUploadRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.PersonalInfoUpdateRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.PersonalPasswordUpdateRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.PersonalAvatarResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.PersonalInfoResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.PersonalMenuResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.PersonalPermsResponse;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "系统/当前用户")
@SysLogger(module = {"系统", "当前用户"})
@RequestMapping(value = "/api/sys/current-user")
@WrappedApiController
public class CurrentUserController {

    private static final String PRIVATE_KEY_ITEM = "privateKey";

    private final CurrentUserService currentUserService;
    private final CurrentUserResolver currentUserResolver;
    private final PrincipalIdentityService principalIdentityService;
    private final PreAuthSessionService preAuthSessionService;

    public CurrentUserController(
            CurrentUserService currentUserService,
            CurrentUserResolver currentUserResolver,
            PrincipalIdentityService principalIdentityService,
            PreAuthSessionService preAuthSessionService) {

        this.currentUserService = currentUserService;
        this.currentUserResolver = currentUserResolver;
        this.principalIdentityService = principalIdentityService;
        this.preAuthSessionService = preAuthSessionService;
    }

    @Operation(summary = "当前用户信息", description = "读取当前登录后台用户的基础资料和登录名")
    @HasPermission(value = "user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "info")
    public PersonalInfoResponse info() {
        User currentUser = currentUserResolver.requireCurrentUser();

        return PersonalInterfaceAssembler.toInfoResponse(
                currentUser, getAccountLoginName(currentUser), readAvatarUrl(currentUser));
    }

    @Operation(summary = "更新当前用户信息", description = "更新当前登录后台用户的姓名、邮箱和手机号")
    @HasPermission(value = "user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger(value = "更新")
    @PostMapping(value = "info/update")
    public PersonalInfoResponse updateInfo(@Valid @RequestBody PersonalInfoUpdateRequest request) {
        User currentUser = currentUserResolver.currentUser();

        currentUser = currentUserService.changeInfo(new ChangeCurrentUserInfoCommand(
                currentUser.getId(),
                currentUser.getDepartmentId(),
                request.getEmail(),
                request.getMobile(),
                currentUser.getTel(),
                request.getName(),
                currentUser.getRank(),
                currentUser.getPrivilege(),
                currentUser.getStatus(),
                currentUser.getRemarks()));

        return PersonalInterfaceAssembler.toInfoResponse(
                currentUser, getAccountLoginName(currentUser), readAvatarUrl(currentUser));
    }

    @Operation(summary = "更新当前用户密码", description = "校验当前登录后台用户旧密码后更新密码凭据")
    @HasPermission(value = "user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger(value = "更新密码")
    @PostMapping(value = "password/update")
    public Boolean updatePassword(@Valid @RequestBody PersonalPasswordUpdateRequest request) {

        // 解密密码（数据需要加密传输）
        String privateKey = getPrivateKey(request.getToken());
        String password = Sm2Crypto.decrypt(request.getPassword(), privateKey);
        String oldPassword = Sm2Crypto.decrypt(request.getOldPassword(), privateKey);
        request.setPassword(password);
        request.setOldPassword(oldPassword);

        User currentUser = currentUserResolver.currentUser();

        currentUserService.changePassword(
                new ChangeCurrentUserPasswordCommand(currentUser.getId(), oldPassword, password));

        return true;
    }

    @Operation(summary = "上传当前用户头像", description = "保存当前登录后台用户头像文件并返回头像访问信息")
    @HasPermission(value = "user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger(value = "上传头像")
    @PostMapping(value = "avatar/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PersonalAvatarResponse uploadAvatar(@Valid PersonalAvatarUploadRequest request) {
        User currentUser = currentUserResolver.currentUser();

        try {
            currentUserService.changeAvatar(new ChangeCurrentUserAvatarCommand(
                    currentUser.getId(),
                    request.getAvatar().getInputStream(),
                    request.getAvatar().getOriginalFilename()));
        } catch (IOException e) {
            throw AdminResponseExceptions.system(e.getMessage());
        }

        return PersonalInterfaceAssembler.toAvatarResponse(readAvatarUrl(currentUser));
    }

    @Operation(summary = "删除当前用户头像", description = "删除当前登录后台用户头像文件并返回头像访问信息")
    @HasPermission(value = "user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger(value = "删除头像")
    @PostMapping(value = "avatar/delete")
    public PersonalAvatarResponse deleteAvatar() {
        User currentUser = currentUserResolver.currentUser();

        currentUserService.removeAvatar(new RemoveCurrentUserAvatarCommand(currentUser.getId()));

        return PersonalInterfaceAssembler.toAvatarResponse(null);
    }

    @Operation(summary = "当前用户菜单列表", description = "按当前登录后台用户角色和访问等级返回可见菜单树列表")
    @HasPermission(value = "user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "menus")
    public List<PersonalMenuResponse> menus() {
        return currentUserService.listVisibleMenus(toQuery(currentUserResolver.currentUser())).stream()
                .map(PersonalInterfaceAssembler::toMenuResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "当前用户权限列表", description = "返回当前登录后台用户认证上下文中的权限编码集合")
    @HasPermission(value = "user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "perms")
    public PersonalPermsResponse perms() {
        return PersonalInterfaceAssembler.toPermsResponse(currentUserResolver.currentAuthorities());
    }

    private String getAccountLoginName(User user) {
        if (user == null || user.getId() == null) {
            return null;
        }
        PrincipalIdentity identity = principalIdentityService.get(identityQuery(
                PrincipalKey.of(PrincipalType.USER, UserIdCodec.toValue(user.getId())),
                PrincipalIdentityType.USER_ACCOUNT));
        return identity == null ? null : identity.getIdentityValue();
    }

    private String readAvatarUrl(User user) {
        if (user == null || !currentUserService.existsAvatar(user.getId())) {
            return null;
        }
        return UserController.getAvatarUrl(UserIdCodec.toStringValue(user.getId()));
    }

    private PrincipalIdentityQuery identityQuery(PrincipalKey principalKey, PrincipalIdentityType identityType) {
        PrincipalIdentityQuery query = new PrincipalIdentityQuery();
        query.setPrincipalKey(principalKey);
        query.setIdentityType(identityType);
        return query;
    }

    private CurrentUserQuery toQuery(User currentUser) {
        return new CurrentUserQuery(
                currentUser.getId(), currentUser.getPrivilege(), currentUser.getStatus(), currentUser.getRank());
    }

    private String getPrivateKey(String token) {
        PreAuthSessionId sessionId = preAuthSessionService.getIdByToken(PreAuthSessionToken.of(token));
        if (sessionId == null) {
            throw AdminResponseExceptions.invalidToken();
        }
        String privateKey = preAuthSessionService.getValue(new PreAuthSessionValueQuery(sessionId, PRIVATE_KEY_ITEM));
        if (StringUtils.isBlank(privateKey)) {
            throw AdminResponseExceptions.invalidToken();
        }
        return privateKey;
    }
}
