import console from "node:console";
import { readdirSync, statSync } from "node:fs";
import { join, relative, resolve, sep } from "node:path";
import process from "node:process";

const pagesRoot = resolve(process.argv[2] ?? "src/pages");

// These domains still contain a components bucket or root-level private component files.
const LEGACY_PAGE_COMPONENT_LAYOUT_EXEMPTIONS = new Set(["operations/dashboard", "system/user"]);

const normalizePath = (path) => path.split(sep).join("/");
const isDirectory = (path) => statSync(path).isDirectory();
const readDirectories = (path) =>
    readdirSync(path)
        .map((name) => join(path, name))
        .filter(isDirectory);
const errors = [];

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

        if (pageEntries.length === 0) {
            continue;
        }

        if (!domainEntries.includes(pageFileName)) {
            errors.push(
                `ADMIN_WEB_PATH_PAGE_SHAPE: ${domainKey} must contain its page entry ${pageFileName}; found ${pageEntries.join(", ")}.`
            );
        }

        if (domainName.endsWith("s")) {
            errors.push(
                `ADMIN_WEB_PATH_PAGE_DOMAIN_SINGULAR: ${domainKey} must use a singular page-domain name.`
            );
        }

        if (LEGACY_PAGE_COMPONENT_LAYOUT_EXEMPTIONS.has(domainKey)) {
            continue;
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
                entry !== "hooks" &&
                entry !== "components" &&
                !readdirSync(entryPath).includes(`${entry}.tsx`)
            ) {
                errors.push(
                    `ADMIN_WEB_PATH_PAGE_COMPONENT_DIRECTORY: ${domainKey}/${entry} must contain its entry component ${entry}.tsx.`
                );
            }
        }
    }
}

if (errors.length > 0) {
    console.error(errors.join("\n"));
    process.exitCode = 1;
}
