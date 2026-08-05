import fs from "node:fs";
import path from "node:path";
import process from "node:process";
import ts from "typescript";

const SOURCE_ROOT = path.resolve(process.argv[2] ?? "src");
const TABLE_USAGE_PATTERN = /<\s*(?:KuzhambuTable|KuzhambuListPage)\b/;

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

const readPropertyName = (name) => {
    if (!name) {
        return undefined;
    }

    if (ts.isIdentifier(name) || ts.isStringLiteral(name) || ts.isNumericLiteral(name)) {
        return name.text;
    }

    return undefined;
};

const readStringLiteral = (expression) => {
    if (ts.isAsExpression(expression) || ts.isSatisfiesExpression(expression)) {
        return readStringLiteral(expression.expression);
    }

    if (ts.isStringLiteral(expression) || ts.isNoSubstitutionTemplateLiteral(expression)) {
        return expression.text;
    }

    return undefined;
};

const readObjectProperties = (objectLiteral) => {
    const properties = new Map();

    objectLiteral.properties.forEach((property) => {
        if (
            ts.isPropertyAssignment(property) ||
            ts.isMethodDeclaration(property) ||
            ts.isShorthandPropertyAssignment(property)
        ) {
            const propertyName = readPropertyName(property.name);
            if (propertyName) {
                properties.set(propertyName, property);
            }
        }
    });

    return properties;
};

const readLineNumber = (sourceFile, node) =>
    sourceFile.getLineAndCharacterOfPosition(node.getStart(sourceFile)).line + 1;

const isDividerAction = (properties) => {
    const typeProperty = properties.get("type");
    return (
        typeProperty &&
        ts.isPropertyAssignment(typeProperty) &&
        readStringLiteral(typeProperty.initializer) === "divider"
    );
};

const isDestructiveAction = (properties) => {
    const typeProperty = properties.get("type");
    const textProperty = properties.get("text");
    const typeValue =
        typeProperty && ts.isPropertyAssignment(typeProperty)
            ? readStringLiteral(typeProperty.initializer)
            : undefined;
    const textValue =
        textProperty && ts.isPropertyAssignment(textProperty)
            ? readStringLiteral(textProperty.initializer)
            : undefined;

    return typeValue === "danger" || textValue === "删除";
};

const validateActionOptionsArray = (filePath, sourceFile, arrayLiteral) => {
    let previousConcreteActionIsDivider = false;

    arrayLiteral.elements.forEach((element) => {
        if (!ts.isObjectLiteralExpression(element)) {
            previousConcreteActionIsDivider = false;
            return;
        }

        const properties = readObjectProperties(element);

        if (properties.has("icon")) {
            violations.push(
                `${normalizePath(filePath)}:${readLineNumber(
                    sourceFile,
                    element
                )}: ADMIN_WEB_UI_TABLE_ACTION_COLUMN KuzhambuTable row actions must not configure icon.`
            );
        }

        if (isDividerAction(properties)) {
            previousConcreteActionIsDivider = true;
            return;
        }

        if (isDestructiveAction(properties) && !previousConcreteActionIsDivider) {
            violations.push(
                `${normalizePath(filePath)}:${readLineNumber(
                    sourceFile,
                    element
                )}: ADMIN_WEB_UI_TABLE_ACTION_COLUMN destructive row actions must be preceded by a divider.`
            );
        }

        previousConcreteActionIsDivider = false;
    });
};

const validateActionOptionsExpression = (filePath, sourceFile, expression) => {
    if (ts.isArrayLiteralExpression(expression)) {
        validateActionOptionsArray(filePath, sourceFile, expression);
        return;
    }

    if (!ts.isArrowFunction(expression) && !ts.isFunctionExpression(expression)) {
        return;
    }

    if (ts.isArrayLiteralExpression(expression.body)) {
        validateActionOptionsArray(filePath, sourceFile, expression.body);
        return;
    }

    if (ts.isBlock(expression.body)) {
        expression.body.statements.forEach((statement) => {
            if (ts.isReturnStatement(statement) && statement.expression) {
                validateActionOptionsExpression(filePath, sourceFile, statement.expression);
            }
        });
    }
};

const sourceFiles = listFiles(SOURCE_ROOT, (filePath) => /\.(?:ts|tsx)$/.test(filePath));
const violations = [];

sourceFiles.forEach((filePath) => {
    const content = fs.readFileSync(filePath, "utf8");
    if (!TABLE_USAGE_PATTERN.test(content)) {
        return;
    }

    const sourceFile = ts.createSourceFile(
        filePath,
        content,
        ts.ScriptTarget.Latest,
        true,
        filePath.endsWith(".tsx") ? ts.ScriptKind.TSX : ts.ScriptKind.TS
    );

    const visit = (node) => {
        if (ts.isObjectLiteralExpression(node)) {
            const properties = readObjectProperties(node);
            const keyProperty = properties.get("key");
            const keyValue =
                keyProperty && ts.isPropertyAssignment(keyProperty)
                    ? readStringLiteral(keyProperty.initializer)
                    : undefined;

            if (keyValue === "actions" && properties.has("render") && !properties.has("options")) {
                violations.push(
                    `${normalizePath(filePath)}:${readLineNumber(
                        sourceFile,
                        node
                    )}: ADMIN_WEB_UI_TABLE_ACTION_COLUMN KuzhambuTable actions column must use options instead of render.`
                );
            }

            const optionsProperty = properties.get("options");
            if (
                keyValue === "actions" &&
                optionsProperty &&
                ts.isPropertyAssignment(optionsProperty)
            ) {
                validateActionOptionsExpression(filePath, sourceFile, optionsProperty.initializer);
            }
        }

        ts.forEachChild(node, visit);
    };

    visit(sourceFile);
});

if (violations.length > 0) {
    process.stderr.write(violations.join("\n") + "\n");
    process.exit(1);
}
