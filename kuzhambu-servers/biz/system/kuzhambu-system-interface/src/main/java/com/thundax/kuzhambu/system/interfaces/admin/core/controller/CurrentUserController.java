package com.thundax.kuzhambu.system.interfaces.admin.core.controller;

import com.thundax.kuzhambu.common.core.crypto.Sm2Crypto;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.PostJsonApiExempt;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.system.application.auth.query.PrincipalIdentityQuery;
import com.thundax.kuzhambu.system.application.auth.service.PreAuthSessionApplicationService;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalIdentityApplicationService;
import com.thundax.kuzhambu.system.application.core.service.CurrentUserProfileApplicationService;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalIdentity;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.interfaces.admin.auth.security.CurrentUserResolver;
import com.thundax.kuzhambu.system.interfaces.admin.core.assembler.PersonalInterfaceAssembler;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.PersonalAvatarUploadRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.PersonalInfoUpdateRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.PersonalPasswordUpdateRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.PersonalAvatarResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.PersonalInfoResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.PersonalMenuResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.PersonalPermsResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.support.AdminAvatarUrlBuilder;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "系统模块-个人信息", description = "个人信息")
@SysLogger(module = {"系统", "当前用户"})
@RequestMapping(value = "/api/sys/current-user")
@WrappedApiController
public class CurrentUserController {

    private static final String PRIVATE_KEY_ITEM = "privateKey";

    private final CurrentUserProfileApplicationService currentUserService;
    private final CurrentUserResolver currentUserResolver;
    private final PrincipalIdentityApplicationService principalIdentityService;
    private final PreAuthSessionApplicationService preAuthSessionService;
    private final AdminAvatarUrlBuilder avatarUrlBuilder;

    public CurrentUserController(
            CurrentUserProfileApplicationService currentUserService,
            CurrentUserResolver currentUserResolver,
            PrincipalIdentityApplicationService principalIdentityService,
            PreAuthSessionApplicationService preAuthSessionService,
            AdminAvatarUrlBuilder avatarUrlBuilder) {

        this.currentUserService = currentUserService;
        this.currentUserResolver = currentUserResolver;
        this.principalIdentityService = principalIdentityService;
        this.preAuthSessionService = preAuthSessionService;
        this.avatarUrlBuilder = avatarUrlBuilder;
    }

    @Operation(summary = "当前用户信息", description = "读取当前登录后台用户的基础资料和登录名")
    @HasPermission(value = "user")
    @IgnoreSysLogger
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "get")
    public PersonalInfoResponse get() {
        User currentUser = currentUserResolver.requireCurrentUser();

        return PersonalInterfaceAssembler.toInfoResponse(
                currentUser,
                Optional.ofNullable(getAccountLoginName(currentUser)),
                Optional.ofNullable(readAvatarUrl(currentUser)));
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

        currentUser = currentUserService.changeInfo(
                PersonalInterfaceAssembler.toChangeCurrentUserInfoCommand(currentUser, request));

        return PersonalInterfaceAssembler.toInfoResponse(
                currentUser,
                Optional.ofNullable(getAccountLoginName(currentUser)),
                Optional.ofNullable(readAvatarUrl(currentUser)));
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
                PersonalInterfaceAssembler.toChangeCurrentUserPasswordCommand(currentUser, oldPassword, password));

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
    @PostJsonApiExempt(reason = "头像上传必须使用 multipart/form-data 承载文件流")
    @PostMapping(value = "avatar/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PersonalAvatarResponse uploadAvatar(@Valid PersonalAvatarUploadRequest request) {
        User currentUser = currentUserResolver.currentUser();

        try {
            currentUserService.changeAvatar(PersonalInterfaceAssembler.toChangeCurrentUserAvatarCommand(
                    currentUser,
                    request.getAvatar().getInputStream(),
                    Optional.ofNullable(request.getAvatar().getOriginalFilename())));
        } catch (IOException e) {
            throw AdminResponseExceptions.system(e.getMessage());
        }

        return PersonalInterfaceAssembler.toAvatarResponse(Optional.ofNullable(readAvatarUrl(currentUser)));
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

        currentUserService.removeAvatar(PersonalInterfaceAssembler.toRemoveCurrentUserAvatarCommand(currentUser));

        return PersonalInterfaceAssembler.toAvatarResponse(Optional.empty());
    }

    @Operation(summary = "当前用户菜单列表", description = "按当前登录后台用户角色和访问等级返回可见菜单树列表")
    @HasPermission(value = "user")
    @IgnoreSysLogger
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "menu/list")
    public List<PersonalMenuResponse> listMenus() {
        return currentUserService
                .listVisibleMenus(PersonalInterfaceAssembler.toCurrentUserQuery(currentUserResolver.currentUser()))
                .stream()
                .map(PersonalInterfaceAssembler::toMenuResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "当前用户权限列表", description = "返回当前登录后台用户认证上下文中的权限编码集合")
    @HasPermission(value = "user")
    @IgnoreSysLogger
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "permission/list")
    public PersonalPermsResponse listPermissions() {
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
        if (user == null
                || !currentUserService.existsAvatar(PersonalInterfaceAssembler.toCurrentUserAvatarQuery(user))) {
            return null;
        }
        return avatarUrlBuilder.build(UserIdCodec.toStringValue(user.getId()));
    }

    private PrincipalIdentityQuery identityQuery(PrincipalKey principalKey, PrincipalIdentityType identityType) {
        return PersonalInterfaceAssembler.toPrincipalIdentityQuery(principalKey, identityType);
    }

    private String getPrivateKey(String token) {
        PreAuthSessionId sessionId =
                preAuthSessionService.getIdByToken(PersonalInterfaceAssembler.toPreAuthSessionQuery(token));
        if (sessionId == null) {
            throw AdminResponseExceptions.invalidToken();
        }
        String privateKey = preAuthSessionService.getValue(
                PersonalInterfaceAssembler.toPreAuthSessionValueQuery(sessionId, PRIVATE_KEY_ITEM));
        if (StringUtils.isBlank(privateKey)) {
            throw AdminResponseExceptions.invalidToken();
        }
        return privateKey;
    }
}
