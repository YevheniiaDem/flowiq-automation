package com.flowiq.agents.rootcause.analyzer;

import com.flowiq.agents.rootcause.model.RootCauseCategory;

import java.util.regex.Pattern;

public class DatabaseFailureAnalyzer extends AbstractPatternRootCauseAnalyzer {

    public DatabaseFailureAnalyzer() {
        super(RootCauseCategory.DATA,
                Pattern.compile("(?i)(SQLException|constraint violation|duplicate key|deadlock"
                        + "|could not execute statement|DataIntegrityViolationException"
                        + "|PSQLException|ORA-\\d+|connection pool exhausted|FlywayException"
                        + "|migration failed|foreign key constraint)",
                        Pattern.DOTALL),
                "Database constraint, migration, or persistence layer failure affecting test data.",
                80);
    }

    @Override
    protected int adjustConfidence(int confidence, FailureAnalysisContext context) {
        if (!context.backendLogExcerpt().isEmpty()) {
            return confidence + 8;
        }
        return confidence;
    }
}
