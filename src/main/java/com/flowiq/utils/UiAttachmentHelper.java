package com.flowiq.utils;

import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public final class UiAttachmentHelper {

    private UiAttachmentHelper() {
    }

    public static void attachScreenshot(Page page, String testName) {
        if (page == null) {
            return;
        }
        try {
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
            Allure.addAttachment(
                    testName + " - screenshot",
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    "png"
            );
        } catch (Exception e) {
            log.warn("Failed to attach screenshot for test {}", testName, e);
        }
    }

    public static void attachTrace(Path tracePath) {
        if (tracePath == null || !Files.exists(tracePath)) {
            return;
        }
        try {
            Allure.addAttachment(
                    "Playwright trace",
                    "application/zip",
                    Files.newInputStream(tracePath),
                    "zip"
            );
        } catch (IOException e) {
            log.warn("Failed to attach Playwright trace {}", tracePath, e);
        }
    }

    public static void attachVideo(Path videoPath) {
        if (videoPath == null || !Files.exists(videoPath)) {
            return;
        }
        try {
            Allure.addAttachment(
                    "Playwright video",
                    "video/webm",
                    Files.newInputStream(videoPath),
                    "webm"
            );
        } catch (IOException e) {
            log.warn("Failed to attach Playwright video {}", videoPath, e);
        }
    }
}
