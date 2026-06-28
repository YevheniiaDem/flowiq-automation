package com.flowiq.pages.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.nio.file.Path;

/**
 * Reusable file input for CSV and document uploads.
 */
public class FileUploadComponent extends BaseComponent {

    private final Locator input;

    public FileUploadComponent(Page page, String testId, String cssFallback) {
        super(page);
        this.input = byTestIdOr(testId, cssFallback).first();
    }

    public Locator input() {
        return input;
    }

    public void upload(Path filePath) {
        input.setInputFiles(filePath);
    }
}
