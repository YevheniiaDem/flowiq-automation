package com.flowiq.constants;

public final class TestConstants {

    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String ACCEPT_JSON = "application/json";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    public static final String APP_LANGUAGE_HEADER = "X-App-Language";
    public static final String APP_CURRENCY_HEADER = "X-App-Currency";
    public static final String DEFAULT_LANGUAGE = "uk";
    public static final String DEFAULT_CURRENCY = "UAH";

    public static final String DEFAULT_LOCALE = "uk-UA";
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int TRANSACTION_LIST_PAGE_SIZE = 10;

    public static java.util.Map<String, Integer> pagination(int pageSize) {
        return java.util.Map.of("page", DEFAULT_PAGE, "size", pageSize);
    }

    private TestConstants() {
    }
}
