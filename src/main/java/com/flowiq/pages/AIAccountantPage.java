package com.flowiq.pages;

import com.flowiq.constants.TestIds;
import com.flowiq.constants.UiPaths;
import com.flowiq.pages.base.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class AIAccountantPage extends BasePage {

  public AIAccountantPage(Page page) {
    super(page);
  }

  @Override
  protected String getPath() {
    return UiPaths.AI_ACCOUNTANT;
  }

  @Override
  protected String getPageTestId() {
    return TestIds.AI_ACCOUNTANT_PAGE;
  }

  public Locator chatSection() {
    return byTestId(TestIds.AI_ACCOUNTANT_CHAT);
  }

  public Locator chatInput() {
    return byTestIdOr(TestIds.AI_ACCOUNTANT_CHAT_INPUT, "form input");
  }

  public Locator sendButton() {
    return byTestId(TestIds.AI_ACCOUNTANT_CHAT_SEND_BTN);
  }

  public Locator chatMessages() {
    return chatSection().locator(".flex.gap-2");
  }

  public Locator thinkingIndicator() {
    return chatSection().locator(".animate-spin");
  }

  public AIAccountantPage sendMessage(String message) {
    chatInput().fill(message);
    sendButton().click();
    return this;
  }

  public AIAccountantPage typeMessage(String message) {
    chatInput().fill(message);
    return this;
  }

  public void submitMessage() {
    byTestIdOr(TestIds.AI_ACCOUNTANT_CHAT_FORM, "form").press("Enter");
  }

  public int getMessageCount() {
    return chatMessages().count();
  }

  public void waitForResponse() {
    waitForHidden(thinkingIndicator());
  }
}
