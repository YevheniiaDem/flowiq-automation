package com.flowiq.utils;

import com.microsoft.playwright.Page;

/**
 * Controls frontend onboarding / activation localStorage flags for UI tests.
 */
public final class OnboardingUiHelper {

    private OnboardingUiHelper() {
    }

    public static void dismissOverlays(Page page) {
        page.evaluate("() => {"
                + "localStorage.setItem('onboarding_completed', 'true');"
                + "localStorage.setItem('onboarding_skipped', 'true');"
                + "localStorage.removeItem('onboarding_pending');"
                + "localStorage.setItem('onboarding_whats_new_version', '999');"
                + "localStorage.setItem('onboarding_checklist_dismissed', 'true');"
                + "localStorage.setItem('onboarding_demo_workspace', 'false');"
                + "sessionStorage.removeItem('onboarding_tour_step');"
                + "}");
    }

    public static void resetForOnboardingFlow(Page page) {
        page.evaluate("() => {"
                + "['onboarding_completed','onboarding_skipped','onboarding_pending',"
                + "'onboarding_checklist_dismissed','onboarding_demo_workspace']"
                + ".forEach(k => localStorage.removeItem(k));"
                + "localStorage.removeItem('onboarding_whats_new_version');"
                + "sessionStorage.removeItem('onboarding_tour_step');"
                + "Object.keys(localStorage).filter(k => k.startsWith('onboarding_checklist_'))"
                + ".forEach(k => localStorage.removeItem(k));"
                + "}");
    }

    public static void enableDemoWorkspace(Page page) {
        page.evaluate("() => localStorage.setItem('onboarding_demo_workspace', 'true')");
    }

    public static void showActivationChecklist(Page page) {
        page.evaluate("() => {"
                + "localStorage.removeItem('onboarding_checklist_dismissed');"
                + "localStorage.setItem('onboarding_checklist_product_tour', 'false');"
                + "}");
    }
}
