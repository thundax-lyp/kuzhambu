import pytest

from kuzhambu_workers.render.browser_pool import BrowserPool


@pytest.mark.asyncio
async def test_browser_pool_launches_configured_browsers() -> None:
    fake = FakePlaywrightManager()
    pool = BrowserPool(
        pool_size=2,
        max_pages=4,
        page_timeout_ms=1000,
        playwright_factory=lambda: fake,
    )

    await pool.start()
    await pool.stop()

    assert fake.chromium.launch_count == 2
    assert fake.stop_count == 1
    assert all(browser.close_count == 1 for browser in fake.chromium.browsers)


@pytest.mark.asyncio
async def test_browser_pool_renders_pdf_and_releases_context() -> None:
    fake = FakePlaywrightManager()
    pool = BrowserPool(
        pool_size=1,
        max_pages=1,
        page_timeout_ms=1000,
        playwright_factory=lambda: fake,
    )

    pdf = await pool.html_to_pdf("<html></html>")
    await pool.stop()

    browser = fake.chromium.browsers[0]
    context = browser.contexts[0]
    assert pdf == b"%PDF-fake"
    assert context.pages[0].default_timeout == 1000
    assert context.pages[0].html == "<html></html>"
    assert context.close_count == 1


@pytest.mark.asyncio
async def test_browser_pool_releases_context_on_exception() -> None:
    fake = FakePlaywrightManager()
    pool = BrowserPool(
        pool_size=1,
        max_pages=1,
        page_timeout_ms=1000,
        playwright_factory=lambda: fake,
    )

    with pytest.raises(RuntimeError):
        async with pool.page():
            raise RuntimeError("render failed")

    async with pool.page() as page:
        page.set_default_timeout(1000)

    await pool.stop()
    assert fake.chromium.browsers[0].contexts[0].close_count == 1
    assert fake.chromium.browsers[0].contexts[1].close_count == 1


def test_browser_pool_rejects_invalid_limits() -> None:
    with pytest.raises(ValueError):
        BrowserPool(pool_size=0, max_pages=1, page_timeout_ms=1000)
    with pytest.raises(ValueError):
        BrowserPool(pool_size=1, max_pages=0, page_timeout_ms=1000)
    with pytest.raises(ValueError):
        BrowserPool(pool_size=1, max_pages=1, page_timeout_ms=0)


class FakePlaywrightManager:
    def __init__(self) -> None:
        self.chromium = FakeChromium()
        self.stop_count = 0

    async def start(self) -> "FakePlaywrightManager":
        return self

    async def stop(self) -> None:
        self.stop_count += 1


class FakeChromium:
    def __init__(self) -> None:
        self.launch_count = 0
        self.browsers: list[FakeBrowser] = []

    async def launch(self, **kwargs) -> "FakeBrowser":
        self.launch_count += 1
        browser = FakeBrowser(kwargs)
        self.browsers.append(browser)
        return browser


class FakeBrowser:
    def __init__(self, launch_kwargs: dict) -> None:
        self.launch_kwargs = launch_kwargs
        self.contexts: list[FakeContext] = []
        self.close_count = 0

    async def new_context(self) -> "FakeContext":
        context = FakeContext()
        self.contexts.append(context)
        return context

    async def close(self) -> None:
        self.close_count += 1


class FakeContext:
    def __init__(self) -> None:
        self.pages: list[FakePage] = []
        self.close_count = 0

    async def new_page(self) -> "FakePage":
        page = FakePage()
        self.pages.append(page)
        return page

    async def close(self) -> None:
        self.close_count += 1


class FakePage:
    def __init__(self) -> None:
        self.default_timeout = 0
        self.html = ""

    def set_default_timeout(self, timeout_ms: int) -> None:
        self.default_timeout = timeout_ms

    async def set_content(self, html: str, **kwargs) -> None:
        self.html = html

    async def pdf(self, **kwargs) -> bytes:
        return b"%PDF-fake"
