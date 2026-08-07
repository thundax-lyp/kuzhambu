import fs from "node:fs";
import path from "node:path";
import process from "node:process";

const SOURCE_ROOT = path.resolve("src");
const COMPONENT_ROOT = path.join(SOURCE_ROOT, "components");
const OPTIONS_TYPE_FILE = path.join(SOURCE_ROOT, "types", "options.ts").split(path.sep).join("/");
const CLASS_NAME_PATTERN = /\.((?:kuzhambu-[a-z0-9]+)(?:-[a-z0-9]+)*)/g;
const ANTD_SELECTOR_LINE_PATTERN = /\.ant-[A-Za-z0-9_-]+/;
const OPTION_RECORD_DECLARATION_PATTERN =
    /^\s*(?:export\s+)?(?:interface\s+[A-Za-z0-9_]*(?:OptionRecord|OptionsRecord)\b|type\s+[A-Za-z0-9_]*(?:OptionRecord|OptionsRecord)(?:<[^>]+>)?\s*=)/gm;
const FORBIDDEN_DIRECTORY_RULES = new Map([
    ["common", "ADMIN_WEB_FORBID_BOUNDARYLESS_DIR"],
    ["base", "ADMIN_WEB_FORBID_BOUNDARYLESS_DIR"],
    ["shared", "ADMIN_WEB_FORBID_BOUNDARYLESS_DIR"],
    ["store", "ADMIN_WEB_FORBID_EXTRA_SYSTEM"],
    ["routes", "ADMIN_WEB_FORBID_EXTRA_SYSTEM"],
    ["request", "ADMIN_WEB_FORBID_EXTRA_SYSTEM"],
    ["requests", "ADMIN_WEB_FORBID_EXTRA_SYSTEM"],
    ["permission", "ADMIN_WEB_FORBID_EXTRA_SYSTEM"],
    ["permissions", "ADMIN_WEB_FORBID_EXTRA_SYSTEM"],
    ["style", "ADMIN_WEB_FORBID_EXTRA_SYSTEM"],
    ["styles", "ADMIN_WEB_FORBID_EXTRA_SYSTEM"],
    ["controller", "ADMIN_WEB_FORBID_BACKEND_LAYER_DIR"],
    ["dao", "ADMIN_WEB_FORBID_BACKEND_LAYER_DIR"],
    ["mapper", "ADMIN_WEB_FORBID_BACKEND_LAYER_DIR"],
    ["repository", "ADMIN_WEB_FORBID_BACKEND_LAYER_DIR"],
    ["utils", "ADMIN_WEB_FORBID_BUCKET_DIR"],
    ["models", "ADMIN_WEB_FORBID_BUCKET_DIR"],
    ["stores", "ADMIN_WEB_FORBID_BUCKET_DIR"]
]);
const PAGE_CSS_ANTD_SELECTOR_ALLOWLIST = [
    { file: "src/pages/ai/prompt/prompt-page.css", allowedAntdSelectorLines: 1 },
    { file: "src/pages/auth/login/login-page.css", allowedAntdSelectorLines: 8 },
    {
        file: "src/pages/classics/common/classics-content-tag-panel.css",
        allowedAntdSelectorLines: 1
    },
    {
        file: "src/pages/classics/ming-custom/ming-customs-keyword-cloud/ming-customs-keyword-cloud.css",
        allowedAntdSelectorLines: 2
    },
    {
        file: "src/pages/classics/publication-job/publication-job-page.css",
        allowedAntdSelectorLines: 1
    },
    {
        file: "src/pages/classics/sancai/sancai-catalog-tree-panel/sancai-catalog-tree-panel.css",
        allowedAntdSelectorLines: 2
    },
    {
        file: "src/pages/classics/sancai/sancai-entry-panel/sancai-entry-edit-drawer/sancai-entry-basic-section/sancai-entry-image-field/sancai-entry-image-field.css",
        allowedAntdSelectorLines: 5
    },
    {
        file: "src/pages/classics/sancai/sancai-entry-panel/sancai-entry-edit-drawer/sancai-entry-basic-section/sancai-entry-translation-text-field/sancai-entry-translation-text-field.css",
        allowedAntdSelectorLines: 5
    },
    {
        file: "src/pages/classics/sancai/sancai-entry-panel/sancai-entry-panel.css",
        allowedAntdSelectorLines: 4
    },
    { file: "src/pages/classics/sancai/sancai-page.css", allowedAntdSelectorLines: 3 },
    {
        file: "src/pages/classics/sancai-visual/sancai-entry-visual-section/sancai-entry-visual-section.css",
        allowedAntdSelectorLines: 14
    },
    {
        file: "src/pages/classics/sancai-visual/sancai-visual-entry-context/sancai-visual-entry-context.css",
        allowedAntdSelectorLines: 1
    },
    {
        file: "src/pages/classics/sancai-visual/sancai-visual-entry-picker-modal/sancai-visual-entry-picker-modal.css",
        allowedAntdSelectorLines: 7
    },
    {
        file: "src/pages/classics/wangqi/wangqi-document-edit-drawer/wangqi-document-edit-drawer.css",
        allowedAntdSelectorLines: 1
    },
    { file: "src/pages/classics/wangqi/wangqi-page.css", allowedAntdSelectorLines: 10 },
    { file: "src/pages/dashboard/dashboard/dashboard-page.css", allowedAntdSelectorLines: 6 },
    {
        file: "src/pages/discovery/qa/qa-message-panel/qa-message-panel.css",
        allowedAntdSelectorLines: 23
    },
    {
        file: "src/pages/discovery/qa/qa-session-table/qa-session-table.css",
        allowedAntdSelectorLines: 2
    },
    { file: "src/pages/discovery/qa-console/qa-console-page.css", allowedAntdSelectorLines: 1 },
    {
        file: "src/pages/discovery/search-statistic/search-statistic-page.css",
        allowedAntdSelectorLines: 16
    },
    {
        file: "src/pages/knowledge/graph-extraction/graph-extraction-page.css",
        allowedAntdSelectorLines: 1
    },
    { file: "src/pages/knowledge/graph-result/graph-result-page.css", allowedAntdSelectorLines: 1 },
    { file: "src/pages/knowledge/lineage/lineage-page.css", allowedAntdSelectorLines: 2 },
    { file: "src/pages/knowledge/taxonomy/taxonomy-page.css", allowedAntdSelectorLines: 1 },
    {
        file: "src/pages/operations/backup-restore/backup-restore-page.css",
        allowedAntdSelectorLines: 1
    },
    {
        file: "src/pages/storage/storage-object/storage-object-page.css",
        allowedAntdSelectorLines: 16
    },
    { file: "src/pages/system/department/department-page.css", allowedAntdSelectorLines: 11 },
    { file: "src/pages/system/menu/menu-page.css", allowedAntdSelectorLines: 10 },
    {
        file: "src/pages/system/role/role-edit-drawer/menu-tree-field/menu-tree-field.css",
        allowedAntdSelectorLines: 2
    },
    {
        file: "src/pages/system/user/user-department-tree/user-department-tree.css",
        allowedAntdSelectorLines: 7
    },
    { file: "src/pages/system/user/user-page.css", allowedAntdSelectorLines: 5 }
];
const pageCssAntdSelectorAllowanceByFile = new Map(
    PAGE_CSS_ANTD_SELECTOR_ALLOWLIST.map((entry) => [entry.file, entry.allowedAntdSelectorLines])
);

