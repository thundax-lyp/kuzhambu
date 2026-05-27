package com.thundax.kuzhambu.system.interfaces.admin.core.assembler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.system.application.core.codec.AccessRankCodec;
import com.thundax.kuzhambu.system.application.core.entity.Menu;
import com.thundax.kuzhambu.system.application.core.entity.User;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.MenuIdCodec;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.UserIdCodec;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.PersonalInfoUpdateRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.PersonalAvatarResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.PersonalInfoResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.PersonalMenuResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.PersonalPermsResponse;
import java.util.Set;
import org.springframework.lang.NonNull;

public final class PersonalInterfaceAssembler {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private PersonalInterfaceAssembler() {}

    @NonNull
    public static PersonalInfoResponse toInfoResponse(User entity, String loginName, String avatarUrl) {
        if (entity == null) {
            return PersonalInfoResponse.builder().build();
        }
        return PersonalInfoResponse.builder()
                .id(UserIdCodec.toStringValue(entity.getId()))
                .loginName(loginName)
                .ranks(AccessRankCodec.toValue(entity.getRank()))
                .name(entity.getName())
                .mobile(entity.getMobile())
                .email(entity.getEmail())
                .avatar(avatarUrl)
                .admin(entity.isAdmin())
                .superAdmin(entity.isSuper())
                .build();
    }

    @NonNull
    public static PersonalAvatarResponse toAvatarResponse(String avatarUrl) {
        return PersonalAvatarResponse.builder().avatar(avatarUrl).build();
    }

    @NonNull
    public static PersonalMenuResponse toMenuResponse(Menu entity) {
        if (entity == null) {
            return PersonalMenuResponse.builder().build();
        }
        return PersonalMenuResponse.builder()
                .id(MenuIdCodec.toStringValue(entity.getId()))
                .parentId(MenuIdCodec.toStringValue(entity.getParentId()))
                .name(entity.getName())
                .url(entity.getUrl())
                .icon(readIcon(entity.getDisplayParams()))
                .displayParams(entity.getDisplayParams())
                .build();
    }

    @NonNull
    public static PersonalPermsResponse toPermsResponse(Set<String> perms) {
        return PersonalPermsResponse.builder().perms(perms).build();
    }

    @NonNull
    public static User toEntity(@NonNull User entity, @NonNull PersonalInfoUpdateRequest request) {
        entity.setName(request.getName());
        entity.setEmail(request.getEmail());
        entity.setMobile(request.getMobile());
        return entity;
    }

    private static String readIcon(String displayParams) {
        if (displayParams == null || displayParams.trim().isEmpty()) {
            return null;
        }
        try {
            JsonNode icon = OBJECT_MAPPER.readTree(displayParams).get("icon");
            if (icon == null || !icon.isTextual() || icon.asText().trim().isEmpty()) {
                return null;
            }
            return icon.asText().trim();
        } catch (Exception ignored) {
            return null;
        }
    }
}
