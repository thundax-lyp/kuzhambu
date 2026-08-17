const createTestStorage = (): Storage => {
    const values = new Map<string, string>();
    return {
        get length() {
            return values.size;
        },
        clear: () => values.clear(),
        getItem: (key: string) => values.get(key) ?? null,
        key: (index: number) => Array.from(values.keys())[index] ?? null,
        removeItem: (key: string) => values.delete(key),
        setItem: (key: string, value: string) => values.set(key, value)
    };
};

if (typeof globalThis.localStorage === "undefined") {
    Object.defineProperty(globalThis, "localStorage", {
        configurable: true,
        value: createTestStorage()
    });
}

if (typeof window !== "undefined" && typeof window.localStorage === "undefined") {
    Object.defineProperty(window, "localStorage", {
        configurable: true,
        value: globalThis.localStorage
    });
}

const enableReactActEnvironment = (target: object) => {
    Object.defineProperty(target, "IS_REACT_ACT_ENVIRONMENT", {
        configurable: true,
        value: true,
        writable: true
    });
};

enableReactActEnvironment(globalThis);

if (typeof window !== "undefined") {
    enableReactActEnvironment(window);
}

if (typeof self !== "undefined") {
    enableReactActEnvironment(self);
}
