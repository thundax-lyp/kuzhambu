import js from "@eslint/js";
import prettier from "eslint-config-prettier";
import boundaries from "eslint-plugin-boundaries";
import reactHooks from "eslint-plugin-react-hooks";
import reactRefresh from "eslint-plugin-react-refresh";
import globals from "globals";
import path from "node:path";
import tseslint from "typescript-eslint";

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
                                    "PORTAL_WEB_NAME_FILE_KEBAB_CASE: frontend source file names must use kebab-case."
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
                                    "PORTAL_WEB_LAYER_COMPONENT_INDEX_EXPORT_ONLY: component index.ts files may contain re-export declarations only."
                            });
                        });
                    }
                };
            }
        },
        "page-no-parent-relative-import": {
            create(context) {
                return {
                    ImportDeclaration(node) {
                        const normalizedFilePath = context.physicalFilename
                            .split(path.sep)
                            .join("/");
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
                                "PORTAL_WEB_LAYER_PAGE_NO_PARENT_RELATIVE_IMPORT: page files must use ./ for same page-domain imports and @/ for cross-domain or shared imports."
                        });
                    }
                };
            }
        },
        "page-no-external-service": {
            create(context) {
                const readPageDomainRoot = (normalizedFilePath) => {
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
                            !resolvedImportPath.endsWith("-service")
                        ) {
                            return;
                        }

                        if (normalizedPageDomainRoot !== pageDomainRoot) {
                            context.report({
                                node,
                                message:
                                    "PORTAL_WEB_LAYER_PAGE_NO_EXTERNAL_SERVICE: page domains must not import services from other page domains."
                            });
                        }
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
                    return /\/src\/pages\/[^/]+\/[^/]+-service$/.test(resolvedImportPath);
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
                                "PORTAL_WEB_NAME_SERVICE_NAMESPACE_IMPORT: runtime service imports in pages/components must use namespace import; import type is allowed."
                        });
                    }
                };
            }
        }
    }
};

const frontendRestrictedSyntax = [
    {
        selector: "ConditionalExpression > ConditionalExpression",
        message: "PORTAL_WEB_NAME_NO_NESTED_TERNARY: nested ternary expressions are forbidden."
    },
    {
        selector: "FunctionDeclaration",
        message:
            "PORTAL_WEB_NAME_FUNCTION_ARROW: use arrow functions by default for frontend methods."
    }
];

export default tseslint.config(
    {
        ignores: ["dist/**", "node_modules/**"]
    },
    js.configs.recommended,
    ...tseslint.configs.recommended,
    {
        files: ["src/**/*.{ts,tsx}"],
        languageOptions: {
            ecmaVersion: "latest",
            globals: {
                ...globals.browser,
                ...globals.es2024,
                ...globals.vitest
            },
            parserOptions: {
                ecmaFeatures: {
                    jsx: true
                }
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
                { type: "shared-component", pattern: "src/components/*", mode: "full" },
                { type: "page", pattern: "src/pages/*/*-page.tsx", mode: "full" },
                { type: "page-service", pattern: "src/pages/*/*-service.ts", mode: "full" },
                { type: "page-types", pattern: "src/pages/*/*-types.ts", mode: "full" },
                { type: "lib", pattern: "src/lib/*", mode: "full" }
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
                            disallow: { to: { type: ["page", "page-service", "page-types"] } },
                            message:
                                "PORTAL_WEB_LAYER_SHARED_COMPONENT_NO_PAGE: shared components must not import pages."
                        },
                        {
                            from: { type: "api" },
                            disallow: {
                                to: {
                                    type: ["page", "page-service", "page-types", "shared-component"]
                                }
                            },
                            message:
                                "PORTAL_WEB_LAYER_API_NO_PAGE: api code must not import pages or components."
                        }
                    ]
                }
            ],
            "local/component-index-export-only": "error",
            "local/kebab-case-file-name": "error",
            "local/page-no-external-service": "error",
            "local/page-no-parent-relative-import": "error",
            "local/service-namespace-import": "error",
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
            "no-console": ["error", { allow: ["warn", "error"] }],
            "no-restricted-imports": [
                "error",
                {
                    patterns: [
                        {
                            regex: "^\\.\\./\\.\\./",
                            message:
                                "PORTAL_WEB_LAYER_NO_DEEP_RELATIVE_IMPORT: use @/ for imports crossing two or more directories."
                        }
                    ]
                }
            ],
            "no-restricted-syntax": ["error", ...frontendRestrictedSyntax],
            "@typescript-eslint/no-explicit-any": "error",
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
                        "PORTAL_WEB_LAYER_FETCH_ONLY_HTTP: only src/api/http.ts may call fetch directly."
                }
            ]
        }
    },
    {
        files: ["src/components/ui/**/*.{ts,tsx}"],
        rules: {
            "no-restricted-syntax": [
                "error",
                {
                    selector: "ConditionalExpression > ConditionalExpression",
                    message:
                        "PORTAL_WEB_NAME_NO_NESTED_TERNARY: nested ternary expressions are forbidden."
                }
            ]
        }
    },
    prettier
);
