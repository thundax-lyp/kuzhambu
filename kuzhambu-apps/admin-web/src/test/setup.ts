import "@testing-library/jest-dom/vitest";

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

Object.defineProperty(window, "ResizeObserver", {
    writable: true,
    value: ResizeObserverMock
});

Object.defineProperty(globalThis, "ResizeObserver", {
    writable: true,
    value: ResizeObserverMock
});
