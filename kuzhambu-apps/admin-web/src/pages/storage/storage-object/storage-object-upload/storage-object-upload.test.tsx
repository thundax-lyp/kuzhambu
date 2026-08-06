import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { App } from "antd";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { StorageObjectUpload } from "./storage-object-upload";

describe("StorageObjectUpload", () => {
    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("aborts an active upload when the capability unmounts", async () => {
        const uploadRequest: { signal: AbortSignal | null } = { signal: null };
        vi.spyOn(globalThis, "fetch").mockImplementation((_input, init) => {
            uploadRequest.signal = init?.signal || null;
            return new Promise(() => undefined);
        });
        const queryClient = new QueryClient({
            defaultOptions: {
                mutations: { retry: false }
            }
        });
        const { unmount } = render(
            <QueryClientProvider client={queryClient}>
                <App>
                    <StorageObjectUpload canUpload onUploaded={() => Promise.resolve()} />
                </App>
            </QueryClientProvider>
        );

        fireEvent.change(screen.getByLabelText("选择上传文件"), {
            target: {
                files: [new File(["content"], "upload.txt", { type: "text/plain" })]
            }
        });

        await waitFor(() => expect(uploadRequest.signal).not.toBeNull());
        unmount();

        expect(uploadRequest.signal?.aborted).toBe(true);
    });
});
