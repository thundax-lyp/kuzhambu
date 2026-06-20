import { afterEach, describe, expect, it } from "vitest";
import { clearAccessToken, saveTokenSession } from "./token-storage";
import { toAuthenticatedResourceUrl } from "./resource-url";

describe("resource url", () => {
    afterEach(() => {
        clearAccessToken();
    });

    it("adds current access token to relative resource url", () => {
        saveTokenSession({ token: "token-1" });

        expect(
            toAuthenticatedResourceUrl("/kuzhambu-admin-api/api/storage/object/1001/content")
        ).toBe("/kuzhambu-admin-api/api/storage/object/1001/content?token=token-1");
    });

    it("uses the latest access token without mutating backend resource url", () => {
        saveTokenSession({ token: "token-1" });
        saveTokenSession({ token: "token-2" });

        expect(toAuthenticatedResourceUrl("/kuzhambu-admin-api/api/sys/user/avatar?id=1001")).toBe(
            "/kuzhambu-admin-api/api/sys/user/avatar?id=1001&token=token-2"
        );
    });
});
