const THEME_STORAGE_KEY = "kuzhambu.admin.theme";
const THEME_CHANGE_EVENT = "kuzhambu.admin.theme.change";

export type AdminThemeName = "light" | "dark";

export const getStoredTheme = (): AdminThemeName => {
    return localStorage.getItem(THEME_STORAGE_KEY) === "dark" ? "dark" : "light";
};

export const setAdminTheme = (themeName: AdminThemeName) => {
    localStorage.setItem(THEME_STORAGE_KEY, themeName);
    window.dispatchEvent(new Event(THEME_CHANGE_EVENT));
};

export const subscribeAdminThemeChange = (listener: () => void) => {
    window.addEventListener(THEME_CHANGE_EVENT, listener);
    window.addEventListener("storage", listener);

    return () => {
        window.removeEventListener(THEME_CHANGE_EVENT, listener);
        window.removeEventListener("storage", listener);
    };
};
