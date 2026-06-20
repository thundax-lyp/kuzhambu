import { readFileSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

type TreeNode<T> = T & { children?: Array<TreeNode<T>> };

type DepartmentSeed = {
  name: string;
  shortName?: string;
  remarks?: string;
};

type RoleSeed = {
  name: string;
  privilege?: string;
  status?: string;
  priority: number;
  remarks?: string;
  menus: string[];
};

type UserSeed = {
  loginName: string;
  password?: string;
  name: string;
  departmentPath: string;
  email?: string | null;
  mobile?: string | null;
  tel?: string | null;
  ranks?: number;
  privilege?: string;
  status?: string;
  needChangePassword?: boolean;
  remarks?: string;
  roles: string[];
};

type MenuSeed = {
  name: string;
  perms?: string[];
  priority: number;
  visibility?: string;
  icon: string;
  url?: string | null;
  target?: string | null;
  remarks?: string;
};

type DictSeed = {
  type: string;
  label: string;
  value: string;
  priority: number;
  remarks?: string;
};

type SystemSeed = {
  departments: Array<TreeNode<DepartmentSeed>>;
  roles: RoleSeed[];
  users: UserSeed[];
  dicts?: DictSeed[];
  menus: Array<TreeNode<MenuSeed>>;
};

type TreeRecord<T> = T & {
  id: number;
  parentId: number | null;
  path: string;
  lft: number;
  rgt: number;
};

const scriptDir = dirname(fileURLToPath(import.meta.url));
const repoRoot = resolve(scriptDir, "..");
const sourcePath = resolve(repoRoot, "db/data-source/system.json");
const outputPath = resolve(repoRoot, "db/data/system.sql");

const DEFAULT_PASSWORD = "Q1w2e3r$";

const main = () => {
  const seed = JSON.parse(readFileSync(sourcePath, "utf8")) as SystemSeed;
  const sql = generate(seed);

  if (process.argv.includes("--check")) {
    const current = readFileSync(outputPath, "utf8");
    if (current !== sql) {
      console.error(
        "db/data/system.sql is out of date. Run: node scripts/generate-system-data-sql.ts",
      );
      process.exit(1);
    }
    return;
  }

  writeFileSync(outputPath, sql);
};

const generate = (seed: SystemSeed) => {
  const departments = flattenTree(seed.departments);
  const menus = flattenTree(seed.menus);
  const roles = buildRoles(seed.roles);
  const users = buildUsers(seed.users);

  const departmentByPath = indexBy(
    departments,
    (department) => department.path,
  );
  const roleByName = indexBy(roles, (role) => role.name);
  const menuByPath = indexBy(menus, (menu) => menu.path);

  const lines: string[] = ["SET NAMES utf8mb4;", ""];
  appendDepartmentSql(lines, departments);
  appendUserSql(lines, users, departmentByPath);
  appendRoleSql(lines, roles);
  appendMenuSql(lines, menus);
  appendDictSql(lines, seed.dicts ?? []);
  appendUserRoleSql(lines, users, roleByName);
  appendRoleMenuSql(lines, roles, menus, menuByPath);
  appendAuthSql(lines, users);
  appendAutoIncrementSql(lines, [
    { table: "system_department", nextValue: departments.length + 1 },
    { table: "system_user", nextValue: users.length + 1 },
    { table: "system_role", nextValue: roles.length + 1 },
    { table: "system_menu", nextValue: menus.length + 1 },
    { table: "system_dict", nextValue: (seed.dicts ?? []).length + 1 },
    { table: "system_auth_principal_identity", nextValue: users.length + 1 },
    { table: "system_auth_principal_credential", nextValue: users.length + 1 },
  ]);
  lines.push("-- Audit has no required seed data.", "");
  return lines.join("\n");
};

const buildRoles = (roles: RoleSeed[]) => {
  return roles.map((role, index) => ({
    ...role,
    id: index + 1,
    privilege: role.privilege ?? "NORMAL",
    status: role.status ?? "ENABLED",
    remarks: role.remarks ?? null,
  }));
};

const buildUsers = (users: UserSeed[]) => {
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

const flattenTree = <T extends object>(nodes: Array<TreeNode<T>>) => {
  const records: Array<TreeRecord<T>> = [];
  let index = 1;
  let position = 1;

  const visit = (
    node: TreeNode<T>,
    parentId: number | null,
    parentPath: string | null,
  ) => {
    const name = readName(node);
    const path = parentPath ? `${parentPath}/${name}` : name;
    const id = index;
    index += 1;
    const lft = position;
    position += 1;
    for (const child of node.children ?? []) {
      visit(child, id, path);
    }
    const rgt = position;
    position += 1;
    const { children: _children, ...rest } = node;
    records.push({ ...(rest as T), id, parentId, path, lft, rgt });
  };

  for (const node of nodes) {
    visit(node, null, null);
  }

  return records.sort((a, b) => a.lft - b.lft);
};

const readName = (node: object) => {
  const name = (node as { name?: unknown }).name;
  if (typeof name !== "string" || name.trim() === "") {
    throw new Error("Tree node name is required.");
  }
  return name;
};

const appendDepartmentSql = (
  lines: string[],
  departments: Array<TreeRecord<DepartmentSeed>>,
) => {
  lines.push("INSERT INTO `system_department` (");
  lines.push(
    "    `id`, `parent_id`, `lft`, `rgt`, `name`, `short_name`, `remarks`",
  );
  lines.push(") VALUES");
  lines.push(
    departments
      .map((department) =>
        row([
          department.id,
          department.parentId,
          department.lft,
          department.rgt,
          department.name,
          department.shortName ?? null,
          department.remarks ?? null,
        ]),
      )
      .join(",\n"),
  );
  lines.push("ON DUPLICATE KEY UPDATE");
  lines.push("    `parent_id` = VALUES(`parent_id`),");
  lines.push("    `lft` = VALUES(`lft`),");
  lines.push("    `rgt` = VALUES(`rgt`),");
  lines.push("    `name` = VALUES(`name`),");
  lines.push("    `short_name` = VALUES(`short_name`),");
  lines.push("    `remarks` = VALUES(`remarks`);");
  lines.push("");
};

const appendUserSql = (
  lines: string[],
  users: ReturnType<typeof buildUsers>,
  departmentByPath: Map<string, TreeRecord<DepartmentSeed>>,
) => {
  lines.push("INSERT INTO `system_user` (");
  lines.push(
    "    `id`, `department_id`, `email`, `mobile`, `tel`, `name`, `ranks`,",
  );
  lines.push("    `privilege`, `status`, `remarks`");
  lines.push(") VALUES");
  lines.push(
    users
      .map((user) => {
        const department = requireLookup(
          departmentByPath,
          user.departmentPath,
          "department",
        );
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
      .join(",\n"),
  );
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

const appendRoleSql = (
  lines: string[],
  roles: ReturnType<typeof buildRoles>,
) => {
  lines.push("INSERT INTO `system_role` (");
  lines.push("    `id`, `name`, `privilege`, `status`, `priority`, `remarks`");
  lines.push(") VALUES");
  lines.push(
    roles
      .map((role) =>
        row([
          role.id,
          role.name,
          role.privilege,
          role.status,
          role.priority,
          role.remarks,
        ]),
      )
      .join(",\n"),
  );
  lines.push("ON DUPLICATE KEY UPDATE");
  lines.push("    `name` = VALUES(`name`),");
  lines.push("    `privilege` = VALUES(`privilege`),");
  lines.push("    `status` = VALUES(`status`),");
  lines.push("    `priority` = VALUES(`priority`),");
  lines.push("    `remarks` = VALUES(`remarks`);");
  lines.push("");
};

const appendMenuSql = (lines: string[], menus: Array<TreeRecord<MenuSeed>>) => {
  lines.push("INSERT INTO `system_menu` (");
  lines.push("    `id`, `parent_id`, `lft`, `rgt`, `name`, `perms`, `ranks`,");
  lines.push("    `visibility`, `display_params`, `url`, `target`, `remarks`");
  lines.push(") VALUES");
  lines.push(
    menus
      .map((menu) =>
        row([
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
        ]),
      )
      .join(",\n"),
  );
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

const appendDictSql = (lines: string[], dicts: DictSeed[]) => {
  if (dicts.length === 0) {
    return;
  }
  lines.push("INSERT INTO `system_dict` (");
  lines.push("    `id`, `type`, `label`, `value`, `priority`, `remarks`");
  lines.push(") VALUES");
  lines.push(
    dicts
      .map((dict, index) =>
        row([
          index + 1,
          dict.type,
          dict.label,
          dict.value,
          dict.priority,
          dict.remarks ?? null,
        ]),
      )
      .join(",\n"),
  );
  lines.push("ON DUPLICATE KEY UPDATE");
  lines.push("    `type` = VALUES(`type`),");
  lines.push("    `label` = VALUES(`label`),");
  lines.push("    `value` = VALUES(`value`),");
  lines.push("    `priority` = VALUES(`priority`),");
  lines.push("    `remarks` = VALUES(`remarks`);");
  lines.push("");
};

const appendUserRoleSql = (
  lines: string[],
  users: ReturnType<typeof buildUsers>,
  roleByName: Map<string, ReturnType<typeof buildRoles>[number]>,
) => {
  const values = users.flatMap((user) =>
    user.roles.map((roleName) =>
      row([user.id, requireLookup(roleByName, roleName, "role").id]),
    ),
  );
  lines.push("INSERT INTO `system_user_role` (`user_id`, `role_id`) VALUES");
  lines.push(values.join(",\n"));
  lines.push("ON DUPLICATE KEY UPDATE");
  lines.push("    `role_id` = VALUES(`role_id`);");
  lines.push("");
};

const appendRoleMenuSql = (
  lines: string[],
  roles: ReturnType<typeof buildRoles>,
  menus: Array<TreeRecord<MenuSeed>>,
  menuByPath: Map<string, TreeRecord<MenuSeed>>,
) => {
  const values = roles.flatMap((role) => {
    const roleMenus = role.menus.includes("*")
      ? menus
      : role.menus.map((menuPath) =>
          requireLookup(menuByPath, menuPath, "menu"),
        );
    return roleMenus.map((menu) => row([role.id, menu.id]));
  });
  lines.push(
    "INSERT IGNORE INTO `system_role_menu` (`role_id`, `menu_id`) VALUES",
  );
  lines.push(values.join(",\n"));
  lines.push(";");
  lines.push("");
};

const appendAuthSql = (
  lines: string[],
  users: ReturnType<typeof buildUsers>,
) => {
  lines.push("SET NAMES utf8mb4;");
  lines.push("");
  lines.push("-- Initial admin accounts:");
  lines.push("--   login name: admin");
  lines.push("--   login name: developer");
  lines.push(
    "--   password credential values are placeholders and must be rotated before production use.",
  );
  lines.push("");
  lines.push("INSERT INTO `system_auth_principal_identity` (");
  lines.push(
    "    `id`, `principal_type`, `principal_id`, `identity_type`, `identity_value`, `status`",
  );
  lines.push(") VALUES");
  lines.push(
    users
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
      .join(",\n"),
  );
  lines.push("ON DUPLICATE KEY UPDATE");
  lines.push("    `principal_type` = VALUES(`principal_type`),");
  lines.push("    `principal_id` = VALUES(`principal_id`),");
  lines.push("    `identity_value` = VALUES(`identity_value`),");
  lines.push("    `status` = VALUES(`status`);");
  lines.push("");
  lines.push("INSERT INTO `system_auth_principal_credential` (");
  lines.push("    `id`, `principal_type`, `principal_id`, `identity_id`,");
  lines.push(
    "    `credential_type`, `credential_value`, `status`, `need_change_password`,",
  );
  lines.push("    `failed_count`, `failed_limit`");
  lines.push(") VALUES");
  lines.push(
    users
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
      .join(",\n"),
  );
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

const appendAutoIncrementSql = (
  lines: string[],
  targets: Array<{ table: string; nextValue: number }>,
) => {
  for (const target of targets) {
    lines.push(
      `ALTER TABLE \`${target.table}\` AUTO_INCREMENT = ${target.nextValue};`,
    );
  }
  lines.push("");
};

const row = (values: Array<string | number | null>) =>
  `    (${values.map(sqlValue).join(", ")})`;

const sqlValue = (value: string | number | null) => {
  if (value === null) {
    return "NULL";
  }
  if (typeof value === "number") {
    return String(value);
  }
  return `'${value.replace(/'/g, "''")}'`;
};

const indexBy = <T>(items: T[], keyFactory: (item: T) => string) => {
  const map = new Map<string, T>();
  for (const item of items) {
    const key = keyFactory(item);
    if (map.has(key)) {
      throw new Error(`Duplicate key: ${key}`);
    }
    map.set(key, item);
  }
  return map;
};

const requireLookup = <T>(map: Map<string, T>, key: string, type: string) => {
  const value = map.get(key);
  if (!value) {
    throw new Error(`Unknown ${type}: ${key}`);
  }
  return value;
};

main();
