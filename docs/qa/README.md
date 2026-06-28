# FlowIQ QA Documentation Index

| Field | Value |
|-------|-------|
| **Version** | 1.0 |
| **Last updated** | 2026-06-28 |

Enterprise QA documentation for the **flowiq-automation** project, aligned with the implemented test framework, CI pipelines, and coverage audit.

---

## Document Register

| # | Document | ID | Description |
|---|----------|-----|-------------|
| 1 | [TEST_STRATEGY.md](TEST_STRATEGY.md) | QA-STR-001 | Overall quality strategy and principles |
| 2 | [TEST_PLAN.md](TEST_PLAN.md) | QA-PLN-001 | Activities, schedule, resources, deliverables |
| 3 | [TEST_POLICY.md](TEST_POLICY.md) | QA-POL-001 | Mandatory standards and prohibited practices |
| 4 | [TEST_SCOPE.md](TEST_SCOPE.md) | QA-SCP-001 | In/out of scope features and environments |
| 5 | [TEST_LEVELS.md](TEST_LEVELS.md) | QA-LVL-001 | Unit → E2E level definitions |
| 6 | [TEST_TYPES.md](TEST_TYPES.md) | QA-TYP-001 | Smoke, regression, contract, security, etc. |
| 7 | [ENTRY_EXIT_CRITERIA.md](ENTRY_EXIT_CRITERIA.md) | QA-EEC-001 | Start/stop testing and release gates |
| 8 | [RISK_ANALYSIS.md](RISK_ANALYSIS.md) | QA-RSK-001 | Quality risks and mitigations |
| 9 | [ENVIRONMENT_DESCRIPTION.md](ENVIRONMENT_DESCRIPTION.md) | QA-ENV-001 | local, ci, stage, dev configuration |
| 10 | [DEFECT_WORKFLOW.md](DEFECT_WORKFLOW.md) | QA-DEF-001 | Severity, lifecycle, verification |
| 11 | [REGRESSION_STRATEGY.md](REGRESSION_STRATEGY.md) | QA-REG-001 | API/UI regression approach |
| 12 | [SMOKE_STRATEGY.md](SMOKE_STRATEGY.md) | QA-SMK-001 | Smoke suite design and CI |
| 13 | [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) | QA-REL-001 | Pre-production verification |
| 14 | [ACCEPTANCE_CRITERIA.md](ACCEPTANCE_CRITERIA.md) | QA-ACC-001 | Feature-level acceptance criteria |
| 15 | [TEST_DATA_STRATEGY.md](TEST_DATA_STRATEGY.md) | QA-DAT-001 | Users, factories, isolation, cleanup |
| 16 | [AUTOMATION_STRATEGY.md](AUTOMATION_STRATEGY.md) | QA-AUT-001 | Framework patterns, suites, CI |
| 17 | [TRACEABILITY_MATRIX.md](TRACEABILITY_MATRIX.md) | QA-TRC-001 | Feature → test class mapping |
| 18 | [REQUIREMENTS_COVERAGE_MATRIX.md](REQUIREMENTS_COVERAGE_MATRIX.md) | QA-RCM-001 | Coverage % and gaps |

---

## Related Technical Documentation

| Document | Location |
|----------|----------|
| Automation coverage audit | [../automation/AUTOMATION_COVERAGE_REPORT.md](../automation/AUTOMATION_COVERAGE_REPORT.md) |
| CI/CD pipelines | [../CI-CD.md](../CI-CD.md) |
| API regression inventory | [../API-REGRESSION-COVERAGE.md](../API-REGRESSION-COVERAGE.md) |
| Contract inventory | [../CONTRACT-COVERAGE.md](../CONTRACT-COVERAGE.md) |
| CI infrastructure | [../automation/CI_INFRASTRUCTURE.md](../automation/CI_INFRASTRUCTURE.md) |
| Flaky test detection | [../automation/FLAKY_TEST_DETECTION.md](../automation/FLAKY_TEST_DETECTION.md) |
| UI smoke stability | [../UI-SMOKE-STABILITY.md](../UI-SMOKE-STABILITY.md) |

---

## Quick Commands

```bash
mvn test -Papi-smoke -Plocal
mvn test -Pui-smoke -Plocal
mvn test -Papi-regression -Plocal
mvn test -Pui-regression -Plocal
mvn test -Pcontract -Plocal
mvn test -Psecurity -Plocal
mvn test -Pe2e -Plocal
mvn verify -Prequirements-traceability
```

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-06-28 | QA Engineering | Initial enterprise QA documentation set |
