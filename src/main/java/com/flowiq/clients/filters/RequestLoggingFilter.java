package com.flowiq.clients.filters;

import io.qameta.allure.Allure;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestLoggingFilter implements Filter {

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {
        String method = requestSpec.getMethod();
        String uri = requestSpec.getURI();

        log.info(">>> {} {}", method, uri);

        if (log.isDebugEnabled()) {
            log.debug("Request headers: {}", requestSpec.getHeaders());
            Object body = requestSpec.getBody();
            if (body != null) {
                log.debug("Request body: {}", safeBody(body));
            }
        }

        Allure.addAttachment("Request", "text/plain",
                method + " " + uri + "\n\n" + safeBody(requestSpec.getBody()));

        return ctx.next(requestSpec, responseSpec);
    }

    private String safeBody(Object body) {
        return body != null ? body.toString() : "";
    }
}
