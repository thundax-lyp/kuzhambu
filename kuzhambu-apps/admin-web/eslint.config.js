import js from "@eslint/js";
import prettier from "eslint-config-prettier";
import boundaries from "eslint-plugin-boundaries";
import reactHooks from "eslint-plugin-react-hooks";
import reactRefresh from "eslint-plugin-react-refresh";
import globals from "globals";
import fs from "node:fs";
import path from "node:path";
import tseslint from "typescript-eslint";

const SERVICE_METHOD_VERBS = [
    "page",
    "list",
    "get",
    "init",
    "add",
    "create",
    "complete",
    "remove",
    "delete",
    "abort",
    "update",
    "change",
    "sort",
    "move",
    "upload",
    "download",
    "reset",
    "login",
    "logout",
    "refresh",
    "load",
    "request",
    "latest",
    "search",
    "click",
    "rebuild",
    "preview",
    "apply",
    "deprecate",
    "reject",
    "review",
    "approve",
    "recover",
    "submit",
    "confirm",
    "cancel",
    "publish",
    "revoke",
    "regenerate"
];

const localRules = {
    rules: {
        "kebab-case-file-name": {
            create(context) {
                return {
                    Program(node) {
                        const fileName = path.basename(context.physicalFilename);
                        const extension = path.extname(fileName);
                        const name = fileName.slice(0, -extension.length);

                        if (
                            !/^[a-z0-9]+(?:-[a-z0-9]+)*(?:\.[a-z0-9]+(?:-[a-z0-9]+)*)*$/.test(name)
                        ) {
                            context.report({
                                node,
                                message:
                                    "ADMIN_WEB_NAME_FILE_KEBAB_CASE: frontend source file names must use kebab-case."
                            });
                        }
                    }
                };
            }
        },
        "no-nested-ternary": {
            create(context) {
                const isDirectJsxRenderBranch = (node) => {
                    return (
                        node.parent?.type === "ConditionalExpression" &&
                        node.parent.parent?.type === "JSXExpressionContainer"
                    );
                };

                return {
                    ConditionalExpression(node) {
                        if (node.parent?.type !== "ConditionalExpression") {
                            return;
                        }
                        if (isDirectJsxRenderBranch(node)) {
                            return;
                        }

                        context.report({
                            node,
                            message:
                                "ADMIN_WEB_NAME_NO_NESTED_TERNARY: nested ternary expressions are forbidden except direct three-branch JSX rendering."
                        });
                    }
                };
            }
        },
        "hook-file-path": {
            create(context) {
                const isHookFilePath = (normalizedFilePath) => {
                    return /\/hooks\/use-[a-z0-9]+(?:-[a-z0-9]+)*\.ts$/.test(normalizedFilePath);
                };

                const isHookName = (name) => /^use[A-Z]/.test(name);

                const reportHookOutsideHookFile = (node, name, normalizedFilePath) => {
                    if (!isHookName(name) || isHookFilePath(normalizedFilePath)) {
                        return;
                    }

                    context.report({
                        node,
                        message:
                            "ADMIN_WEB_PATH_HOOK_FILE: useXxx hook methods must live in hooks/use-<name>.ts."
                    });
                };

                return {
                    Program(node) {
                        const filePath = context.physicalFilename;
                        const normalizedFilePath = filePath.split(path.sep).join("/");
                        const fileName = path.basename(filePath);
                        const isHookFile = /^use-[a-z0-9]+(?:-[a-z0-9]+)*\.tsx?$/.test(fileName);

                        if (normalizedFilePath.includes("/hooks/")) {
                            if (!/^use-[a-z0-9]+(?:-[a-z0-9]+)*\.ts$/.test(fileName)) {
                                context.report({
                                    node,
                                    message:
                                        "ADMIN_WEB_PATH_HOOK_FILE: hook files must be named hooks/use-<name>.ts."
                                });
                            }
                            return;
                        }

                        if (isHookFile) {
                            context.report({
                                node,
                                message:
                                    "ADMIN_WEB_PATH_HOOK_FILE: use-*.ts hook files must live in a hooks/ directory."
                            });
                        }
                    },
                    FunctionDeclaration(node) {
                        const normalizedFilePath = context.physicalFilename
                            .split(path.sep)
                            .join("/");
                        const name = node.id?.name;
                        if (name) {
                            reportHookOutsideHookFile(node.id, name, normalizedFilePath);
                        }
                    },
                    VariableDeclarator(node) {
                        const normalizedFilePath = context.physicalFilename
                            .split(path.sep)
                            .join("/");

                        if (node.id.type === "Identifier") {
                            reportHookOutsideHookFile(node.id, node.id.name, normalizedFilePath);
                        }
                    },
                    ExportNamedDeclaration(node) {
                        const normalizedFilePath = context.physicalFilename
                            .split(path.sep)
                            .join("/");

                        node.specifiers.forEach((specifier) => {
                            if (specifier.exported?.type === "Identifier") {
                                reportHookOutsideHookFile(
                                    specifier.exported,
                                    specifier.exported.name,
                                    normalizedFilePath
                                );
                            }
                        });
                    }
                };
            }
        },
        "kuzhambu-component-name": {
            create(context) {
                const isKuzhambuName = (name) => /^Kuzhambu[A-Z]/.test(name);

                const reportKuzhambuNameOutsideSharedComponents = (
                    node,
                    name,
                    normalizedFilePath
                ) => {
                    if (!isKuzhambuName(name) || normalizedFilePath.includes("/src/components/")) {
                        return;
                    }

                    context.report({
                        node,
                        message:
                            "ADMIN_WEB_NAME_KUZHAMBU_COMPONENT: Kuzhambu* names may only be defined in src/components/."
                    });
                };

                const checkNamedNode = (node) => {
                    const normalizedFilePath = context.physicalFilename.split(path.sep).join("/");
                    const name = node.id?.name;
                    if (name) {
                        reportKuzhambuNameOutsideSharedComponents(
                            node.id,
                            name,
                            normalizedFilePath
                        );
                    }
                };

                return {
                    ClassDeclaration: checkNamedNode,
                    FunctionDeclaration: checkNamedNode,
                    TSEnumDeclaration: checkNamedNode,
                    TSInterfaceDeclaration: checkNamedNode,
                    TSTypeAliasDeclaration: checkNamedNode,
                    VariableDeclarator(node) {
                        const normalizedFilePath = context.physicalFilename
                            .split(path.sep)
                            .join("/");

                        if (node.id.type === "Identifier") {
                            reportKuzhambuNameOutsideSharedComponents(
                                node.id,
                                node.id.name,
                                normalizedFilePath
                            );
                        }
                    },
                    ExportNamedDeclaration(node) {
                        const normalizedFilePath = context.physicalFilename
                            .split(path.sep)
                            .join("/");

                        node.specifiers.forEach((specifier) => {
                            if (specifier.exported?.type === "Identifier") {
                                reportKuzhambuNameOutsideSharedComponents(
                                    specifier.exported,
                                    specifier.exported.name,
                                    normalizedFilePath
                                );
                            }
                        });
                    }
                };
            }
        },
        "no-console-log": {
            create(context) {
                return {
                    CallExpression(node) {
                        if (
                            node.callee.type !== "MemberExpression" ||
                            node.callee.object.type !== "Identifier" ||
                            node.callee.object.name !== "console" ||
                            node.callee.property.type !== "Identifier" ||
                            node.callee.property.name !== "log"
                        ) {
                            return;
                        }

                        context.report({
                            node,
                            message:
                                "ADMIN_WEB_CODE_NO_CONSOLE_LOG: console.log is forbidden; use a focused eslint-disable comment with a reason for temporary diagnostics."
                        });
                    }
                };
            }
        },
        "no-explicit-any": {
            create(context) {
                return {
                    TSAnyKeyword(node) {
                        context.report({
                            node,
                            message:
                                "ADMIN_WEB_CODE_NO_EXPLICIT_ANY: explicit any is forbidden; use a focused eslint-disable comment with a reason only when the boundary cannot be typed."
                        });
                    }
                };
            }
        },
        "no-antd-space-direct-in-pages": {
            create(context) {
                const readNormalizedFilePath = () => {
                    return context.physicalFilename.split(path.sep).join("/");
                };

                const isPageFile = (normalizedFilePath) => {
                    return (
                        normalizedFilePath.includes("/src/pages/") &&
                        /\.(?:ts|tsx)$/.test(normalizedFilePath)
                    );
                };

                return {
                    ImportDeclaration(node) {
                        if (node.source.value !== "antd" || !isPageFile(readNormalizedFilePath())) {
                            return;
                        }

                        node.specifiers.forEach((specifier) => {
                            if (
                                specifier.type !== "ImportSpecifier" ||
                                (specifier.imported.name !== "Space" &&
                                    specifier.imported.name !== "SpaceProps")
                            ) {
                                return;
                            }

                            context.report({
                                node: specifier,
                                message:
                                    "ADMIN_WEB_UI_NO_ANTD_SPACE_DIRECT_IN_PAGES: pages/**/*.{ts,tsx} must use KuzhambuSpace or KuzhambuSpaceCompact from src/components/kuzhambu-space/ instead of importing Space or SpaceProps from antd."
                            });
                        });
                    }
                };
            }
        },
        "no-antd-drawer-direct-in-pages": {
            create(context) {
                const readNormalizedFilePath = () => {
                    return context.physicalFilename.split(path.sep).join("/");
                };

                const isPageFile = (normalizedFilePath) => {
                    return (
                        normalizedFilePath.includes("/src/pages/") &&
                        /\.(?:ts|tsx)$/.test(normalizedFilePath)
                    );
                };

                return {
                    ImportDeclaration(node) {
                        if (node.source.value !== "antd" || !isPageFile(readNormalizedFilePath())) {
                            return;
                        }

                        node.specifiers.forEach((specifier) => {
                            if (specifier.type !== "ImportSpecifier") {
                                return;
                            }

                            if (specifier.imported.name !== "Drawer") {
                                return;
                            }

                            context.report({
                                node: specifier,
                                message:
                                    "ADMIN_WEB_UI_NO_ANTD_DRAWER_DIRECT_IN_PAGES: pages/**/*.{ts,tsx} must use KuzhambuDrawer from src/components/kuzhambu-drawer/ instead of importing Drawer from antd."
                            });
                        });
                    }
                };
            }
        },
        "no-antd-modal-direct-in-pages": {
            create(context) {
                const readNormalizedFilePath = () => {
                    return context.physicalFilename.split(path.sep).join("/");
                };

                const isPageFile = (normalizedFilePath) => {
                    return (
                        normalizedFilePath.includes("/src/pages/") &&
                        /\.(?:ts|tsx)$/.test(normalizedFilePath)
                    );
                };

                return {
                    ImportDeclaration(node) {
                        if (node.source.value !== "antd" || !isPageFile(readNormalizedFilePath())) {
                            return;
                        }

                        node.specifiers.forEach((specifier) => {
                            if (
                                specifier.type !== "ImportSpecifier" ||
                                specifier.imported.name !== "Modal"
                            ) {
                                return;
                            }

                            context.report({
                                node: specifier,
                                message:
                                    "ADMIN_WEB_UI_NO_ANTD_MODAL_DIRECT_IN_PAGES: pages/**/*.{ts,tsx} must use KuzhambuModal from src/components/kuzhambu-modal/ instead of importing Modal from antd."
                            });
                        });
                    }
                };
            }
        },
        "no-antd-alert-direct-in-pages": {
            create(context) {
                const readNormalizedFilePath = () => {
                    return context.physicalFilename.split(path.sep).join("/");
                };

                const isPageFile = (normalizedFilePath) => {
                    return (
                        normalizedFilePath.includes("/src/pages/") &&
                        /\.(?:ts|tsx)$/.test(normalizedFilePath)
                    );
                };

                return {
                    ImportDeclaration(node) {
                        if (node.source.value !== "antd" || !isPageFile(readNormalizedFilePath())) {
                            return;
                        }

                        node.specifiers.forEach((specifier) => {
                            if (
                                specifier.type !== "ImportSpecifier" ||
                                specifier.imported.name !== "Alert"
                            ) {
                                return;
                            }

                            context.report({
                                node: specifier,
                                message:
                                    "ADMIN_WEB_UI_NO_ANTD_ALERT_DIRECT_IN_PAGES: pages/**/*.{ts,tsx} must use KuzhambuAlert from src/components/kuzhambu-alert/ instead of importing Alert from antd."
                            });
                        });
                    }
                };
            }
        },
        "no-antd-button-direct-in-pages": {
            create(context) {
                const readNormalizedFilePath = () => {
                    return context.physicalFilename.split(path.sep).join("/");
                };

                const isPageFile = (normalizedFilePath) => {
                    return (
                        normalizedFilePath.includes("/src/pages/") &&
                        /\.(?:ts|tsx)$/.test(normalizedFilePath)
                    );
                };

                return {
                    ImportDeclaration(node) {
                        if (node.source.value !== "antd" || !isPageFile(readNormalizedFilePath())) {
                            return;
                        }

                        node.specifiers.forEach((specifier) => {
                            if (
                                specifier.type !== "ImportSpecifier" ||
                                specifier.imported.name !== "Button"
                            ) {
                                return;
                            }

                            context.report({
                                node: specifier,
                                message:
                                    "ADMIN_WEB_UI_NO_ANTD_BUTTON_DIRECT_IN_PAGES: pages/**/*.{ts,tsx} must use KuzhambuButton from src/components/kuzhambu-button/ instead of importing Button from antd."
                            });
                        });
                    }
                };
            }
        },
        "shared-component-css-local": {
            create(context) {
                return {
                    ImportDeclaration(node) {
                        const normalizedFilePath = context.physicalFilename
                            .split(path.sep)
                            .join("/");
                        const importPath = node.source.value;

                        if (
                            typeof importPath !== "string" ||
                            !normalizedFilePath.includes("/src/components/") ||
                            !importPath.startsWith("../") ||
                            !importPath.endsWith(".css")
                        ) {
                            return;
                        }

                        context.report({
                            node,
                            message:
                                "ADMIN_WEB_LAYER_SHARED_COMPONENT_CSS_LOCAL: shared components must import CSS from their own directory."
                        });
                    }
                };
            }
        },
        "props-camel-case-property": {
            create(context) {
                const isPropsName = (name) => /Props$/.test(name);

                const checkMembers = (members) => {
                    members.forEach((member) => {
                        if (
                            member.type !== "TSPropertySignature" ||
                            member.key.type !== "Literal" ||
                            typeof member.key.value !== "string"
                        ) {
                            return;
                        }

                        context.report({
                            node: member.key,
                            message: `ADMIN_WEB_NAME_PROPS_CAMEL_CASE: *Props property "${member.key.value}" must be exposed as camelCase. Convert to native DOM attributes only at the render boundary.`
                        });
                    });
                };

                return {
                    TSInterfaceDeclaration(node) {
                        if (!isPropsName(node.id.name)) {
                            return;
                        }
                        checkMembers(node.body.body);
                    },
                    TSTypeAliasDeclaration(node) {
                        if (
                            !isPropsName(node.id.name) ||
                            node.typeAnnotation.type !== "TSTypeLiteral"
                        ) {
                            return;
                        }
                        checkMembers(node.typeAnnotation.members);
                    }
                };
            }
        },
        "e2e-spec-file-path": {
            create(context) {
                return {
                    Program(node) {
                        const filePath = context.physicalFilename;
                        const normalizedFilePath = filePath.split(path.sep).join("/");

                        if (
                            !normalizedFilePath.includes("/e2e/") ||
                            !normalizedFilePath.endsWith(".spec.ts")
                        ) {
                            return;
                        }

                        const isLayoutSpec = /\/e2e\/layout\/[^/]+\.spec\.ts$/.test(
                            normalizedFilePath
                        );
                        const isPageSpec = /\/e2e\/[^/]+\/([^/]+)\/\1\.spec\.ts$/.test(
                            normalizedFilePath
                        );

                        if (!isLayoutSpec && !isPageSpec) {
                            context.report({
                                node,
                                message:
                                    "ADMIN_WEB_PATH_E2E_PAGE_SPEC / ADMIN_WEB_PATH_E2E_LAYOUT_SPEC: e2e specs must live in e2e/<module>/<domain>/<domain>.spec.ts or e2e/layout/*.spec.ts."
                            });
                        }
                    }
                };
            }
        },
        "page-class-name-prefix": {
            create(context) {
                const readPageDomain = () => {
                    const normalizedFilePath = context.physicalFilename.split(path.sep).join("/");
                    const match = normalizedFilePath.match(
                        /\/src\/pages\/[^/]+\/([^/]+)\/\1-page\.tsx$/
                    );
                    return match?.[1] ?? "";
                };

                const readClassText = (attribute) => {
                    if (!attribute.value) {
                        return "";
                    }

                    if (
                        attribute.value.type === "Literal" &&
                        typeof attribute.value.value === "string"
                    ) {
                        return attribute.value.value;
                    }

                    if (
                        attribute.value.type === "JSXExpressionContainer" &&
                        attribute.value.expression.type === "TemplateLiteral"
                    ) {
                        return attribute.value.expression.quasis
                            .map((quasi) => quasi.value.cooked ?? "")
                            .join(" ");
                    }

                    return "";
                };

                const hasPageDomainClass = (attribute, pageDomain) => {
                    return readClassText(attribute)
                        .split(/\s+/)
                        .some((className) => className.startsWith(`${pageDomain}-`));
                };

                return {
                    Program(node) {
                        const pageDomain = readPageDomain();
                        if (!pageDomain) {
                            return;
                        }

                        let hasPageRootClassName = false;

                        const visit = (currentNode) => {
                            if (!currentNode || typeof currentNode !== "object") {
                                return;
                            }

                            if (
                                currentNode.type === "JSXAttribute" &&
                                (currentNode.name.name === "className" ||
                                    currentNode.name.name === "pageClassName") &&
                                hasPageDomainClass(currentNode, pageDomain)
                            ) {
                                hasPageRootClassName = true;
                                return;
                            }

                            Object.keys(currentNode).forEach((key) => {
                                if (hasPageRootClassName || key === "parent") {
                                    return;
                                }

                                const value = currentNode[key];
                                if (Array.isArray(value)) {
                                    value.forEach(visit);
                                    return;
                                }
                                visit(value);
                            });
                        };

                        visit(node);

                        if (!hasPageRootClassName) {
                            context.report({
                                node,
                                message: `ADMIN_WEB_NAME_PAGE_CLASS_PREFIX: page root className must include a "${pageDomain}-" class.`
                            });
                        }
                    }
                };
            }
        },
        "page-style-file": {
            create(context) {
                return {
                    Program(node) {
                        const filePath = context.physicalFilename;
                        const normalizedFilePath = filePath.split(path.sep).join("/");
                        const fileName = path.basename(filePath);

                        if (
                            !normalizedFilePath.includes("/src/pages/") ||
                            !fileName.endsWith("-page.tsx")
                        ) {
                            return;
                        }

                        const styleFileName = fileName.replace(/\.tsx$/, ".css");
                        const styleFilePath = path.join(path.dirname(filePath), styleFileName);
                        const requiredImport = `./${styleFileName}`;
                        const hasStyleImport = node.body.some((statement) => {
                            return (
                                statement.type === "ImportDeclaration" &&
                                statement.source.value === requiredImport
                            );
                        });

                        if (!fs.existsSync(styleFilePath)) {
                            context.report({
                                node,
                                message: `ADMIN_WEB_NAME_PAGE_STYLE_FILE: page ${fileName} must have sibling style file ${styleFileName}.`
                            });
                            return;
                        }

                        if (!hasStyleImport) {
                            context.report({
                                node,
                                message: `ADMIN_WEB_NAME_PAGE_STYLE_FILE: page ${fileName} must explicitly import "${requiredImport}".`
                            });
                        }
                    }
                };
            }
        },
        "page-no-parent-relative-import": {
            create(context) {
                return {
                    ImportDeclaration(node) {
                        const filePath = context.physicalFilename;
                        const normalizedFilePath = filePath.split(path.sep).join("/");
                        const importPath = node.source.value;

                        if (
                            !normalizedFilePath.includes("/src/pages/") ||
                            !normalizedFilePath.endsWith("-page.tsx") ||
                            typeof importPath !== "string" ||
                            !importPath.startsWith("../")
                        ) {
                            return;
                        }

                        context.report({
                            node,
                            message:
                                "ADMIN_WEB_LAYER_PAGE_NO_PARENT_RELATIVE_IMPORT: page files must use ./ for same page-domain imports and @/ for cross-domain or shared imports."
                        });
                    }
                };
            }
        },
        "page-component-no-external-page": {
            create(context) {
                const readPageDomainRoot = (normalizedFilePath) => {
                    const match = normalizedFilePath.match(/\/src\/pages\/[^/]+\/[^/]+\//);
                    return match?.[0];
                };

                const readPageModuleRoot = (normalizedFilePath) => {
                    const match = normalizedFilePath.match(/\/src\/pages\/[^/]+\//);
                    return match?.[0];
                };

                return {
                    ImportDeclaration(node) {
                        const filePath = context.physicalFilename;
                        const normalizedFilePath = filePath.split(path.sep).join("/");
                        const importPath = node.source.value;
                        const pageDomainRoot = readPageDomainRoot(normalizedFilePath);
                        const pageModuleRoot = readPageModuleRoot(normalizedFilePath);

                        if (
                            !pageDomainRoot ||
                            !normalizedFilePath.includes(`${pageDomainRoot}components/`) ||
                            typeof importPath !== "string"
                        ) {
                            return;
                        }

                        if (importPath.startsWith("@/pages/")) {
                            const resolvedImportPath = `/src/${importPath.slice(2)}`;
                            const importModuleRoot = readPageModuleRoot(resolvedImportPath);
                            const importPageDomainRoot = readPageDomainRoot(resolvedImportPath);
                            const isSamePageDomainImport = importPageDomainRoot === pageDomainRoot;
                            const isModuleCommonImport =
                                pageModuleRoot &&
                                importModuleRoot === pageModuleRoot &&
                                importPageDomainRoot?.endsWith("/common/");

                            if (isSamePageDomainImport || isModuleCommonImport) {
                                return;
                            }

                            context.report({
                                node,
                                message:
                                    "ADMIN_WEB_LAYER_PAGE_COMPONENT_NO_EXTERNAL_PAGE: page components must not import from other page domains."
                            });
                            return;
                        }

                        if (!importPath.startsWith(".")) {
                            return;
                        }

                        const resolvedImportPath = path
                            .resolve(path.dirname(filePath), importPath)
                            .split(path.sep)
                            .join("/");

                        if (!resolvedImportPath.includes(pageDomainRoot)) {
                            context.report({
                                node,
                                message:
                                    "ADMIN_WEB_LAYER_PAGE_COMPONENT_NO_EXTERNAL_PAGE: page components may only use relative imports inside their own page domain."
                            });
                        }
                    }
                };
            }
        },
        "page-no-external-service": {
            create(context) {
                const readPageDomainRoot = (normalizedFilePath) => {
                    const match = normalizedFilePath.match(/\/src\/pages\/[^/]+\/[^/]+\//);
                    return match?.[0];
                };

                const readPageModuleRoot = (normalizedFilePath) => {
                    const match = normalizedFilePath.match(/\/src\/pages\/[^/]+\//);
                    return match?.[0];
                };

                const resolveImportPath = (filePath, importPath) => {
                    if (importPath.startsWith("@/")) {
                        return `/src/${importPath.slice(2)}`;
                    }
                    if (!importPath.startsWith(".")) {
                        return importPath;
                    }
                    return path
                        .resolve(path.dirname(filePath), importPath)
                        .split(path.sep)
                        .join("/");
                };

                return {
                    ImportDeclaration(node) {
                        const filePath = context.physicalFilename;
                        const normalizedFilePath = filePath.split(path.sep).join("/");
                        const importPath = node.source.value;
                        const pageDomainRoot = readPageDomainRoot(normalizedFilePath);

                        if (
                            !pageDomainRoot ||
                            typeof importPath !== "string" ||
                            !normalizedFilePath.includes("/src/pages/")
                        ) {
                            return;
                        }

                        const resolvedImportPath = resolveImportPath(filePath, importPath);
                        const normalizedPageDomainRoot = readPageDomainRoot(resolvedImportPath);
                        if (
                            !resolvedImportPath.includes("/src/pages/") ||
                            !resolvedImportPath.endsWith("-service.ts")
                        ) {
                            return;
                        }

                        const pageModuleRoot = readPageModuleRoot(normalizedFilePath);
                        const importModuleRoot = readPageModuleRoot(resolvedImportPath);
                        const isModuleCommonService =
                            pageModuleRoot &&
                            importModuleRoot === pageModuleRoot &&
                            normalizedPageDomainRoot?.endsWith("/common/");

                        if (isModuleCommonService) {
                            return;
                        }

                        if (normalizedPageDomainRoot !== pageDomainRoot) {
                            context.report({
                                node,
                                message:
                                    "ADMIN_WEB_LAYER_PAGE_NO_EXTERNAL_SERVICE: page domains must not import services from other page domains."
                            });
                        }
                    }
                };
            }
        },
        "page-component-single-export": {
            create(context) {
                const isPascalCase = (name) => /^[A-Z][A-Za-z0-9]*$/.test(name);

                const readExportedName = (specifier) => {
                    if (specifier.exported?.type === "Identifier") {
                        return specifier.exported.name;
                    }
                    if (specifier.local?.type === "Identifier") {
                        return specifier.local.name;
                    }
                    return "";
                };

                return {
                    Program(node) {
                        const filePath = context.physicalFilename;
                        const normalizedFilePath = filePath.split(path.sep).join("/");

                        if (
                            !normalizedFilePath.includes("/src/pages/") ||
                            !normalizedFilePath.includes("/components/") ||
                            !normalizedFilePath.endsWith(".tsx")
                        ) {
                            return;
                        }

                        const exportedComponents = [];
                        node.body.forEach((statement) => {
                            if (statement.type !== "ExportNamedDeclaration") {
                                return;
                            }

                            if (statement.declaration?.type === "VariableDeclaration") {
                                statement.declaration.declarations.forEach((declaration) => {
                                    if (
                                        declaration.id.type === "Identifier" &&
                                        isPascalCase(declaration.id.name)
                                    ) {
                                        exportedComponents.push(declaration.id.name);
                                    }
                                });
                            }

                            if (
                                statement.declaration?.type === "FunctionDeclaration" &&
                                statement.declaration.id &&
                                isPascalCase(statement.declaration.id.name)
                            ) {
                                exportedComponents.push(statement.declaration.id.name);
                            }

                            statement.specifiers.forEach((specifier) => {
                                const exportedName = readExportedName(specifier);
                                if (isPascalCase(exportedName)) {
                                    exportedComponents.push(exportedName);
                                }
                            });
                        });

                        if (exportedComponents.length > 1) {
                            context.report({
                                node,
                                message: `ADMIN_WEB_COMPONENT_SINGLE_EXPORT: page component files may export only one PascalCase component; move sibling components to separate files. Found ${exportedComponents.join(", ")}.`
                            });
                        }
                    }
                };
            }
        },
        "post-helper-service-only": {
            create(context) {
                return {
                    ImportDeclaration(node) {
                        const importPath = node.source.value;
                        if (
                            importPath !== "@/api/http" &&
                            importPath !== "../api/http" &&
                            importPath !== "./api/http"
                        ) {
                            return;
                        }

                        const importsPostHelper = node.specifiers.some((specifier) => {
                            return (
                                specifier.type === "ImportSpecifier" &&
                                (specifier.imported.name === "postJson" ||
                                    specifier.imported.name === "postFormData")
                            );
                        });
                        if (!importsPostHelper) {
                            return;
                        }

                        const fileName = path.basename(context.physicalFilename);
                        if (fileName.endsWith("-service.ts")) {
                            return;
                        }

                        context.report({
                            node,
                            message:
                                "ADMIN_WEB_LAYER_POST_HELPER_SERVICE_ONLY / ADMIN_WEB_LAYER_QUERY_FN_FROM_SERVICE: postJson and postFormData may only be imported by *-service.ts files."
                        });
                    }
                };
            }
        },
        "shared-service-types-only": {
            create(context) {
                const isSharedServiceFile = () => {
                    const normalizedFilePath = context.physicalFilename.split(path.sep).join("/");
                    return /\/src\/service\/[^/]+-service\.ts$/.test(normalizedFilePath);
                };

                const isAllowedSharedTypeImport = (importPath) => {
                    return /(?:^|\/)[^/]+-types$/.test(importPath);
                };

                const isSharedServiceInternalImport = (resolvedImportPath) => {
                    return resolvedImportPath.includes("/src/service/");
                };

                const resolveImportPath = (importPath) => {
                    if (!importPath.startsWith(".")) {
                        return importPath;
                    }
                    return path
                        .resolve(path.dirname(context.physicalFilename), importPath)
                        .split(path.sep)
                        .join("/");
                };

                return {
                    ImportDeclaration(node) {
                        if (!isSharedServiceFile() || typeof node.source.value !== "string") {
                            return;
                        }

                        const importPath = node.source.value;
                        const resolvedImportPath = resolveImportPath(importPath);
                        if (
                            importPath.startsWith("@/pages/") ||
                            resolvedImportPath.includes("/src/pages/")
                        ) {
                            context.report({
                                node,
                                message:
                                    "ADMIN_WEB_LAYER_SHARED_SERVICE_TYPES_ONLY: shared services must not import page files."
                            });
                            return;
                        }

                        if (
                            (importPath.startsWith("@/service/") ||
                                isSharedServiceInternalImport(resolvedImportPath)) &&
                            !/(?:^|\/)[^/]+-service$/.test(importPath) &&
                            !/(?:^|\/)[^/]+-service$/.test(resolvedImportPath) &&
                            !isAllowedSharedTypeImport(importPath)
                        ) {
                            context.report({
                                node,
                                message:
                                    "ADMIN_WEB_LAYER_SHARED_SERVICE_TYPES_ONLY: shared service types must be imported from *-types.ts boundaries."
                            });
                        }
                    }
                };
            }
        },
        "component-index-export-only": {
            create(context) {
                return {
                    Program(node) {
                        const normalizedFilePath = context.physicalFilename
                            .split(path.sep)
                            .join("/");
                        if (
                            !normalizedFilePath.includes("/src/components/") ||
                            !normalizedFilePath.endsWith("/index.ts")
                        ) {
                            return;
                        }

                        node.body.forEach((statement) => {
                            if (statement.type === "ExportAllDeclaration" && statement.source) {
                                return;
                            }

                            if (statement.type === "ExportNamedDeclaration" && statement.source) {
                                return;
                            }

                            context.report({
                                node: statement,
                                message:
                                    "ADMIN_WEB_LAYER_COMPONENT_INDEX_EXPORT_ONLY: component index.ts files may contain re-export declarations only."
                            });
                        });
                    }
                };
            }
        },
        "confirm-hook-only": {
            create(context) {
                const isConfirmHookFile = () => {
                    return context.physicalFilename
                        .split(path.sep)
                        .join("/")
                        .endsWith(
                            "/src/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm.ts"
                        );
                };

                return {
                    CallExpression(node) {
                        if (
                            isConfirmHookFile() ||
                            node.callee.type !== "MemberExpression" ||
                            node.callee.object.type !== "Identifier" ||
                            node.callee.object.name !== "Modal" ||
                            node.callee.property.type !== "Identifier" ||
                            node.callee.property.name !== "confirm"
                        ) {
                            return;
                        }

                        context.report({
                            node,
                            message:
                                "ADMIN_WEB_UI_CONFIRM_HOOK: confirmation actions must use useKuzhambuConfirm instead of Modal.confirm."
                        });
                    }
                };
            }
        },
        "table-action-column-shape": {
            create(context) {
                const isPageFile = () => {
                    const normalizedFilePath = context.physicalFilename.split(path.sep).join("/");
                    return /\/src\/pages\/.*\/[^/]+-page\.tsx$/.test(normalizedFilePath);
                };

                const readPropertyName = (property) => {
                    if (property.type !== "Property") {
                        return "";
                    }
                    if (property.key.type === "Identifier") {
                        return property.key.name;
                    }
                    if (property.key.type === "Literal") {
                        return String(property.key.value);
                    }
                    return "";
                };

                const isActionsColumn = (node) => {
                    return node.properties.some((property) => {
                        if (property.type !== "Property" || readPropertyName(property) !== "key") {
                            return false;
                        }
                        return (
                            property.value.type === "Literal" && property.value.value === "actions"
                        );
                    });
                };

                return {
                    ObjectExpression(node) {
                        if (!isPageFile() || !isActionsColumn(node)) {
                            return;
                        }

                        const propertyNames = node.properties.map(readPropertyName);
                        const hasOptions = propertyNames.includes("options");
                        const hasRender = propertyNames.includes("render");

                        node.properties.forEach((property) => {
                            const name = readPropertyName(property);
                            if (name !== "title" && name !== "width") {
                                return;
                            }

                            context.report({
                                node: property,
                                message:
                                    "ADMIN_WEB_UI_TABLE_ACTION_COLUMN: actions column title and width are handled by KuzhambuTable."
                            });
                        });

                        if (!hasOptions && !hasRender) {
                            context.report({
                                node,
                                message:
                                    "ADMIN_WEB_UI_TABLE_ACTION_COLUMN: actions column must provide options or render."
                            });
                        }
                    }
                };
            }
        },
        "interactive-accessible-name": {
            create(context) {
                const readJsxName = (nameNode) => {
                    if (nameNode.type === "JSXIdentifier") {
                        return nameNode.name;
                    }
                    if (nameNode.type === "JSXMemberExpression") {
                        return `${readJsxName(nameNode.object)}.${readJsxName(nameNode.property)}`;
                    }
                    return "";
                };

                const hasAttribute = (node, attributeName) => {
                    return node.attributes.some((attribute) => {
                        return (
                            attribute.type === "JSXAttribute" &&
                            attribute.name.type === "JSXIdentifier" &&
                            attribute.name.name === attributeName
                        );
                    });
                };

                const hasSpreadAttribute = (node) => {
                    return node.attributes.some(
                        (attribute) => attribute.type === "JSXSpreadAttribute"
                    );
                };

                const hasAccessibleName = (node) => {
                    return (
                        hasAttribute(node, "aria-label") ||
                        hasAttribute(node, "aria-labelledby") ||
                        hasAttribute(node, "ariaLabel") ||
                        hasAttribute(node, "ariaLabelledBy")
                    );
                };

                const hasVisibleText = (node) => {
                    if (!node || node.type !== "JSXElement") {
                        return false;
                    }

                    return node.children.some((child) => {
                        if (child.type === "JSXText") {
                            return child.value.trim().length > 0;
                        }
                        if (child.type === "JSXExpressionContainer") {
                            return child.expression.type !== "JSXEmptyExpression";
                        }
                        if (child.type === "JSXElement") {
                            return hasVisibleText(child);
                        }
                        return false;
                    });
                };

                const reportMissingAccessibleName = (node, componentName) => {
                    context.report({
                        node,
                        message: `ADMIN_WEB_UI_INTERACTIVE_ACCESSIBLE_NAME: ${componentName} must have a stable accessible name. Use visible text, ariaLabel/ariaLabelledBy for custom components, or native aria-label/aria-labelledby on DOM elements.`
                    });
                };

                return {
                    JSXOpeningElement(node) {
                        const componentName = readJsxName(node.name);
                        if (componentName === "Button") {
                            if (!hasAccessibleName(node) && !hasVisibleText(node.parent)) {
                                reportMissingAccessibleName(node, componentName);
                            }
                            return;
                        }

                        if (componentName === "Input.Search") {
                            if (!hasAccessibleName(node) && !hasSpreadAttribute(node)) {
                                reportMissingAccessibleName(node, componentName);
                            }
                            return;
                        }

                        if (componentName === "Table" || componentName === "KuzhambuTable") {
                            if (!hasAccessibleName(node) && !hasSpreadAttribute(node)) {
                                reportMissingAccessibleName(node, componentName);
                            }
                        }
                    }
                };
            }
        },
        "service-method-verb-prefix": {
            create(context) {
                const startsWithServiceVerb = (name) => {
                    return SERVICE_METHOD_VERBS.some((verb) => {
                        return (
                            name === verb ||
                            name.startsWith(`${verb}${name.charAt(verb.length).toUpperCase()}`)
                        );
                    });
                };

                const reportInvalidServiceMethod = (node, name) => {
                    if (startsWithServiceVerb(name)) {
                        return;
                    }

                    context.report({
                        node,
                        message: `ADMIN_WEB_NAME_SERVICE_METHOD: service method "${name}" must start with one of ${SERVICE_METHOD_VERBS.join(", ")}.`
                    });
                };

                return {
                    ExportNamedDeclaration(node) {
                        const filePath = context.physicalFilename;
                        if (!filePath.endsWith("-service.ts")) {
                            return;
                        }

                        if (node.declaration?.type === "VariableDeclaration") {
                            node.declaration.declarations.forEach((declaration) => {
                                if (declaration.id.type === "Identifier") {
                                    reportInvalidServiceMethod(declaration.id, declaration.id.name);
                                }
                            });
                        }

                        node.specifiers.forEach((specifier) => {
                            if (specifier.exported?.type === "Identifier") {
                                reportInvalidServiceMethod(
                                    specifier.exported,
                                    specifier.exported.name
                                );
                            }
                        });
                    }
                };
            }
        },
        "service-method-input-shape": {
            create(context) {
                const scalarTypeNames = ["Blob", "File", "FormData", "Date"];
                const isServiceInputName = (name) => /(?:Query|Command)$/.test(name);

                const readTypeAnnotation = (param) => {
                    if (param.type === "AssignmentPattern") {
                        return param.left.typeAnnotation?.typeAnnotation;
                    }
                    return param.typeAnnotation?.typeAnnotation;
                };

                const readTypeName = (typeNode) => {
                    if (!typeNode || typeNode.type !== "TSTypeReference") {
                        return "";
                    }
                    if (typeNode.typeName.type === "Identifier") {
                        return typeNode.typeName.name;
                    }
                    return "";
                };

                const readTypeArguments = (typeNode) => {
                    return (
                        typeNode?.typeArguments?.params ?? typeNode?.typeParameters?.params ?? []
                    );
                };

                const isPlainType = (typeNode) => {
                    if (!typeNode) {
                        return false;
                    }

                    if (
                        [
                            "TSBooleanKeyword",
                            "TSStringKeyword",
                            "TSNumberKeyword",
                            "TSNullKeyword",
                            "TSUndefinedKeyword",
                            "TSLiteralType"
                        ].includes(typeNode.type)
                    ) {
                        return true;
                    }

                    if (typeNode.type === "TSArrayType") {
                        return isPlainType(typeNode.elementType);
                    }

                    if (typeNode.type === "TSUnionType") {
                        return typeNode.types.every(isPlainType);
                    }

                    const name = readTypeName(typeNode);
                    if (scalarTypeNames.includes(name)) {
                        return true;
                    }

                    if (name === "Array" || name === "ReadonlyArray") {
                        return readTypeArguments(typeNode).every(isPlainType);
                    }

                    return false;
                };

                const reportInvalidInput = (node) => {
                    context.report({
                        node,
                        message:
                            "ADMIN_WEB_NAME_SERVICE_METHOD_INPUT: service method parameters must be void, one XxxQuery, one XxxCommand, or up to 3 plain parameters."
                    });
                };

                const checkInputShape = (node, params) => {
                    if (params.length === 0) {
                        return;
                    }

                    if (params.length > 3) {
                        reportInvalidInput(node);
                        return;
                    }

                    const typeNodes = params.map(readTypeAnnotation);
                    if (params.length === 1) {
                        const name = readTypeName(typeNodes[0]);
                        if (isServiceInputName(name) || isPlainType(typeNodes[0])) {
                            return;
                        }
                        reportInvalidInput(params[0]);
                        return;
                    }

                    if (typeNodes.every(isPlainType)) {
                        return;
                    }

                    reportInvalidInput(node);
                };

                return {
                    ExportNamedDeclaration(node) {
                        const filePath = context.physicalFilename;
                        if (!filePath.endsWith("-service.ts")) {
                            return;
                        }

                        if (node.declaration?.type !== "VariableDeclaration") {
                            return;
                        }

                        node.declaration.declarations.forEach((declaration) => {
                            if (
                                declaration.id.type !== "Identifier" ||
                                declaration.init?.type !== "ArrowFunctionExpression"
                            ) {
                                return;
                            }

                            checkInputShape(declaration.id, declaration.init.params);
                        });
                    }
                };
            }
        },
        "api-contract-type-location": {
            create(context) {
                const isApiContractName = (name) => /(?:Request|Response)$/.test(name);

                const isAllowedFile = () => {
                    const normalizedFilePath = context.physicalFilename.split(path.sep).join("/");
                    return (
                        normalizedFilePath.endsWith("-service.ts") ||
                        normalizedFilePath.includes("/src/api/")
                    );
                };

                const reportInvalidApiContractType = (node, name) => {
                    if (!isApiContractName(name) || isAllowedFile()) {
                        return;
                    }

                    context.report({
                        node,
                        message:
                            "ADMIN_WEB_NAME_API_CONTRACT_TYPE_LOCATION: XxxRequest/XxxResponse types may only be defined in *-service.ts or src/api/."
                    });
                };

                return {
                    TSInterfaceDeclaration(node) {
                        reportInvalidApiContractType(node.id, node.id.name);
                    },
                    TSTypeAliasDeclaration(node) {
                        reportInvalidApiContractType(node.id, node.id.name);
                    }
                };
            }
        },
        "service-input-type-location": {
            create(context) {
                const isServiceInputName = (name) => /(?:Query|Command)$/.test(name);

                const isAllowedFile = () => {
                    const normalizedFilePath = context.physicalFilename.split(path.sep).join("/");
                    return (
                        normalizedFilePath.endsWith("-service.ts") ||
                        normalizedFilePath.endsWith("/src/types/page.ts")
                    );
                };

                const reportInvalidServiceInputType = (node, name) => {
                    if (!isServiceInputName(name) || isAllowedFile()) {
                        return;
                    }

                    context.report({
                        node,
                        message:
                            "ADMIN_WEB_NAME_SERVICE_INPUT_TYPE_LOCATION: XxxQuery/XxxCommand types may only be defined in *-service.ts."
                    });
                };

                return {
                    TSInterfaceDeclaration(node) {
                        reportInvalidServiceInputType(node.id, node.id.name);
                    },
                    TSTypeAliasDeclaration(node) {
                        reportInvalidServiceInputType(node.id, node.id.name);
                    }
                };
            }
        },
        "business-data-type-location": {
            create(context) {
                const isBusinessDataName = (name) => /(?:Record|Node)$/.test(name);
                const isOptionDataName = (name) => /(?:OptionRecord|OptionsRecord)$/.test(name);

                const isAllowedFile = () => {
                    const normalizedFilePath = context.physicalFilename.split(path.sep).join("/");
                    return (
                        /\/src\/pages\/[^/]+\/([^/]+)\/\1-types\.ts$/.test(normalizedFilePath) ||
                        /\/src\/pages\/[^/]+\/common\/[^/]+-types\.ts$/.test(normalizedFilePath) ||
                        /\/src\/service\/[^/]+-types\.ts$/.test(normalizedFilePath) ||
                        /\/src\/auth\/[^/]+-types\.ts$/.test(normalizedFilePath)
                    );
                };

                const reportInvalidBusinessDataType = (node, name) => {
                    if (!isBusinessDataName(name) || isOptionDataName(name) || isAllowedFile()) {
                        return;
                    }

                    context.report({
                        node,
                        message:
                            "ADMIN_WEB_NAME_BUSINESS_DATA_TYPE_LOCATION: XxxRecord/XxxNode types may only be defined in a clear *-types.ts boundary."
                    });
                };

                return {
                    TSInterfaceDeclaration(node) {
                        reportInvalidBusinessDataType(node.id, node.id.name);
                    },
                    TSTypeAliasDeclaration(node) {
                        reportInvalidBusinessDataType(node.id, node.id.name);
                    }
                };
            }
        },
        "service-type-exposure": {
            create(context) {
                const isApiContractName = (name) => /(?:Request|Response)$/.test(name);
                const isBusinessDataName = (name) => /(?:Record|Node)$/.test(name);

                const normalizedFilePath = context.physicalFilename.split(path.sep).join("/");
                const isServiceFile = normalizedFilePath.endsWith("-service.ts");

                const resolveImportPath = (importPath) => {
                    if (importPath.startsWith("@/")) {
                        return `/src/${importPath.slice(2)}`;
                    }
                    if (!importPath.startsWith(".")) {
                        return importPath;
                    }
                    return path
                        .resolve(path.dirname(context.physicalFilename), importPath)
                        .split(path.sep)
                        .join("/");
                };

                const isServiceImport = (importPath) => {
                    const resolvedImportPath = resolveImportPath(importPath);
                    return /(?:^|\/)[^/]+-service(?:\.ts)?$/.test(resolvedImportPath);
                };

                const readExportName = (specifier) => {
                    if (specifier.exported?.type === "Identifier") {
                        return specifier.exported.name;
                    }
                    if (specifier.local?.type === "Identifier") {
                        return specifier.local.name;
                    }
                    return "";
                };

                const readImportedName = (specifier) => {
                    if (specifier.imported?.type === "Identifier") {
                        return specifier.imported.name;
                    }
                    if (specifier.local?.type === "Identifier") {
                        return specifier.local.name;
                    }
                    return "";
                };

                const reportApiContractExposure = (node, name) => {
                    if (!isApiContractName(name)) {
                        return;
                    }

                    context.report({
                        node,
                        message:
                            "ADMIN_WEB_NAME_API_CONTRACT_TYPE_EXPOSURE: XxxRequest/XxxResponse are private API contract types and must not be exported from service."
                    });
                };

                return {
                    ExportNamedDeclaration(node) {
                        if (!isServiceFile) {
                            return;
                        }

                        if (
                            (node.declaration?.type === "TSInterfaceDeclaration" ||
                                node.declaration?.type === "TSTypeAliasDeclaration") &&
                            node.declaration.id
                        ) {
                            reportApiContractExposure(
                                node.declaration.id,
                                node.declaration.id.name
                            );
                        }

                        node.specifiers.forEach((specifier) => {
                            const name = readExportName(specifier);
                            reportApiContractExposure(specifier, name);
                        });
                    },
                    ImportDeclaration(node) {
                        const importPath = node.source.value;
                        if (typeof importPath !== "string" || !isServiceImport(importPath)) {
                            return;
                        }

                        node.specifiers.forEach((specifier) => {
                            const name = readImportedName(specifier);
                            if (!isApiContractName(name) && !isBusinessDataName(name)) {
                                return;
                            }

                            context.report({
                                node: specifier,
                                message:
                                    "ADMIN_WEB_NAME_SERVICE_TYPE_EXPOSURE: service imports may expose only Query/Command input types; Record/Node must come from *-types.ts and Request/Response are private."
                            });
                        });
                    }
                };
            }
        },
        "service-namespace-import": {
            create(context) {
                const normalizedFilePath = context.physicalFilename.split(path.sep).join("/");

                const resolveImportPath = (importPath) => {
                    if (importPath.startsWith("@/")) {
                        return `/src/${importPath.slice(2)}`;
                    }
                    if (!importPath.startsWith(".")) {
                        return importPath;
                    }
                    return path
                        .resolve(path.dirname(context.physicalFilename), importPath)
                        .split(path.sep)
                        .join("/");
                };

                const isPageRuntimeFile = () => {
                    return (
                        normalizedFilePath.includes("/src/pages/") &&
                        !normalizedFilePath.endsWith("-service.ts") &&
                        !normalizedFilePath.endsWith("-types.ts")
                    );
                };

                const isPageServiceImport = (importPath) => {
                    const resolvedImportPath = resolveImportPath(importPath);
                    return /\/src\/pages\/[^/]+\/[^/]+\/[^/]+-service$/.test(resolvedImportPath);
                };

                return {
                    ImportDeclaration(node) {
                        if (!isPageRuntimeFile() || node.importKind === "type") {
                            return;
                        }

                        const importPath = node.source.value;
                        if (typeof importPath !== "string" || !isPageServiceImport(importPath)) {
                            return;
                        }

                        const hasOnlyNamespaceRuntimeImport = node.specifiers.every(
                            (specifier) =>
                                specifier.type === "ImportNamespaceSpecifier" ||
                                specifier.importKind === "type"
                        );
                        if (hasOnlyNamespaceRuntimeImport) {
                            return;
                        }

                        context.report({
                            node,
                            message:
                                "ADMIN_WEB_NAME_SERVICE_NAMESPACE_IMPORT: runtime service imports in pages/components must use namespace import; import type is allowed."
                        });
                    }
                };
            }
        },
        "service-helper-contract-types": {
            create(context) {
                const normalizedFilePath = context.physicalFilename.split(path.sep).join("/");
                const isServiceFile = normalizedFilePath.endsWith("-service.ts");
                const isApiContractName = (name) => /(?:Request|Response)$/.test(name);
                const isServiceInputName = (name) => /(?:Query|Command)$/.test(name);
                const isBusinessDataName = (name) => /(?:Record|Node)$/.test(name);
                const transparentTypeNames = [
                    "Array",
                    "ReadonlyArray",
                    "Page",
                    "Partial",
                    "Promise"
                ];
                const scalarTypeNames = [
                    "boolean",
                    "string",
                    "number",
                    "void",
                    "unknown",
                    "never",
                    "Blob",
                    "File"
                ];

                const readTypeName = (typeNode) => {
                    if (!typeNode || typeNode.type !== "TSTypeReference") {
                        return "";
                    }
                    if (typeNode.typeName.type === "Identifier") {
                        return typeNode.typeName.name;
                    }
                    return "";
                };

                const readTypeArguments = (typeNode) => {
                    return (
                        typeNode?.typeArguments?.params ?? typeNode?.typeParameters?.params ?? []
                    );
                };

                const checkNoApiContractType = (node, typeNode) => {
                    const name = readTypeName(typeNode);
                    if (isApiContractName(name)) {
                        context.report({
                            node,
                            message:
                                "ADMIN_WEB_NAME_SERVICE_HELPER_TYPE: service helper generic types must not expose XxxRequest/XxxResponse."
                        });
                    }

                    readTypeArguments(typeNode).forEach((param) => {
                        checkNoApiContractType(node, param);
                    });
                };

                const isInlineOrScalarType = (typeNode) => {
                    return [
                        "TSBooleanKeyword",
                        "TSStringKeyword",
                        "TSNumberKeyword",
                        "TSVoidKeyword",
                        "TSUnknownKeyword",
                        "TSNeverKeyword",
                        "TSTypeLiteral"
                    ].includes(typeNode.type);
                };

                const checkReturnTypeShape = (node, typeNode) => {
                    checkNoApiContractType(node, typeNode);

                    if (!typeNode || isInlineOrScalarType(typeNode)) {
                        return;
                    }

                    if (typeNode.type === "TSArrayType") {
                        checkReturnTypeShape(node, typeNode.elementType);
                        return;
                    }

                    const name = readTypeName(typeNode);
                    if (!name || scalarTypeNames.includes(name)) {
                        return;
                    }

                    if (transparentTypeNames.includes(name)) {
                        readTypeArguments(typeNode).forEach((param) => {
                            checkReturnTypeShape(node, param);
                        });
                        return;
                    }

                    if (name === "OptionsRecord" || isBusinessDataName(name)) {
                        return;
                    }

                    context.report({
                        node,
                        message:
                            "ADMIN_WEB_NAME_SERVICE_HELPER_TYPE: service return types must be plain value, XxxRecord, XxxNode, Page<XxxRecord/XxxNode>, or arrays."
                    });
                };

                const checkInputTypeShape = (node, typeNode) => {
                    checkNoApiContractType(node, typeNode);

                    if (!typeNode || isInlineOrScalarType(typeNode)) {
                        return;
                    }

                    if (typeNode.type === "TSArrayType") {
                        checkInputTypeShape(node, typeNode.elementType);
                        return;
                    }

                    const name = readTypeName(typeNode);
                    if (!name || scalarTypeNames.includes(name)) {
                        return;
                    }

                    if (name === "Record") {
                        return;
                    }

                    if (name === "Array" || name === "ReadonlyArray") {
                        readTypeArguments(typeNode).forEach((param) => {
                            checkInputTypeShape(node, param);
                        });
                        return;
                    }

                    if (isServiceInputName(name)) {
                        return;
                    }

                    context.report({
                        node,
                        message:
                            "ADMIN_WEB_NAME_SERVICE_HELPER_TYPE: service request object types must be XxxQuery/XxxCommand or inline payload types."
                    });
                };

                return {
                    CallExpression(node) {
                        if (
                            !isServiceFile ||
                            node.callee.type !== "Identifier" ||
                            (node.callee.name !== "postJson" && node.callee.name !== "postFormData")
                        ) {
                            return;
                        }

                        const typeArguments =
                            node.typeArguments?.params ?? node.typeParameters?.params ?? [];
                        const [returnType, requestType] = typeArguments;
                        if (returnType) {
                            checkReturnTypeShape(node, returnType);
                        }
                        if (requestType) {
                            checkInputTypeShape(node, requestType);
                        }
                    }
                };
            }
        }
    }
};

const frontendRestrictedSyntax = [
    {
        selector: "FunctionDeclaration",
        message:
            "ADMIN_WEB_NAME_FUNCTION_ARROW: use arrow functions by default for frontend methods."
    }
];

export default tseslint.config(
    {
        ignores: ["dist", "node_modules"]
    },
    js.configs.recommended,
    ...tseslint.configs.recommended,
    {
        files: ["**/*.{ts,tsx}"],
        languageOptions: {
            ecmaVersion: 2020,
            globals: {
                ...globals.browser,
                ...globals.vitest
            }
        },
        plugins: {
            boundaries,
            local: localRules,
            "react-hooks": reactHooks,
            "react-refresh": reactRefresh
        },
        settings: {
            "import/resolver": {
                typescript: {
                    project: "./tsconfig.json"
                }
            },
            "boundaries/include": ["src/**/*"],
            "boundaries/elements": [
                { type: "api", pattern: "src/api/*", mode: "full" },
                { type: "auth", pattern: "src/auth/*", mode: "full" },
                { type: "layout", pattern: "src/layouts/*", mode: "full" },
                { type: "shared-component", pattern: "src/components/*", mode: "full" },
                { type: "page", pattern: "src/pages/*/*/*-page.tsx", mode: "full" },
                { type: "page-component", pattern: "src/pages/*/*/components/*", mode: "full" },
                { type: "page-service", pattern: "src/pages/*/*/*-service.ts", mode: "full" },
                { type: "query", pattern: "src/query/*", mode: "full" },
                { type: "router", pattern: "src/router/*", mode: "full" },
                { type: "shared-service", pattern: "src/service/*", mode: "full" },
                { type: "types", pattern: "src/types/*", mode: "full" }
            ]
        },
        rules: {
            ...reactHooks.configs.recommended.rules,
            "boundaries/dependencies": [
                "error",
                {
                    default: "allow",
                    rules: [
                        {
                            from: { type: "shared-component" },
                            disallow: { to: { type: ["page", "page-component", "page-service"] } },
                            message:
                                "ADMIN_WEB_LAYER_SHARED_COMPONENT_NO_PAGE: shared components must not import pages."
                        },
                        {
                            from: { type: "api" },
                            disallow: {
                                to: {
                                    type: ["page", "page-component", "layout", "shared-component"]
                                }
                            },
                            message:
                                "ADMIN_WEB_LAYER_API_NO_PAGE: api code must not import pages, layouts, or components."
                        },
                        {
                            from: { type: "auth" },
                            disallow: {
                                to: { type: ["page", "page-component", "layout", "page-service"] }
                            },
                            message:
                                "ADMIN_WEB_LAYER_AUTH_NO_PAGE: auth code must not import pages, layouts, or page services."
                        }
                    ]
                }
            ],
            "local/post-helper-service-only": "error",
            "local/shared-service-types-only": "error",
            "local/component-index-export-only": "error",
            "local/page-no-parent-relative-import": "error",
            "local/page-component-no-external-page": "error",
            "local/page-no-external-service": "error",
            "local/shared-component-css-local": "error",
            "local/page-style-file": "error",
            "local/hook-file-path": "error",
            "local/props-camel-case-property": "error",
            "local/e2e-spec-file-path": "error",
            "local/kebab-case-file-name": "error",
            "local/page-component-single-export": "error",
            "local/page-class-name-prefix": "error",
            "local/api-contract-type-location": "error",
            "local/service-input-type-location": "error",
            "local/business-data-type-location": "error",
            "local/kuzhambu-component-name": "error",
            "local/no-nested-ternary": "error",
            "local/service-method-verb-prefix": "error",
            "local/service-method-input-shape": "error",
            "local/service-helper-contract-types": "error",
            "local/service-type-exposure": "error",
            "local/service-namespace-import": "error",
            "local/no-console-log": "error",
            "local/no-explicit-any": "error",
            "local/no-antd-space-direct-in-pages": "error",
            "local/no-antd-drawer-direct-in-pages": "error",
            "local/no-antd-modal-direct-in-pages": "error",
            "local/no-antd-alert-direct-in-pages": "error",
            "local/no-antd-button-direct-in-pages": "error",
            "@typescript-eslint/no-explicit-any": "off",
            "local/confirm-hook-only": "error",
            "local/table-action-column-shape": "error",
            "local/interactive-accessible-name": "error",
            "@typescript-eslint/naming-convention": [
                "error",
                {
                    selector: "variableLike",
                    format: ["camelCase", "PascalCase", "UPPER_CASE"],
                    leadingUnderscore: "allow"
                },
                {
                    selector: "method",
                    format: ["camelCase"],
                    leadingUnderscore: "allow"
                },
                {
                    selector: "typeLike",
                    format: ["PascalCase"]
                }
            ],
            "no-restricted-imports": [
                "error",
                {
                    patterns: [
                        {
                            regex: "^\\.\\./\\.\\./",
                            message:
                                "ADMIN_WEB_LAYER_NO_DEEP_RELATIVE_IMPORT: use @/ for imports crossing two or more directories."
                        }
                    ]
                }
            ],
            "no-restricted-syntax": ["error", ...frontendRestrictedSyntax],
            "react-refresh/only-export-components": ["warn", { allowConstantExport: true }]
        }
    },
    {
        files: ["src/**/*.{ts,tsx}"],
        ignores: ["src/api/http.ts"],
        rules: {
            "no-restricted-syntax": [
                "error",
                ...frontendRestrictedSyntax,
                {
                    selector: "CallExpression[callee.name='fetch']",
                    message:
                        "ADMIN_WEB_LAYER_FETCH_ONLY_HTTP: only src/api/http.ts may call fetch directly."
                }
            ]
        }
    },
    prettier
);
