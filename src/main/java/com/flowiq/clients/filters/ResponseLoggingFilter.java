package com.flowiq.clients.filters;

import io.qameta.allure.Allure;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResponseLoggingFilter implements Filter {

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {
        Response response = ctx.next(requestSpec, responseSpec);

        log.info("<<< {} {} — {} ({} ms)",
                requestSpec.getMethod(),
                requestSpec.getURI(),
                response.getStatusCode(),
                response.getTime());

        if (log.isDebugEnabled()) {
            log.debug("Response body: {}", response.getBody().asString());
        }

        Allure.addAttachment("Response", "text/plain",
                response.getStatusCode() + " (" + response.getTime() + " ms)\n\n"
                        + response.getBody().asString());

        return response;
    }
}
