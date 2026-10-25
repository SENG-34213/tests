# Testing status for the final report

Generated on 2026-10-25 from the TEST-042 testing deliverables branch. This records local evidence and the external runs still required; it is not a claim that staging or performance testing passed.

## Local verification

- Test repository: `mvn --batch-mode --no-transfer-progress clean verify` completed successfully; 103 JUnit methods ran. The evidence register contains 663 rows: 117 Passed, 546 Not Run, and zero unmapped. Repeated rows can map multiple requirement IDs to one method.
- Backend `feature/TEST-042-testing-coverage-gates`: `mvn --batch-mode --no-transfer-progress verify -pl services/assignment-service -am -Djacoco.skip=false` completed successfully; 95 tests, zero failures/errors/skips. The Maven 90% branch gate passed. `QuizGradingServiceImpl`: 35/35 lines and 14/14 branches. `GradingServiceImpl`: 68/68 lines and 38/38 branches.
- Backend GitHub Actions run [40180383416](https://github.com/SENG-34213/Mentify-backend/actions/runs/40180383416) completed successfully on SHA `8cb633c2c12868cb3239a8940e24cad1abd42746`. All 14 unit/coverage matrix jobs passed; the run published `jacoco-unit-assignment-service-501`.
- Test repository GitHub Actions run [40180387881](https://github.com/SENG-34213/tests/actions/runs/40180387881) completed successfully on SHA `969ae828e057878cfdd75295bb82f8267b0ed9d8` and uploaded `final-testing-pdfs-142`.
- Frontend: 5 Student/Admin Playwright E2E tests passed. The AI chat spec was excluded because the team reported manual verification. The tested E2E files match the current `development` tree; no frontend code change was required.
- `git diff --check` and the PDF generator unit tests pass.

## Historical/backend-wide evidence

Backend Actions run `40172276922`, SHA `40deaa168180365c3f9370aefcffe92471c58c26`, reported 12 unit modules at 53.28% line, 33.73% branch, and 50.55% instruction coverage. These aggregate values are below the guideline's 80% line/branch target and do not represent changed-code coverage. The current TEST-042 grading scope is backed by the successful run above; the older aggregate still does not imply the full backend meets 80%.

The endpoint matrix contains 235 effective HTTP route rows with linked source test requests/assertions. This is implementation traceability, not proof that every route passed in a live environment. Historical live evidence had 17 methods pass and 23 mapped case rows pass, while 640 case rows remained not run; its test SHA and Actions run ID were not captured.

## Runs still required

- Dispatch the backend integration workflow and retain its JaCoCo XML, JUnit reports, run ID, and deployed commit SHA. Check every effective endpoint against passing HTTP integration evidence.
- Configure and approve the protected `dedicated-performance` environment, use synthetic account secrets and an approved non-production URL, dispatch the bounded run at least twice, and include measured latency/throughput/error results.
- Configure and approve the protected `staging` environment, dispatch the read-only smoke workflow against the approved staging URLs, and include its run ID, deployment SHA, and sanitized artifact.
- Update the final report with those run references and results. Never describe a blocked or unrun workflow as a pass.

## Deliverables

- `test-register.pdf`
- `coverage-sprint8.pdf`
- `performance-report.pdf` (execution status only; marked NOT RUN until measurements exist)
- `testing-summary.pdf`
