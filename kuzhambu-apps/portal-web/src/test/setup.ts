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
