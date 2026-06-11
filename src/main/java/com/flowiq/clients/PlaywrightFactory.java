package com.flowiq.clients;

import com.flowiq.config.ConfigManager;
import com.flowiq.config.EnvironmentConfig;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.flowiq.utils.UiAttachmentHelper;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public final class PlaywrightFactory {

    private static final ThreadLocal<PlaywrightSession> SESSION = new ThreadLocal<>();

    private PlaywrightFactory() {
    }

    public static Page createPage() {
        PlaywrightSession session = SESSION.get();
        if (session == null) {
            session = startSession();
            SESSION.set(session);
        }
        return session.page();
    }

    public static BrowserContext context() {
        return SESSION.get().context();
    }

    public static void closeSession() {
        closeSession(false);
    }

    public static void closeSession(boolean failed) {
        PlaywrightSession session = SESSION.get();
        if (session != null) {
            session.close(failed);
            SESSION.remove();
        }
    }

    private static PlaywrightSession startSession() {
        EnvironmentConfig config = ConfigManager.getConfig();

        Playwright playwright = Playwright.create();
        String browserName = config.browser().toLowerCase();
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(config.headless())
                .setSlowMo(config.slowMo());

        if (usesInstalledChannel(browserName)) {
            launchOptions.setChannel(browserName);
        }

        Browser browser = selectBrowser(playwright, browserName).launch(launchOptions);

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setBaseURL(config.baseUrl())
                .setViewportSize(1920, 1080)
                .setIgnoreHTTPSErrors(true);

        BrowserContext context = browser.newContext(contextOptions);
        context.setDefaultTimeout(config.uiTimeout());
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true));

        Page page = context.newPage();
        log.info("Playwright session started: browser={}, headless={}", config.browser(), config.headless());

        return new PlaywrightSession(playwright, browser, context, page);
    }

    private static boolean usesInstalledChannel(String browserName) {
        return "chrome".equals(browserName) || "msedge".equals(browserName);
    }

    private static BrowserType selectBrowser(Playwright playwright, String browserName) {
        return switch (browserName) {
            case "firefox" -> playwright.firefox();
            case "webkit" -> playwright.webkit();
            default -> playwright.chromium();
        };
    }

    private record PlaywrightSession(Playwright playwright, Browser browser, BrowserContext context, Page page) {

        void close(boolean failed) {
            Path tracePath = null;
            try {
                if (context != null) {
                    if (failed) {
                        try {
                            Files.createDirectories(Path.of("target", "traces"));
                            tracePath = Path.of("target", "traces", "trace-" + System.currentTimeMillis() + ".zip");
                            context.tracing().stop(new Tracing.StopOptions().setPath(tracePath));
                        } catch (Exception e) {
                            log.warn("Failed to save Playwright trace", e);
                            context.tracing().stop();
                        }
                    } else {
                        context.tracing().stop();
                    }
                }
            } finally {
                try {
                    if (page != null) {
                        page.close();
                    }
                } finally {
                    try {
                        if (context != null) {
                            context.close();
                        }
                    } finally {
                        try {
                            if (browser != null) {
                                browser.close();
                            }
                        } finally {
                            if (playwright != null) {
                                playwright.close();
                            }
                        }
                    }
                }
            }
            if (tracePath != null) {
                UiAttachmentHelper.attachTrace(tracePath);
            }
            log.info("Playwright session closed");
        }
    }
}
