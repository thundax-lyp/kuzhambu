import console from "node:console";
import { readdirSync, statSync } from "node:fs";
import { join, relative, resolve, sep } from "node:path";
import process from "node:process";

const pagesRoot = resolve(process.argv[2] ?? "src/pages");
const SHARED_PAGE_DOMAIN_NAMES = new Set(["common"]);
const SINGULAR_DOMAIN_SUFFIX_EXEMPTIONS = new Set(["status"]);

const normalizePath = (path) => path.split(sep).join("/");
const isDirectory = (path) => statSync(path).isDirectory();
const readDirectories = (path) =>
    readdirSync(path)
        .map((name) => join(path, name))
        .filter(isDirectory);
const isMockOnlyDomain = (entries) => entries.length === 1 && entries[0] === "__mocks__";
const errors = [];

const validateComponentDirectory = (componentPath, domainKey, relativeComponentPath) => {
    const componentName = relativeComponentPath.split("/").at(-1);
    const componentEntries = readdirSync(componentPath);

    if (!componentEntries.includes(`${componentName}.tsx`)) {
        errors.push(
            `ADMIN_WEB_PATH_PAGE_COMPONENT_DIRECTORY: ${domainKey}/${relativeComponentPath} must contain its entry component ${componentName}.tsx.`
        );
    }
    if (!componentEntries.includes("index.ts")) {
        errors.push(
            `ADMIN_WEB_PATH_PAGE_COMPONENT_DIRECTORY: ${domainKey}/${relativeComponentPath} must contain index.ts.`
        );
    }

    for (const childPath of readDirectories(componentPath)) {
        const childName = relative(componentPath, childPath);
        if (childName === "hooks") {
            continue;
        }
        validateComponentDirectory(
            childPath,
            domainKey,
            normalizePath(join(relativeComponentPath, childName))
        );
    }
};

for (const modulePath of readDirectories(pagesRoot)) {
    for (const domainPath of readDirectories(modulePath)) {
        const moduleName = relative(pagesRoot, modulePath);
        const domainName = relative(modulePath, domainPath);
        const domainKey = normalizePath(join(moduleName, domainName));
        const domainEntries = readdirSync(domainPath);
        const pageFileName = `${domainName}-page.tsx`;
        const pageEntries = domainEntries.filter(
            (entry) => entry.endsWith("-page.tsx") && !entry.endsWith("-page.test.tsx")
        );
        const hasOnlyMockData = isMockOnlyDomain(domainEntries);

        if (SHARED_PAGE_DOMAIN_NAMES.has(domainName)) {
            continue;
        }

        if (!domainEntries.includes(pageFileName) && !hasOnlyMockData) {
            errors.push(
                `ADMIN_WEB_PATH_PAGE_SHAPE: ${domainKey} must contain its page entry ${pageFileName}; found ${pageEntries.join(", ")}.`
            );
        }

        if (domainName.endsWith("s") && !SINGULAR_DOMAIN_SUFFIX_EXEMPTIONS.has(domainName)) {
            errors.push(
                `ADMIN_WEB_PATH_PAGE_DOMAIN_SINGULAR: ${domainKey} must use a singular page-domain name.`
            );
        }

        if (domainEntries.includes("components")) {
            errors.push(
                `ADMIN_WEB_PATH_PAGE_NO_COMPONENTS_BUCKET: ${domainKey}/components is forbidden; place each private component in ${domainKey}/<component>/<component>.tsx.`
            );
        }

        for (const entry of domainEntries) {
            const entryPath = join(domainPath, entry);
            if (
                entry.endsWith(".tsx") &&
                entry !== pageFileName &&
                entry !== `${domainName}-page.test.tsx`
            ) {
                errors.push(
                    `ADMIN_WEB_PATH_PAGE_COMPONENT_DIRECTORY: ${domainKey}/${entry} must move to ${domainKey}/${entry.replace(/\.tsx$/, "")}/${entry}.`
                );
            }

            if (
                isDirectory(entryPath) &&
                entry !== "__mocks__" &&
                entry !== "hooks" &&
                entry !== "components"
            ) {
                validateComponentDirectory(entryPath, domainKey, entry);
            }
        }
    }
}

if (errors.length > 0) {
    console.error(errors.join("\n"));
    process.exitCode = 1;
}
