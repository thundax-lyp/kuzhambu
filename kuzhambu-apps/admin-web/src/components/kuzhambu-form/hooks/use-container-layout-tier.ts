import { useEffect, useRef, useState } from "react";
import { readKuzhambuFormLayoutTier, type KuzhambuFormLayoutTier } from "../kuzhambu-form-layout";

export const useContainerLayoutTier = () => {
    const containerRef = useRef<HTMLDivElement | null>(null);
    const [layoutTier, setLayoutTier] = useState<KuzhambuFormLayoutTier>("lg");

    useEffect(() => {
        const container = containerRef.current;
        if (!container) {
            return undefined;
        }

        const updateLayoutTier = () => {
            setLayoutTier(readKuzhambuFormLayoutTier(container.getBoundingClientRect().width));
        };
        updateLayoutTier();

        const observer = new ResizeObserver(updateLayoutTier);
        observer.observe(container);
        return () => observer.disconnect();
    }, []);

    return { containerRef, layoutTier };
};