const listFiles = (directory, predicate) => {
    if (!fs.existsSync(directory)) {
        return [];
    }

    return fs.readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
        const entryPath = path.join(directory, entry.name);
        if (entry.isDirectory()) {
            return listFiles(entryPath, predicate);
        }
        return predicate(entryPath) ? [entryPath] : [];
    });
};

const listDirectories = (directory) => {
    if (!fs.existsSync(directory)) {
        return [];
    }

    return fs.readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
        const entryPath = path.join(directory, entry.name);
        if (!entry.isDirectory()) {
            return [];
        }
        return [entryPath, ...listDirectories(entryPath)];
    });
};

const componentNames = fs
    .readdirSync(COMPONENT_ROOT, { withFileTypes: true })
    .filter((entry) => entry.isDirectory() && entry.name.startsWith("kuzhambu-"))
    .map((entry) => entry.name)
    .sort((left, right) => right.length - left.length);

const readComponentName = (className) => {
    return componentNames.find((componentName) => {
        return className === componentName || className.startsWith(`${componentName}-`);
    });
};

const pageStyleFiles = listFiles(SOURCE_ROOT, (filePath) =>
    /\/pages\/.*\/[^/]+-page\.css$/.test(filePath.split(path.sep).join("/"))
);

const pageStyleByDomain = new Map(
    pageStyleFiles.map((filePath) => {
        const normalizedFilePath = filePath.split(path.sep).join("/");
        const domainName = path.basename(filePath, ".css").replace(/-page$/, "");
        return [domainName, normalizedFilePath];
    })
);

const readPageDomainName = (className) => {
    return [...pageStyleByDomain.keys()]
        .sort((left, right) => right.length - left.length)
        .find((domainName) => className === domainName || className.startsWith(`${domainName}-`));
};

const cssFiles = listFiles(SOURCE_ROOT, (filePath) => filePath.endsWith(".css"));
const sourceFiles = listFiles(SOURCE_ROOT, (filePath) => /\.(?:ts|tsx|css)$/.test(filePath));
const violations = [];

