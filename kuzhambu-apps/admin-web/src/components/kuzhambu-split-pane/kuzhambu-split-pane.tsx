import type { CSSProperties, KeyboardEvent, PointerEvent, ReactNode } from "react";
import { useCallback, useEffect, useRef, useState } from "react";
import "./kuzhambu-split-pane.css";

const DEFAULT_LEFT_WIDTH = 280;
const DEFAULT_MAX_LEFT_WIDTH = 520;
const DEFAULT_MIN_LEFT_WIDTH = 220;
const KEYBOARD_STEP = 24;

export interface KuzhambuSplitPaneProps {
    ariaLabel?: string;
    className?: string;
    defaultLeftWidth?: number;
    left: ReactNode;
    leftClassName?: string;
    maxLeftWidth?: number;
    minLeftWidth?: number;
    right: ReactNode;
    rightClassName?: string;
    storageKey?: string;
}

const clampWidth = (value: number, min: number, max: number) => {
    return Math.min(Math.max(value, min), max);
};

const readStoredWidth = (storageKey: string | undefined) => {
    if (!storageKey) {
        return null;
    }
    const storedValue = window.localStorage.getItem(storageKey);
    if (!storedValue) {
        return null;
    }
    const parsedValue = Number(storedValue);
    return Number.isFinite(parsedValue) ? parsedValue : null;
};

export const KuzhambuSplitPane = ({
    ariaLabel = "可调整左右分栏",
    className,
    defaultLeftWidth = DEFAULT_LEFT_WIDTH,
    left,
    leftClassName,
    maxLeftWidth = DEFAULT_MAX_LEFT_WIDTH,
    minLeftWidth = DEFAULT_MIN_LEFT_WIDTH,
    right,
    rightClassName,
    storageKey
}: KuzhambuSplitPaneProps) => {
    const containerRef = useRef<HTMLDivElement | null>(null);
    const [leftWidth, setLeftWidth] = useState(() =>
        clampWidth(
            typeof window === "undefined" ? defaultLeftWidth : (readStoredWidth(storageKey) ?? defaultLeftWidth),
            minLeftWidth,
            maxLeftWidth
        )
    );
    const [isDragging, setIsDragging] = useState(false);

    const updateLeftWidth = useCallback(
        (value: number) => {
            const nextWidth = clampWidth(value, minLeftWidth, maxLeftWidth);
            setLeftWidth(nextWidth);
            if (storageKey) {
                window.localStorage.setItem(storageKey, String(nextWidth));
            }
        },
        [maxLeftWidth, minLeftWidth, storageKey]
    );
    const actualLeftWidth = clampWidth(leftWidth, minLeftWidth, maxLeftWidth);

    useEffect(() => {
        if (!isDragging) {
            return undefined;
        }

        const handlePointerMove = (event: globalThis.PointerEvent) => {
            const rect = containerRef.current?.getBoundingClientRect();
            if (!rect) {
                return;
            }
            updateLeftWidth(event.clientX - rect.left);
        };

        const stopDragging = () => {
            setIsDragging(false);
        };

        window.addEventListener("pointermove", handlePointerMove);
        window.addEventListener("pointerup", stopDragging);
        window.addEventListener("pointercancel", stopDragging);

        return () => {
            window.removeEventListener("pointermove", handlePointerMove);
            window.removeEventListener("pointerup", stopDragging);
            window.removeEventListener("pointercancel", stopDragging);
        };
    }, [isDragging, updateLeftWidth]);

    const startDragging = (event: PointerEvent<HTMLDivElement>) => {
        event.preventDefault();
        setIsDragging(true);
    };

    const moveByKeyboard = (event: KeyboardEvent<HTMLDivElement>) => {
        if (event.key === "ArrowLeft") {
            event.preventDefault();
            updateLeftWidth(actualLeftWidth - KEYBOARD_STEP);
        }
        if (event.key === "ArrowRight") {
            event.preventDefault();
            updateLeftWidth(actualLeftWidth + KEYBOARD_STEP);
        }
        if (event.key === "Home") {
            event.preventDefault();
            updateLeftWidth(minLeftWidth);
        }
        if (event.key === "End") {
            event.preventDefault();
            updateLeftWidth(maxLeftWidth);
        }
    };

    return (
        <div
            ref={containerRef}
            className={[
                "kuzhambu-split-pane",
                isDragging ? "kuzhambu-split-pane-dragging" : "",
                className
            ]
                .filter(Boolean)
                .join(" ")}
            style={
                {
                    "--kuzhambu-split-pane-left-width": `${actualLeftWidth}px`
                } as CSSProperties
            }
        >
            <aside className={["kuzhambu-split-pane-left", leftClassName].filter(Boolean).join(" ")}>
                {left}
            </aside>
            <div
                className="kuzhambu-split-pane-resizer"
                role="separator"
                aria-label={ariaLabel}
                aria-orientation="vertical"
                aria-valuemax={maxLeftWidth}
                aria-valuemin={minLeftWidth}
                aria-valuenow={actualLeftWidth}
                tabIndex={0}
                onKeyDown={moveByKeyboard}
                onPointerDown={startDragging}
            />
            <div className={["kuzhambu-split-pane-right", rightClassName].filter(Boolean).join(" ")}>
                {right}
            </div>
        </div>
    );
};
