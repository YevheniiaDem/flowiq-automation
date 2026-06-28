package com.flowiq.pages;

import com.flowiq.constants.TestIds;
import com.flowiq.constants.UiLocators;
import com.flowiq.constants.UiPaths;
import com.flowiq.pages.base.AbstractPage;
import com.flowiq.pages.components.ModalComponent;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class OnboardingPage extends AbstractPage {

    private final ModalComponent welcomeModal;
    private final ModalComponent whatsNewModal;

    public OnboardingPage(Page page) {
        super(page);
        this.welcomeModal = new ModalComponent(page, TestIds.ONBOARDING_WELCOME_MODAL);
        this.whatsNewModal = new ModalComponent(page, TestIds.WHATS_NEW_MODAL);
    }

    public Locator welcomeModal() {
        return welcomeModal.root();
    }

    public Locator startTourButton() {
        return byTestId(TestIds.ONBOARDING_START_TOUR_BTN);
    }

    public Locator skipButton() {
        return byTestId(TestIds.ONBOARDING_SKIP_BTN);
    }

    public Locator activationChecklist() {
        return byTestId(TestIds.ACTIVATION_CHECKLIST);
    }

    public Locator whatsNewModal() {
        return whatsNewModal.root();
    }

    public Locator demoWorkspaceBanner() {
        return byTestId(TestIds.DEMO_WORKSPACE_BANNER);
    }

    public Locator driverPopover() {
        return page.locator(UiLocators.ONBOARDING_DRIVER_POPOVER);
    }
}
