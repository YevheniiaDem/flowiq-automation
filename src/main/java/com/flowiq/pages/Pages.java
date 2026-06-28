package com.flowiq.pages;

import com.microsoft.playwright.Page;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Pages {

  private final Page page;

  public LoginPage login() {
    return new LoginPage(page);
  }

  public DashboardPage dashboard() {
    return new DashboardPage(page);
  }

  public TransactionsPage transactions() {
    return new TransactionsPage(page);
  }

  public ImportsPage imports() {
    return new ImportsPage(page);
  }

  public ReportsPage reports() {
    return new ReportsPage(page);
  }

  public NotificationsPage notifications() {
    return new NotificationsPage(page);
  }

  public TasksPage tasks() {
    return new TasksPage(page);
  }

  public ForecastsPage forecasts() {
    return new ForecastsPage(page);
  }

  public BusinessGuidePage businessGuide() {
    return new BusinessGuidePage(page);
  }

  public AIAccountantPage aiAccountant() {
    return new AIAccountantPage(page);
  }

  public RegisterPage register() {
    return new RegisterPage(page);
  }

  public AnalyticsPage analytics() {
    return new AnalyticsPage(page);
  }

  public SettingsPage settings() {
    return new SettingsPage(page);
  }

  public OnboardingPage onboarding() {
    return new OnboardingPage(page);
  }
}