const countAntdSelectorLines = (content) => {
    return content.split(/\r?\n/).filter((line) => ANTD_SELECTOR_LINE_PATTERN.test(line)).length;
};

const reportNewAntdSelectorLines = (filePath, content) => {
    const normalizedFilePath = filePath.split(path.sep).join("/");
    if (!/\/src\/pages\/.*\.css$/.test(normalizedFilePath)) {
        return;
    }

    const repoRelativePath = path.relative(SOURCE_ROOT, filePath).split(path.sep).join("/");
    const pageRelativePath = `src/${repoRelativePath}`;
    const allowedCount = pageCssAntdSelectorAllowanceByFile.get(pageRelativePath) ?? 0;
    const currentCount = countAntdSelectorLines(content);
    if (currentCount > allowedCount) {
        violations.push(
            `${normalizedFilePath}: ADMIN_WEB_STYLE_NO_ANTD_SELECTOR_IN_PAGES allows ${allowedCount} existing antd selector line(s), but found ${currentCount}. Move new overrides to src/components/** or global CSS.`
        );
    }
};

listDirectories(SOURCE_ROOT).forEach((directoryPath) => {
    const directoryName = path.basename(directoryPath);
    const rule = FORBIDDEN_DIRECTORY_RULES.get(directoryName);
    if (!rule) {
        return;
    }

    const normalizedDirectoryPath = directoryPath.split(path.sep).join("/");
    if (/\/src\/pages\/[^/]+\/common$/.test(normalizedDirectoryPath)) {
        return;
    }

    violations.push(
        `${rule}: ${normalizedDirectoryPath} uses forbidden directory "${directoryName}".`
    );
});

sourceFiles.forEach((filePath) => {
    const content = fs.readFileSync(filePath, "utf8");
    const normalizedFilePath = filePath.split(path.sep).join("/");

    if (/\.module\.css$/.test(normalizedFilePath)) {
        violations.push(`${normalizedFilePath}: ADMIN_WEB_FORBID_STYLE_SYSTEM forbids CSS module.`);
    }

    if (filePath.endsWith(".css") && /@tailwind\b/.test(content)) {
        violations.push(`${normalizedFilePath}: ADMIN_WEB_FORBID_STYLE_SYSTEM forbids Tailwind.`);
    }

    if (
        /\.(?:ts|tsx)$/.test(filePath) &&
        /from\s+["'](?:styled-components|tailwindcss|@tailwindcss\/[^"']+)["']/.test(content)
    ) {
        violations.push(
            `${normalizedFilePath}: ADMIN_WEB_FORBID_STYLE_SYSTEM forbids styled-components and Tailwind.`
        );
    }

    if (
        /\.(?:ts|tsx)$/.test(filePath) &&
        normalizedFilePath.includes("/src/pages/") &&
        normalizedFilePath !== OPTIONS_TYPE_FILE
    ) {
        for (const match of content.matchAll(OPTION_RECORD_DECLARATION_PATTERN)) {
            violations.push(
                `${normalizedFilePath}: ADMIN_WEB_NAME_OPTION_RECORD_LOCATION ${match[0]} must be defined in ${OPTIONS_TYPE_FILE}`
            );
        }
    }
});

cssFiles.forEach((filePath) => {
    const content = fs.readFileSync(filePath, "utf8");
    const normalizedFilePath = filePath.split(path.sep).join("/");

    reportNewAntdSelectorLines(filePath, content);

    for (const match of content.matchAll(CLASS_NAME_PATTERN)) {
        const className = match[1];
        const componentName = readComponentName(className);
        if (!componentName) {
            continue;
        }

        const expectedFilePath = path
            .join(COMPONENT_ROOT, componentName, `${componentName}.css`)
            .split(path.sep)
            .join("/");

        if (normalizedFilePath !== expectedFilePath) {
            violations.push(
                `${normalizedFilePath}: ADMIN_WEB_STYLE_COMPONENT_CLASS_LOCATION .${className} must live in ${expectedFilePath}`
            );
        }
    }

    for (const match of content.matchAll(CLASS_NAME_PATTERN)) {
        const className = match[1];
        const pageDomainName = readPageDomainName(className);
        if (!pageDomainName) {
            continue;
        }

        const expectedFilePath = pageStyleByDomain.get(pageDomainName);
        if (normalizedFilePath !== expectedFilePath) {
            violations.push(
                `${normalizedFilePath}: ADMIN_WEB_STYLE_PAGE_CLASS_LOCATION .${className} must live in ${expectedFilePath}`
            );
        }
    }
});

if (violations.length > 0) {
    process.stderr.write(violations.join("\n") + "\n");
    process.exit(1);
}
