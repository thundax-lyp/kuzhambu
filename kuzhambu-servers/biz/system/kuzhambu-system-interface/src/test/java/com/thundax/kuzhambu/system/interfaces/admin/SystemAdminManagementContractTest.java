package com.thundax.kuzhambu.system.interfaces.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.MenuController;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.RoleController;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.MenuDisplayRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.MenuIdRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.MenuMoveRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.MenuQueryRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.MenuSaveRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.RoleAssignUserRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.RoleIdRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.RoleQueryRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.RoleSaveRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.RoleSortRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.RoleStatusRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.UserAvatarRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.UserCheckRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.UserIdRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.UserQueryRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.UserSaveRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.UserStatusRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.MenuResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.RoleMenuResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.RoleResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.UserDepartmentResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.UserResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.UserRoleResponse;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class SystemAdminManagementContractTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void userRoleAndMenuRoutesShouldKeepAdminApiPaths() throws Exception {
        assertRequestMapping(UserController.class, "/api/sys/user");
        assertPostMapping(UserController.class, "get", "get", UserIdRequest.class);
        assertPostMapping(UserController.class, "list", "list", UserQueryRequest.class);
        assertPostMapping(UserController.class, "page", "page", UserQueryRequest.class);
        assertPostMapping(UserController.class, "listOptions", "options/list");
        assertPostMapping(UserController.class, "add", "create", UserSaveRequest.class);
        assertPostMapping(UserController.class, "update", "update", UserSaveRequest.class);
        assertPostMapping(UserController.class, "deleteAvatar", "avatar/delete", UserAvatarRequest.class);
        assertPostMapping(UserController.class, "getAvatar", "avatar/get", UserAvatarRequest.class);
        assertPostMapping(UserController.class, "updateStatus", "status/update", List.class);
        assertPostMapping(UserController.class, "delete", "delete", List.class);
        assertPostMapping(UserController.class, "getAvailability", "availability/get", UserCheckRequest.class);
        assertPostMapping(UserController.class, "listDepartments", "department/list");
        assertPostMapping(UserController.class, "listRoles", "role/list");

        assertRequestMapping(RoleController.class, "/api/sys/role");
        assertPostMapping(RoleController.class, "get", "get", RoleIdRequest.class);
        assertPostMapping(RoleController.class, "list", "list", RoleQueryRequest.class);
        assertPostMapping(RoleController.class, "listOptions", "options/list");
        assertPostMapping(RoleController.class, "add", "create", RoleSaveRequest.class);
        assertPostMapping(RoleController.class, "update", "update", RoleSaveRequest.class);
        assertPostMapping(RoleController.class, "updateStatus", "status/update", List.class);
        assertPostMapping(RoleController.class, "sort", "sort", RoleSortRequest.class);
        assertPostMapping(RoleController.class, "delete", "delete", List.class);
        assertPostMapping(RoleController.class, "listMenus", "menu/list");
        assertPostMapping(RoleController.class, "listUserTree", "user-tree/list");
        assertPostMapping(RoleController.class, "listUsers", "user/list", RoleIdRequest.class);
        assertPostMapping(RoleController.class, "updateUsers", "user/update", RoleAssignUserRequest.class);

        assertRequestMapping(MenuController.class, "/api/sys/menu");
        assertPostMapping(MenuController.class, "get", "get", MenuIdRequest.class);
        assertPostMapping(MenuController.class, "list", "list", MenuQueryRequest.class);
        assertPostMapping(MenuController.class, "add", "create", MenuSaveRequest.class);
        assertPostMapping(MenuController.class, "update", "update", MenuSaveRequest.class);
        assertPostMapping(MenuController.class, "updateVisibility", "visibility/update", List.class);
        assertPostMapping(MenuController.class, "delete", "delete", List.class);
        assertPostMapping(MenuController.class, "listTree", "tree/list", List.class);
        assertPostMapping(MenuController.class, "move", "move", MenuMoveRequest.class);
    }

    @Test
    void managementRequestsShouldKeepJsonFieldNames() throws Exception {
        UserSaveRequest userSaveRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "id": "user-1",
                  "remarks": "系统管理员",
                  "loginName": "developer",
                  "loginPass": "secret",
                  "ranks": 1,
                  "name": "Developer",
                  "email": "developer@example.com",
                  "mobile": "13800000000",
                  "admin": true,
                  "enable": true,
                  "token": "login-token",
                  "department": { "id": "department-1" },
                  "roles": [{ "id": "role-1" }]
                }
                """,
                UserSaveRequest.class);
        assertEquals("developer", userSaveRequest.getLoginName());
        assertEquals("department-1", userSaveRequest.getDepartment().getId());
        assertEquals("role-1", userSaveRequest.getRoleList().get(0).getId());
        assertJsonFields(
                userSaveRequest,
                "id",
                "remarks",
                "loginName",
                "loginPass",
                "ranks",
                "name",
                "email",
                "mobile",
                "admin",
                "enable",
                "token",
                "department",
                "roles");

        UserStatusRequest userStatusRequest =
                OBJECT_MAPPER.readValue("{\"id\":\"user-1\",\"enable\":false}", UserStatusRequest.class);
        assertEquals("user-1", userStatusRequest.getId());
        assertJsonFields(userStatusRequest, "id", "enable");

        RoleSaveRequest roleSaveRequest = OBJECT_MAPPER.readValue(
                "{\"id\":\"role-1\",\"remarks\":\"后台角色\",\"name\":\"管理员\",\"admin\":true,\"enable\":true,\"menus\":[{\"id\":\"menu-1\"}]}",
                RoleSaveRequest.class);
        assertEquals("menu-1", roleSaveRequest.getMenuList().get(0).getId());
        assertJsonFields(roleSaveRequest, "id", "remarks", "name", "admin", "enable", "menus");

        RoleStatusRequest roleStatusRequest =
                OBJECT_MAPPER.readValue("{\"id\":\"role-1\",\"enable\":true}", RoleStatusRequest.class);
        assertJsonFields(roleStatusRequest, "id", "enable");

        RoleSortRequest roleSortRequest =
                OBJECT_MAPPER.readValue("{\"orderedIds\":[\"role-1\",\"role-2\"]}", RoleSortRequest.class);
        assertJsonFields(roleSortRequest, "orderedIds");

        MenuSaveRequest menuSaveRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "id": "menu-1",
                  "remarks": "菜单备注",
                  "parentId": "menu-root",
                  "name": "菜单管理",
                  "perms": "sys:menu:edit",
                  "ranks": 1,
                  "display": true,
                  "displayParams": "{\\"icon\\":\\"menu\\"}",
                  "url": "/system/menus"
                }
                """,
                MenuSaveRequest.class);
        assertJsonFields(
                menuSaveRequest,
                "id",
                "remarks",
                "parentId",
                "name",
                "perms",
                "ranks",
                "display",
                "displayParams",
                "url");

        MenuDisplayRequest menuDisplayRequest =
                OBJECT_MAPPER.readValue("{\"id\":\"menu-1\",\"display\":false}", MenuDisplayRequest.class);
        assertJsonFields(menuDisplayRequest, "id", "display");

        MenuMoveRequest menuMoveRequest = OBJECT_MAPPER.readValue(
                "{\"fromNodeId\":\"menu-1\",\"toNodeId\":\"menu-2\",\"type\":\"after\"}", MenuMoveRequest.class);
        assertJsonFields(menuMoveRequest, "fromNodeId", "toNodeId", "type");
    }

    @Test
    void managementResponsesShouldKeepJsonFieldNames() throws Exception {
        UserDepartmentResponse departmentResponse = UserDepartmentResponse.builder()
                .id("department-1")
                .parentId("department-root")
                .name("技术中心")
                .shortName("技术")
                .namePath("总部/技术中心")
                .build();
        UserRoleResponse userRoleResponse =
                UserRoleResponse.builder().id("role-1").name("管理员").build();
        assertJsonFields(
                UserResponse.builder()
                        .id("user-1")
                        .remarks("系统管理员")
                        .loginName("developer")
                        .ranks(1)
                        .name("Developer")
                        .email("developer@example.com")
                        .mobile("13800000000")
                        .avatar("/avatar.png")
                        .superAdmin(false)
                        .admin(true)
                        .enable(true)
                        .department(departmentResponse)
                        .roleList(List.of(userRoleResponse))
                        .build(),
                "id",
                "remarks",
                "loginName",
                "ranks",
                "name",
                "email",
                "mobile",
                "avatar",
                "superAdmin",
                "admin",
                "enable",
                "department",
                "roles");

        RoleMenuResponse roleMenuResponse = RoleMenuResponse.builder()
                .id("menu-1")
                .parentId("menu-root")
                .name("菜单管理")
                .perms("sys:menu:edit")
                .build();
        assertJsonFields(
                RoleResponse.builder()
                        .id("role-1")
                        .remarks("后台角色")
                        .name("管理员")
                        .admin(true)
                        .enable(true)
                        .menuList(List.of(roleMenuResponse))
                        .build(),
                "id",
                "remarks",
                "name",
                "admin",
                "enable",
                "menus");

        assertJsonFields(
                MenuResponse.builder()
                        .id("menu-1")
                        .remarks("菜单备注")
                        .parentId("menu-root")
                        .name("菜单管理")
                        .perms("sys:menu:edit")
                        .ranks(1)
                        .display(true)
                        .displayParams("{\"icon\":\"menu\"}")
                        .url("/system/menus")
                        .build(),
                "id",
                "remarks",
                "parentId",
                "name",
                "perms",
                "ranks",
                "display",
                "displayParams",
                "url");
    }

    private static void assertRequestMapping(Class<?> controllerType, String expectedPath) {
        RequestMapping mapping = controllerType.getAnnotation(RequestMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
    }

    private static void assertPostMapping(
            Class<?> controllerType, String methodName, String expectedPath, Class<?>... parameterTypes)
            throws Exception {
        Method method = controllerType.getDeclaredMethod(methodName, parameterTypes);
        PostMapping mapping = method.getAnnotation(PostMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
    }

    private static void assertJsonFields(Object value, String... expectedFields) throws Exception {
        JsonNode json = OBJECT_MAPPER.valueToTree(value);
        assertEquals(expectedFields.length, json.size(), json::toString);
        for (String expectedField : expectedFields) {
            assertTrue(json.has(expectedField), json::toString);
        }
    }
}
