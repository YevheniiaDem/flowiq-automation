package com.flowiq.pages;

import com.flowiq.constants.TestIds;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class OnboardingPage {

    private final Page page;

    public OnboardingPage(Page page) {
        this.page = page;
    }

    public Locator welcomeModal() {
        return page.getByTestId(TestIds.ONBOARDING_WELCOME_MODAL);
    }

    public Locator startTourButton() {
        return page.getByTestId(TestIds.ONBOARDING_START_TOUR_BTN);
    }

    public Locator skipButton() {
        return page.getByTestId(TestIds.ONBOARDING_SKIP_BTN);
    }

    public Locator activationChecklist() {
        return page.getByTestId(TestIds.ACTIVATION_CHECKLIST);
    }

    public Locator whatsNewModal() {
        return page.getByTestId(TestIds.WHATS_NEW_MODAL);
    }

    public Locator demoWorkspaceBanner() {
        return page.getByTestId(TestIds.DEMO_WORKSPACE_BANNER);
    }

    public Locator driverPopover() {
        return page.locator(".driver-popover");
    }
}
