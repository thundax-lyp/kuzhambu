import "@testing-library/jest-dom/vitest";

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

const testStorage = createTestStorage();

Object.defineProperty(globalThis, "localStorage", {
    configurable: true,
    value: testStorage
});

if (typeof window !== "undefined") {
    Object.defineProperty(window, "localStorage", {
        configurable: true,
        value: testStorage
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

const nativeGetComputedStyle = window.getComputedStyle.bind(window);

Object.defineProperty(window, "getComputedStyle", {
    writable: true,
    value: (element: Element, pseudoElt?: string) =>
        nativeGetComputedStyle(element, pseudoElt ? undefined : pseudoElt)
});

Object.defineProperty(window, "matchMedia", {
    writable: true,
    value: vi.fn().mockImplementation((query: string) => ({
        matches: false,
        media: query,
        onchange: null,
        addListener: vi.fn(),
        removeListener: vi.fn(),
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
        dispatchEvent: vi.fn()
    }))
});

class ResizeObserverMock {
    observe = vi.fn();
    unobserve = vi.fn();
    disconnect = vi.fn();
}

class IntersectionObserverMock implements IntersectionObserver {
    readonly root = null;
    readonly rootMargin = "";
    readonly thresholds = [];

    disconnect = vi.fn();
    observe = vi.fn();
    takeRecords = vi.fn((): IntersectionObserverEntry[] => []);
    unobserve = vi.fn();
}

Object.defineProperty(window, "ResizeObserver", {
    writable: true,
    value: ResizeObserverMock
});

Object.defineProperty(globalThis, "ResizeObserver", {
    writable: true,
    value: ResizeObserverMock
});

Object.defineProperty(window, "IntersectionObserver", {
    writable: true,
    value: IntersectionObserverMock
});

Object.defineProperty(globalThis, "IntersectionObserver", {
    writable: true,
    value: IntersectionObserverMock
});
