import * as service from "./storage-object-service";

const createApiResponse = (data: unknown) =>
    Promise.resolve(
        new Response(
            JSON.stringify({
                code: "COMMON-00000",
                message: "success",
                data
            }),
            {
                headers: { "Content-Type": "application/json" },
                status: 200
            }
        )
    );

describe("storage-object-service", () => {
    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("aborts server multipart upload when cancelled after initiation", async () => {
        const abortController = new AbortController();
        const fetchCalls: Array<{ body?: BodyInit | null; url: string }> = [];
        vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
            const url = String(input);
            fetchCalls.push({ body: init?.body, url });

            if (url.endsWith("/storage/object/multipart/initiate")) {
                abortController.abort();
                return createApiResponse({
                    uploadId: "upload-1",
                    partSize: 5 * 1024 * 1024
                });
            }

            if (url.endsWith("/storage/object/multipart/abort")) {
                return createApiResponse({
                    uploadId: "upload-1",
                    uploadStatus: "ABORTED"
                });
            }

            return Promise.resolve(
                new Response(JSON.stringify({ code: "COMMON-00004", message: "not found" }), {
                    headers: { "Content-Type": "application/json" },
                    status: 404
                })
            );
        });

        const file = new File([new Uint8Array(21 * 1024 * 1024)], "large.bin", {
            type: "application/octet-stream"
        });

        await expect(
            service.uploadStorageFile({
                file,
                signal: abortController.signal
            })
        ).rejects.toMatchObject({
            code: "ABORTED",
            message: "Request was aborted"
        });

        const abortCall = fetchCalls.find((call) =>
            call.url.endsWith("/storage/object/multipart/abort")
        );
        expect(abortCall).toBeDefined();
        expect(JSON.parse(String(abortCall?.body))).toEqual({ uploadId: "upload-1" });
        expect(
            fetchCalls.some((call) => call.url.endsWith("/storage/object/multipart/complete"))
        ).toBe(false);
    });
});
