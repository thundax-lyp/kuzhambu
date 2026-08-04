import fs from "node:fs";
import path from "node:path";
import process from "node:process";

const SOURCE_ROOT = path.resolve(process.argv[2] ?? "src");
const GLOBAL_PAGE_TYPE_FILE = path.join(SOURCE_ROOT, "types", "page.ts").split(path.sep).join("/");
const DECLARATION_PATTERN = /\bexport\s+(?:interface|type|class|enum)\s+([A-Za-z0-9_]+)/g;
const SERVICE_INPUT_TYPE_PATTERN = /(?:Command|Query)$/;
const API_CONTRACT_TYPE_PATTERN = /(?:Request|Response)$/;
const FORM_VALUES_TYPE_PATTERN = /FormValues$/;
const ID_NUMBER_TYPE_DECLARATION_PATTERN =
    /\b(id|[A-Za-z_$][\w$]*Id)\??\s*:\s*([^=;,)\n]*(?:\n\s*[^=;,)\n]+)*)/g;
const ID_NUMBER_HOOK_DECLARATION_PATTERN =
    /\b(?:const|let|var)\s+(?:\[\s*)?(id|[A-Za-z_$][\w$]*Id)\b[^=]*=\s*(?:useState|useRef)<([^>]+)>/g;
const NUMBER_TYPE_PATTERN = /\bnumber\b|\b(?:Array|ReadonlyArray)\s*<\s*number\s*>|\bnumber\s*\[\]/;

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

const normalizePath = (filePath) => filePath.split(path.sep).join("/");

const getLineNumber = (content, index) => content.slice(0, index).split(/\r?\n/).length;

const isAllowedFormValuesFile = (filePath, content) => {
    const normalizedFilePath = normalizePath(filePath);
    const fileName = path.basename(filePath);
    const formComponentFileName = fileName.replace(/-form-values\.ts$/, ".tsx");
    const isPageComponentFormValuesFile =
        normalizedFilePath.includes("/pages/") &&
        ((normalizedFilePath.endsWith(".tsx") &&
            /\b(?:KuzhambuForm|Form\.useForm)\b/.test(content)) ||
            (fileName.endsWith("-form-values.ts") &&
                fs.existsSync(path.join(path.dirname(filePath), formComponentFileName))));
    return (
        isPageComponentFormValuesFile ||
        (normalizedFilePath.includes("/components/") &&
            (normalizedFilePath.endsWith(".tsx") || normalizedFilePath.endsWith("-form-values.ts")))
    );
};

const sourceFiles = listFiles(SOURCE_ROOT, (filePath) => /\.(?:ts|tsx)$/.test(filePath));
const violations = [];

sourceFiles.forEach((filePath) => {
    const normalizedFilePath = normalizePath(filePath);
    const content = fs.readFileSync(filePath, "utf8");

    for (const match of content.matchAll(ID_NUMBER_TYPE_DECLARATION_PATTERN)) {
        const typeText = match[2] ?? "";
        if (NUMBER_TYPE_PATTERN.test(typeText)) {
            violations.push(
                `${normalizedFilePath}:${getLineNumber(content, match.index ?? 0)}: ADMIN_WEB_TYPE_ID_STRING ${match[1]} must use string instead of number.`
            );
        }
    }

    for (const match of content.matchAll(ID_NUMBER_HOOK_DECLARATION_PATTERN)) {
        const typeText = match[2] ?? "";
        if (NUMBER_TYPE_PATTERN.test(typeText)) {
            violations.push(
                `${normalizedFilePath}:${getLineNumber(content, match.index ?? 0)}: ADMIN_WEB_TYPE_ID_STRING ${match[1]} hook state/ref must use string instead of number.`
            );
        }
    }

    for (const match of content.matchAll(DECLARATION_PATTERN)) {
        const declarationName = match[1];

        if (
            normalizedFilePath.endsWith("-types.ts") &&
            (SERVICE_INPUT_TYPE_PATTERN.test(declarationName) ||
                API_CONTRACT_TYPE_PATTERN.test(declarationName) ||
                FORM_VALUES_TYPE_PATTERN.test(declarationName))
        ) {
            violations.push(
                `${normalizedFilePath}: ADMIN_WEB_NAME_BUSINESS_DATA_TYPE_LOCATION ${declarationName} must not be defined in *-types.ts.`
            );
        }

        if (
            normalizedFilePath.endsWith("-service.ts") &&
            (API_CONTRACT_TYPE_PATTERN.test(declarationName) ||
                FORM_VALUES_TYPE_PATTERN.test(declarationName))
        ) {
            violations.push(
                `${normalizedFilePath}: ADMIN_WEB_NAME_API_CONTRACT_TYPE_EXPOSURE ${declarationName} must not be exported from service.`
            );
        }

        if (
            normalizedFilePath.includes("/src/types/") &&
            normalizedFilePath !== GLOBAL_PAGE_TYPE_FILE &&
            (SERVICE_INPUT_TYPE_PATTERN.test(declarationName) ||
                API_CONTRACT_TYPE_PATTERN.test(declarationName) ||
                FORM_VALUES_TYPE_PATTERN.test(declarationName))
        ) {
            violations.push(
                `${normalizedFilePath}: ADMIN_WEB_PATH_GLOBAL_TYPES ${declarationName} must not define page service input, API contract, or form values.`
            );
        }

        if (
            normalizedFilePath.includes("/src/types/") &&
            normalizedFilePath === GLOBAL_PAGE_TYPE_FILE &&
            SERVICE_INPUT_TYPE_PATTERN.test(declarationName) &&
            declarationName !== "PageQuery"
        ) {
            violations.push(
                `${normalizedFilePath}: ADMIN_WEB_NAME_SERVICE_INPUT_TYPE_LOCATION only PageQuery is allowed in src/types/page.ts.`
            );
        }

        if (
            FORM_VALUES_TYPE_PATTERN.test(declarationName) &&
            !isAllowedFormValuesFile(filePath, content)
        ) {
            violations.push(
                `${normalizedFilePath}: ADMIN_WEB_NAME_FORM_VALUES_LOCATION ${declarationName} must live with the form component.`
            );
        }
    }
});

if (violations.length > 0) {
    process.stderr.write(violations.join("\n") + "\n");
    process.exit(1);
}
