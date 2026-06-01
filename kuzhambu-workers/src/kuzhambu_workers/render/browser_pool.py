import asyncio
from collections.abc import AsyncIterator, Callable
from contextlib import asynccontextmanager
from typing import Any

from playwright.async_api import async_playwright


class BrowserPool:
    def __init__(
        self,
        *,
        pool_size: int,
        max_pages: int,
        page_timeout_ms: int,
        playwright_factory: Callable[[], Any] = async_playwright,
    ) -> None:
        if pool_size <= 0:
            raise ValueError("pool_size must be positive")
        if max_pages <= 0:
            raise ValueError("max_pages must be positive")
        if page_timeout_ms <= 0:
            raise ValueError("page_timeout_ms must be positive")
        self.pool_size = pool_size
        self.max_pages = max_pages
        self.page_timeout_ms = page_timeout_ms
        self._playwright_factory = playwright_factory
        self._playwright_manager: Any | None = None
        self._playwright: Any | None = None
        self._browsers: list[Any] = []
        self._browser_index = 0
        self._semaphore = asyncio.Semaphore(max_pages)
        self._lock = asyncio.Lock()

    async def start(self) -> None:
        async with self._lock:
            if self._browsers:
                return
            self._playwright_manager = self._playwright_factory()
            self._playwright = await self._playwright_manager.start()
            for _ in range(self.pool_size):
                browser = await self._playwright.chromium.launch(
                    headless=True,
                    args=["--disable-dev-shm-usage"],
                )
                self._browsers.append(browser)

    async def stop(self) -> None:
        async with self._lock:
            for browser in self._browsers:
                await browser.close()
            self._browsers.clear()
            if self._playwright_manager is not None:
                await self._playwright_manager.stop()
            self._playwright_manager = None
            self._playwright = None
            self._browser_index = 0

    @asynccontextmanager
    async def page(self) -> AsyncIterator[Any]:
        await self.start()
        await asyncio.wait_for(self._semaphore.acquire(), self.page_timeout_ms / 1000)
        context = None
        try:
            browser = self._next_browser()
            context = await browser.new_context()
            page = await context.new_page()
            page.set_default_timeout(self.page_timeout_ms)
            yield page
        finally:
            if context is not None:
                await context.close()
            self._semaphore.release()

    async def html_to_pdf(self, html: str) -> bytes:
        async with self.page() as page:
            await page.set_content(html, wait_until="load", timeout=self.page_timeout_ms)
            return await page.pdf(print_background=True)

    def _next_browser(self) -> Any:
        if not self._browsers:
            raise RuntimeError("browser pool is not started")
        browser = self._browsers[self._browser_index % len(self._browsers)]
        self._browser_index += 1
        return browser
