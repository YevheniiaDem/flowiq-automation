package com.flowiq.services;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.constants.ApiEndpoints;
import com.flowiq.constants.TestConstants;
import com.flowiq.models.knowledge.KnowledgeArticleDetailDto;
import com.flowiq.models.knowledge.KnowledgeArticlePageDto;
import com.flowiq.models.knowledge.KnowledgeCategoryDto;
import com.flowiq.models.knowledge.KnowledgeDashboardSnapshotDto;
import com.flowiq.models.knowledge.KnowledgeSearchResponseDto;
import io.qameta.allure.Step;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusinessGuideService extends BaseApiService {

    @Step("List business guide articles")
    public KnowledgeArticlePageDto listArticles(Map<String, ?> queryParams) {
        return getOk(ApiEndpoints.BUSINESS_GUIDE_ARTICLES, queryParams, KnowledgeArticlePageDto.class);
    }

    @Step("List business guide articles (default pagination)")
    public KnowledgeArticlePageDto listArticles() {
        return listArticles(TestConstants.pagination(TestConstants.DEFAULT_PAGE_SIZE));
    }

    @Step("Get article by slug: {slug}")
    public KnowledgeArticleDetailDto getArticleBySlug(String slug) {
        return getOk(ApiEndpoints.withPathParam(ApiEndpoints.BUSINESS_GUIDE_ARTICLE_BY_SLUG, "slug", slug),
                KnowledgeArticleDetailDto.class);
    }

    @Step("List article categories")
    public List<KnowledgeCategoryDto> getCategories() {
        return getList(ApiEndpoints.BUSINESS_GUIDE_CATEGORIES, KnowledgeCategoryDto.class);
    }

    @Step("Search business guide articles: {query}")
    public KnowledgeSearchResponseDto search(String query, Map<String, ?> additionalParams) {
        Map<String, Object> params = new HashMap<>(additionalParams);
        params.put("q", query);
        return getOk(ApiEndpoints.BUSINESS_GUIDE_SEARCH, params, KnowledgeSearchResponseDto.class);
    }

    @Step("Get business guide dashboard snapshot")
    public KnowledgeDashboardSnapshotDto getDashboardSnapshot() {
        return getOk(ApiEndpoints.BUSINESS_GUIDE_DASHBOARD_SNAPSHOT, KnowledgeDashboardSnapshotDto.class);
    }

    @Step("Fetch articles (unchecked)")
    public ApiCallResult<KnowledgeArticlePageDto> fetchArticles(Map<String, ?> queryParams) {
        return fetch(ApiEndpoints.BUSINESS_GUIDE_ARTICLES, queryParams, KnowledgeArticlePageDto.class);
    }

    @Step("Fetch articles (unchecked, default pagination)")
    public ApiCallResult<KnowledgeArticlePageDto> fetchArticles() {
        return fetchArticles(TestConstants.pagination(TestConstants.DEFAULT_PAGE_SIZE));
    }

    @Step("Fetch articles without authentication")
    public ApiCallResult<KnowledgeArticlePageDto> fetchArticlesUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.BUSINESS_GUIDE_ARTICLES,
                TestConstants.pagination(TestConstants.DEFAULT_PAGE_SIZE),
                KnowledgeArticlePageDto.class);
    }

    @Step("Search articles without query")
    public ApiCallResult<KnowledgeSearchResponseDto> fetchSearchWithoutQuery() {
        return fetch(ApiEndpoints.BUSINESS_GUIDE_SEARCH, TestConstants.pagination(TestConstants.DEFAULT_PAGE_SIZE),
                KnowledgeSearchResponseDto.class);
    }

    @Step("Fetch article by slug {slug} (unchecked)")
    public ApiCallResult<KnowledgeArticleDetailDto> fetchArticleBySlug(String slug) {
        return attemptGet(ApiEndpoints.withPathParam(ApiEndpoints.BUSINESS_GUIDE_ARTICLE_BY_SLUG, "slug", slug),
                KnowledgeArticleDetailDto.class);
    }

    @Step("Fetch categories without authentication")
    public ApiCallResult<Void> fetchCategoriesUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.BUSINESS_GUIDE_CATEGORIES, Void.class);
    }
}
