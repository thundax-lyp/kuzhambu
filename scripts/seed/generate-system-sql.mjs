import { mkdirSync, readFileSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";
const scriptDir = dirname(fileURLToPath(import.meta.url));
const repoRoot = resolve(scriptDir, "../..");
const sourcePath = resolve(repoRoot, "db/data-source/system.json");
const outputPath = resolve(repoRoot, "build/seed-sql/system.sql");
const DEFAULT_PASSWORD = "Q1w2e3r$";
const main = () => {
    const seed = JSON.parse(readFileSync(sourcePath, "utf8"));
    const sql = generate(seed);
    if (process.argv.includes("--check")) {
        const current = readFileSync(outputPath, "utf8");
        if (current !== sql) {
            console.error("build/seed-sql/system.sql is out of date. Run: node scripts/seed/generate-system-sql.mjs");
            process.exit(1);
        }
        return;
    }
    mkdirSync(dirname(outputPath), { recursive: true });
    writeFileSync(outputPath, sql);
};
const generate = (seed) => {
    const departments = flattenTree(seed.departments);
    const menus = flattenTree(seed.menus);
    const roles = buildRoles(seed.roles);
    const users = buildUsers(seed.users);
    const departmentByPath = indexBy(departments, (department) => department.path);
    const roleByName = indexBy(roles, (role) => role.name);
    const menuByPath = indexBy(menus, (menu) => menu.path);
    const lines = ["SET NAMES utf8mb4;", ""];
    appendDepartmentSql(lines, departments);
    appendUserSql(lines, users, departmentByPath);
    appendRoleSql(lines, roles);
    appendMenuSql(lines, menus);
    appendDeprecatedDictSql(lines, seed.deprecatedDicts ?? []);
    appendDictSql(lines, seed.dicts ?? []);
    appendUserRoleSql(lines, users, roleByName);
    appendRoleMenuSql(lines, roles, menus, menuByPath);
    appendAuthSql(lines, users);
    appendAutoIncrementSql(lines, [
        { table: "system_department", nextValue: departments.length + 1 },
        { table: "system_user", nextValue: users.length + 1 },
        { table: "system_role", nextValue: roles.length + 1 },
        { table: "system_menu", nextValue: nextAutoIncrement(menus) },
        { table: "system_dict", nextValue: (seed.dicts ?? []).length + 1 },
        { table: "system_auth_principal_identity", nextValue: users.length + 1 },
        { table: "system_auth_principal_credential", nextValue: users.length + 1 },
    ]);
    lines.push("-- Audit has no required seed data.", "");
    return lines.join("\n");
};
const nextAutoIncrement = (records) => {
    return records.reduce((maxId, record) => Math.max(maxId, record.id), 0) + 1;
};
const buildRoles = (roles) => {
    return roles.map((role, index) => ({
        ...role,
        id: index + 1,
        privilege: role.privilege ?? "NORMAL",
        status: role.status ?? "ENABLED",
        remarks: role.remarks ?? null,
    }));
};
const buildUsers = (users) => {
    return users.map((user, index) => ({
        ...user,
        id: index + 1,
        password: user.password ?? DEFAULT_PASSWORD,
        email: user.email ?? null,
        mobile: user.mobile ?? null,
        tel: user.tel ?? null,
        ranks: user.ranks ?? 2,
        privilege: user.privilege ?? "NORMAL",
        status: user.status ?? "ENABLED",
        needChangePassword: user.needChangePassword ?? true,
        remarks: user.remarks ?? null,
    }));
};
const flattenTree = (nodes) => {
    const records = [];
    const explicitIds = new Set();
    collectExplicitIds(nodes, explicitIds);
    let index = 1;
    let position = 1;
    const nextId = () => {
        while (explicitIds.has(index)) {
            index += 1;
        }
        const id = index;
        index += 1;
        return id;
    };
    const visit = (node, parentId, parentPath) => {
        const name = readName(node);
        const path = parentPath ? `${parentPath}/${name}` : name;
        const id = readExplicitId(node) ?? nextId();
        const lft = position;
        position += 1;
        for (const child of node.children ?? []) {
            visit(child, id, path);
        }
        const rgt = position;
        position += 1;
        const { children: _children, ...rest } = node;
        records.push({ ...rest, id, parentId, path, lft, rgt });
    };
    for (const node of nodes) {
        visit(node, null, null);
    }
    return records.sort((a, b) => a.lft - b.lft);
};
const collectExplicitIds = (nodes, explicitIds) => {
    for (const node of nodes) {
        const explicitId = readExplicitId(node);
        if (explicitId !== null) {
            if (explicitIds.has(explicitId)) {
                throw new Error(`Duplicate explicit id: ${explicitId}`);
            }
            explicitIds.add(explicitId);
        }
        collectExplicitIds(node.children ?? [], explicitIds);
    }
};
const readExplicitId = (node) => {
    const id = node.id;
    if (id === undefined) {
        return null;
    }
    if (!Number.isInteger(id) || Number(id) <= 0) {
        throw new Error("Tree node id must be a positive integer.");
    }
    return Number(id);
};
const readName = (node) => {
    const name = node.name;
    if (typeof name !== "string" || name.trim() === "") {
        throw new Error("Tree node name is required.");
    }
    return name;
};
const appendDepartmentSql = (lines, departments) => {
    lines.push("INSERT INTO `system_department` (");
    lines.push("    `id`, `parent_id`, `lft`, `rgt`, `name`, `short_name`, `remarks`");
    lines.push(") VALUES");
    lines.push(departments
        .map((department) => row([
        department.id,
        department.parentId,
        department.lft,
        department.rgt,
        department.name,
        department.shortName ?? null,
        department.remarks ?? null,
    ]))
        .join(",\n"));
    lines.push("ON DUPLICATE KEY UPDATE");
    lines.push("    `parent_id` = VALUES(`parent_id`),");
    lines.push("    `lft` = VALUES(`lft`),");
    lines.push("    `rgt` = VALUES(`rgt`),");
    lines.push("    `name` = VALUES(`name`),");
    lines.push("    `short_name` = VALUES(`short_name`),");
    lines.push("    `remarks` = VALUES(`remarks`);");
    lines.push("");
};
const appendUserSql = (lines, users, departmentByPath) => {
    lines.push("INSERT INTO `system_user` (");
    lines.push("    `id`, `department_id`, `email`, `mobile`, `tel`, `name`, `ranks`,");
    lines.push("    `privilege`, `status`, `remarks`");
    lines.push(") VALUES");
    lines.push(users
        .map((user) => {
        const department = requireLookup(departmentByPath, user.departmentPath, "department");
        return row([
            user.id,
            department.id,
            user.email,
            user.mobile,
            user.tel,
            user.name,
            user.ranks,
            user.privilege,
            user.status,
            user.remarks,
        ]);
    })
        .join(",\n"));
    lines.push("ON DUPLICATE KEY UPDATE");
    lines.push("    `department_id` = VALUES(`department_id`),");
    lines.push("    `email` = VALUES(`email`),");
    lines.push("    `mobile` = VALUES(`mobile`),");
    lines.push("    `tel` = VALUES(`tel`),");
    lines.push("    `name` = VALUES(`name`),");
    lines.push("    `ranks` = VALUES(`ranks`),");
    lines.push("    `privilege` = VALUES(`privilege`),");
    lines.push("    `status` = VALUES(`status`),");
    lines.push("    `remarks` = VALUES(`remarks`);");
    lines.push("");
};
const appendRoleSql = (lines, roles) => {
    lines.push("INSERT INTO `system_role` (");
    lines.push("    `id`, `name`, `privilege`, `status`, `priority`, `remarks`");
    lines.push(") VALUES");
    lines.push(roles
        .map((role) => row([
        role.id,
        role.name,
        role.privilege,
        role.status,
        role.priority,
        role.remarks,
    ]))
        .join(",\n"));
    lines.push("ON DUPLICATE KEY UPDATE");
    lines.push("    `name` = VALUES(`name`),");
    lines.push("    `privilege` = VALUES(`privilege`),");
    lines.push("    `status` = VALUES(`status`),");
    lines.push("    `priority` = VALUES(`priority`),");
    lines.push("    `remarks` = VALUES(`remarks`);");
    lines.push("");
};
const appendMenuSql = (lines, menus) => {
    lines.push("INSERT INTO `system_menu` (");
    lines.push("    `id`, `parent_id`, `lft`, `rgt`, `name`, `perms`, `ranks`,");
    lines.push("    `visibility`, `display_params`, `url`, `target`, `remarks`");
    lines.push(") VALUES");
    lines.push(menus
        .map((menu) => row([
        menu.id,
        menu.parentId,
        menu.lft,
        menu.rgt,
        menu.name,
        (menu.perms ?? []).join(","),
        menu.priority,
        menu.visibility ?? "VISIBLE",
        JSON.stringify({ icon: menu.icon }),
        menu.url ?? null,
        menu.target ?? "_self",
        menu.remarks ?? null,
    ]))
        .join(",\n"));
    lines.push("ON DUPLICATE KEY UPDATE");
    lines.push("    `parent_id` = VALUES(`parent_id`),");
    lines.push("    `lft` = VALUES(`lft`),");
    lines.push("    `rgt` = VALUES(`rgt`),");
    lines.push("    `name` = VALUES(`name`),");
    lines.push("    `perms` = VALUES(`perms`),");
    lines.push("    `ranks` = VALUES(`ranks`),");
    lines.push("    `visibility` = VALUES(`visibility`),");
    lines.push("    `display_params` = VALUES(`display_params`),");
    lines.push("    `url` = VALUES(`url`),");
    lines.push("    `target` = VALUES(`target`),");
    lines.push("    `remarks` = VALUES(`remarks`);");
    lines.push("");
};
const appendDictSql = (lines, dicts) => {
    if (dicts.length === 0) {
        return;
    }
    lines.push("INSERT INTO `system_dict` (");
    lines.push("    `id`, `type`, `label`, `value`, `priority`, `remarks`");
    lines.push(") VALUES");
    lines.push(dicts
        .map((dict, index) => row([
        index + 1,
        dict.type,
        dict.label,
        dict.value,
        dict.priority,
        dict.remarks ?? null,
    ]))
        .join(",\n"));
    lines.push("ON DUPLICATE KEY UPDATE");
    lines.push("    `type` = VALUES(`type`),");
    lines.push("    `label` = VALUES(`label`),");
    lines.push("    `value` = VALUES(`value`),");
    lines.push("    `priority` = VALUES(`priority`),");
    lines.push("    `remarks` = VALUES(`remarks`);");
    lines.push("");
};
const appendDeprecatedDictSql = (lines, deprecatedDicts) => {
    if (deprecatedDicts.length === 0) {
        return;
    }
    for (const deprecatedDict of deprecatedDicts) {
        lines.push(
            `DELETE FROM \`system_dict\` WHERE \`type\` = ${sqlValue(deprecatedDict.type)} AND \`value\` IN (${deprecatedDict.values.map(sqlValue).join(", ")});`,
        );
    }
    lines.push("");
};
const appendUserRoleSql = (lines, users, roleByName) => {
    const values = users.flatMap((user) => user.roles.map((roleName) => row([user.id, requireLookup(roleByName, roleName, "role").id])));
    lines.push("INSERT INTO `system_user_role` (`user_id`, `role_id`) VALUES");
    lines.push(values.join(",\n"));
    lines.push("ON DUPLICATE KEY UPDATE");
    lines.push("    `role_id` = VALUES(`role_id`);");
    lines.push("");
};
const appendRoleMenuSql = (lines, roles, menus, menuByPath) => {
    const values = roles.flatMap((role) => {
        const roleMenus = role.menus.includes("*")
            ? menus
            : role.menus.map((menuPath) => requireLookup(menuByPath, menuPath, "menu"));
        return roleMenus.map((menu) => row([role.id, menu.id]));
    });
    lines.push("INSERT IGNORE INTO `system_role_menu` (`role_id`, `menu_id`) VALUES");
    lines.push(values.join(",\n"));
    lines.push(";");
    lines.push("");
};
const appendAuthSql = (lines, users) => {
    lines.push("SET NAMES utf8mb4;");
    lines.push("");
    lines.push("-- Initial admin accounts:");
    lines.push("--   login name: admin");
    lines.push("--   login name: developer");
    lines.push("--   password credential values are placeholders and must be rotated before production use.");
    lines.push("");
    lines.push("INSERT INTO `system_auth_principal_identity` (");
    lines.push("    `id`, `principal_type`, `principal_id`, `identity_type`, `identity_value`, `status`");
    lines.push(") VALUES");
    lines.push(users
        .map((user, index) => {
        return row([
            index + 1,
            "USER",
            user.id,
            "USER_ACCOUNT",
            user.loginName,
            user.status,
        ]);
    })
        .join(",\n"));
    lines.push("ON DUPLICATE KEY UPDATE");
    lines.push("    `principal_type` = VALUES(`principal_type`),");
    lines.push("    `principal_id` = VALUES(`principal_id`),");
    lines.push("    `identity_value` = VALUES(`identity_value`),");
    lines.push("    `status` = VALUES(`status`);");
    lines.push("");
    lines.push("INSERT INTO `system_auth_principal_credential` (");
    lines.push("    `id`, `principal_type`, `principal_id`, `identity_id`,");
    lines.push("    `credential_type`, `credential_value`, `status`, `need_change_password`,");
    lines.push("    `failed_count`, `failed_limit`");
    lines.push(") VALUES");
    lines.push(users
        .map((user, index) => {
        const identityId = index + 1;
        const credentialId = index + 1;
        return row([
            credentialId,
            "USER",
            user.id,
            identityId,
            "USER_PASSWORD",
            `{noop}${user.password}`,
            user.status === "DISABLED" ? "LOCKED" : "ACTIVE",
            user.needChangePassword ? 1 : 0,
            0,
            5,
        ]);
    })
        .join(",\n"));
    lines.push("ON DUPLICATE KEY UPDATE");
    lines.push("    `principal_type` = VALUES(`principal_type`),");
    lines.push("    `principal_id` = VALUES(`principal_id`),");
    lines.push("    `identity_id` = VALUES(`identity_id`),");
    lines.push("    `credential_value` = VALUES(`credential_value`),");
    lines.push("    `status` = VALUES(`status`),");
    lines.push("    `need_change_password` = VALUES(`need_change_password`),");
    lines.push("    `failed_count` = VALUES(`failed_count`),");
    lines.push("    `failed_limit` = VALUES(`failed_limit`);");
    lines.push("");
    lines.push("SET NAMES utf8mb4;");
    lines.push("");
};
const appendAutoIncrementSql = (lines, targets) => {
    for (const target of targets) {
        lines.push(`ALTER TABLE \`${target.table}\` AUTO_INCREMENT = ${target.nextValue};`);
    }
    lines.push("");
};
const row = (values) => `    (${values.map(sqlValue).join(", ")})`;
const sqlValue = (value) => {
    if (value === null) {
        return "NULL";
    }
    if (typeof value === "number") {
        return String(value);
    }
    return `'${value.replace(/'/g, "''")}'`;
};
const indexBy = (items, keyFactory) => {
    const map = new Map();
    for (const item of items) {
        const key = keyFactory(item);
        if (map.has(key)) {
            throw new Error(`Duplicate key: ${key}`);
        }
        map.set(key, item);
    }
    return map;
};
const requireLookup = (map, key, type) => {
    const value = map.get(key);
    if (!value) {
        throw new Error(`Unknown ${type}: ${key}`);
    }
    return value;
};
main();
