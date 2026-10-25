#!/usr/bin/env python3
"""Build a conservative, evidence-linked public endpoint test matrix."""

import csv
import re
from collections import Counter
from pathlib import Path


TEST_ROOT = Path(__file__).resolve().parents[1]
BACKEND_ROOT = Path(r"E:\ResumeProjects\Mentify-backend")
SOURCE = TEST_ROOT / "docs/backend-route-test-matrix.csv"
OUT = TEST_ROOT / "docs/endpoint-test-matrix.csv"
REPORT = TEST_ROOT / "docs/missing-test-work.md"

FIELDS = [
    "service", "http_method", "endpoint", "required_role", "success_test",
    "unauthenticated_test", "wrong_role_test", "validation_test", "ownership_test",
    "existing_test_class", "existing_test_method", "missing_work", "coverage_status",
    "gateway_ingress", "source_evidence",
]
CASE_ID_PATTERN = r"(?:TC-[A-Z0-9-]+|TEST-[A-Z0-9-]+|NO_CASE_ID)"


def role_for(row):
    source = BACKEND_ROOT / row["source_file"]
    if not source.exists():
        return "SOURCE NOT FOUND — verify authorization contract"
    text = source.read_text(encoding="utf-8", errors="replace")
    name = re.escape(row["java_method"])
    declarations = list(re.finditer(r"\b" + name + r"\s*\(", text))
    method_annotation = ""
    for match in declarations:
        # A method declaration, not a call inside another method.
        prefix = text[max(0, match.start() - 1200):match.start()]
        if re.search(r"\b(public|protected)\b[^;{}]*$", prefix, re.S):
            annotations = re.findall(r"@(PreAuthorize|RolesAllowed|Secured)\s*\((\"[^\"]*\"|'[^']*')\)", prefix)
            if annotations:
                method_annotation = "; ".join("@%s(%s)" % pair for pair in annotations[-2:])
            break
    class_head = text.split("{", 1)[0]
    class_annotations = re.findall(r"@(PreAuthorize|RolesAllowed|Secured)\s*\((\"[^\"]*\"|'[^']*')\)", class_head)
    if method_annotation:
        return method_annotation
    if class_annotations:
        return "; ".join("@%s(%s)" % pair for pair in class_annotations)
    # Controllers without a method annotation still have a contract established by
    # their service SecurityFilterChain and/or explicit domain-level access checks.
    key = (row["controller"], row["java_method"])
    reviewed_roles = {
        ("AiHealthController", "health"): "Public; SecurityFilterChain permits /api/ai/health",
        ("AiUserController", "getCurrentUserInfo"): "Authenticated user (any role); SecurityFilterChain default authenticated",
        ("QuizGenerationController", "generateQuiz"): "Public; SecurityFilterChain permits /api/ai/quizzes/generate",
        ("AiQuizGenerationController", "generateQuizDraft"): "Authenticated user; assignment-service default authenticated; verify course ownership in domain service",
        ("QuizQuestionController", "deleteQuestion"): "TEACHER, ADMIN, SUPER_ADMIN; controller class-level @PreAuthorize",
        ("AuthController", "login"): "Public; auth SecurityFilterChain permitAll",
        ("AuthController", "forgotPassword"): "Public; auth SecurityFilterChain permitAll",
        ("AuthController", "verifyResetOtp"): "Public; auth SecurityFilterChain permitAll",
        ("AuthController", "resetPassword"): "Public; auth SecurityFilterChain permitAll (reset token required)",
        ("AuthController", "refresh"): "Public; auth SecurityFilterChain permitAll (refresh token required)",
        ("AuthController", "logout"): "Public; auth SecurityFilterChain permitAll (refresh token required)",
        ("AuthController", "jwks"): "Public; auth SecurityFilterChain permitAll",
        ("CommunicationGroupController", "getGroupById"): "Authenticated user; active group membership is enforced by service",
        ("HelloController", "helloCourse"): "Authenticated user (default SecurityFilterChain requires authentication)",
        ("MarketplaceProductController", "listPublic"): "Public marketplace read",
        ("MarketplaceProductController", "getPublic"): "Public marketplace read",
        ("NotificationController", "getNotifications"): "Authenticated user; JWT subject scopes results to owner",
        ("NotificationController", "getUnreadCount"): "Authenticated user; JWT subject scopes results to owner",
        ("NotificationController", "markRead"): "Authenticated user; JWT subject scopes mutation to owner",
        ("NotificationController", "markAllRead"): "Authenticated user; JWT subject scopes mutation to owner",
        ("NotificationController", "delete"): "Authenticated user; JWT subject scopes deletion to owner",
        ("AuthTestController", "publicTest"): "Public test probe; explicitly configured permitAll",
        ("AuthTestController", "protectedTest"): "Authenticated user (any role); protected probe/default authenticated",
        ("StudentController", "registerStudent"): "Public registration endpoint",
    }
    if key in reviewed_roles:
        return reviewed_roles[key]
    return "No method/class role annotation found; confirm effective SecurityFilterChain and ownership policy"


def main():
    rows = list(csv.DictReader(SOURCE.open(encoding="utf-8-sig", newline="")))
    public = [r for r in rows if r["exposure"] in {
        "public-or-user-facing", "admin-facing; Gateway analytics-service route",
        "teacher-facing; Gateway teacher route", "service root; Gateway discovery-locator route /course-service/**",
    }]
    report_counts = Counter()
    success_gap_routes = []
    with OUT.open("w", encoding="utf-8-sig", newline="") as stream:
        writer = csv.DictWriter(stream, fieldnames=FIELDS)
        writer.writeheader()
        for row in public:
            # Quiz APIs live in assignment-service in the backend, but are a distinct
            # functional service/category in the user's 15-service Step 1 plan.
            service = "quiz" if re.search(r"quiz", row["controller"], re.I) else row["service"]
            evidence = row["test_evidence"]
            evidence_items = re.split(r";\s*(?=" + CASE_ID_PATTERN + r":)", evidence)
            cases = re.findall(CASE_ID_PATTERN + r":([^#;]+)#([^ ;]+)", evidence)
            classes = sorted({c for c, _ in cases})
            methods = sorted({m for _, m in cases})
            def linked_assertions(codes=None, ownership=False):
                linked = []
                for item in evidence_items:
                    identity = re.search(CASE_ID_PATTERN + r":[^#;]+#[^ ;]+", item)
                    if not identity:
                        continue
                    if ownership and not re.search(r"owner|ownership|foreign|other user|different.*(owner|user)|belongs to another|cannot access another", item, re.I):
                        continue
                    assertions = [int(x) for x in re.findall(r"assertEquals\s*\(\s*(\d{3})", item)]
                    assertions.extend(int(x) for x in re.findall(
                        r"assertSuccessful\s*\([^,]+,\s*(\d{3})", item))
                    denied = [int(x) for group in re.findall(r"assertDenied\s*\((\d{3}(?:/\d{3})*)", item) for x in group.split("/")]
                    statuses = assertions + denied
                    if codes is None or any(code in codes for code in statuses):
                        linked.append(identity.group(0))
                return sorted(set(linked))
            # Classify by the specific linked status assertion. Ownership is only linked when
            # the case itself describes an explicit cross-owner/foreign-resource boundary.
            checks = {
                "success_test": linked_assertions({200, 201, 202, 204}),
                "unauthenticated_test": linked_assertions({401}),
                "wrong_role_test": linked_assertions({403}),
                "validation_test": linked_assertions({400, 422}),
                "ownership_test": linked_assertions(ownership=True),
            }
            if not checks["success_test"]:
                success_gap_routes.append((service, row["http_method"], row["effective_path"]))
            missing = [name.replace("_test", "") for name, linked in checks.items() if not linked]
            for item in missing:
                report_counts[item] += 1
            if not cases:
                report_counts["no_linked_case"] += 1
            role = role_for(row)
            if role.startswith("No method/class"):
                report_counts["role_contract_review"] += 1
            writer.writerow({
                "service": service, "http_method": row["http_method"],
                "endpoint": row["effective_path"], "required_role": role,
                **{key: ("Linked response assertion: " + "; ".join(value) if value else "NOT EVIDENCED — add/verify") for key, value in checks.items()},
                "existing_test_class": "; ".join(classes) if classes else "None linked",
                "existing_test_method": "; ".join(methods) if methods else "None linked",
                "missing_work": "; ".join(missing) if missing else "No category gap identified by linked response assertions; verify semantic assertions",
                "coverage_status": row["coverage_status"],
                "gateway_ingress": row["explicit_gateway_ingress_paths"],
                "source_evidence": row["source_file"] + " :: " + row["controller"] + "#" + row["java_method"] + " | " + evidence,
            })
    with REPORT.open("w", encoding="utf-8", newline="") as stream:
        stream.write("# Endpoint test evidence review\n\n")
        stream.write("This is a source-level link audit generated from `backend-route-test-matrix.csv`. A linked assertion proves a request and response check are present in test source; it does not prove the test passed against a live deployment or that payload/state semantics are fully asserted.\n\n")
        stream.write(f"The public/user-facing, admin-facing, teacher-facing, and service-root matrix has **{len(public)} rows** out of **{len(rows)} HTTP route rows**. The other **{len(rows)-len(public)} internal/operational rows** are tracked in `backend-internal-route-auth-matrix.csv`; WebSocket routes are tracked separately.\n\n")
        stream.write("Every public row has at least one linked JUnit request/case in this source audit: **%d/%d**. This is the project-guide endpoint-presence check, not live-run evidence.\n\n" % (len(public) - report_counts["no_linked_case"], len(public)))
        stream.write("## Assertion-category review flags\n\n")
        stream.write("A count below means the matrix does not link that assertion category to the route. Categories are not blanket requirements for every route: use the route's SRS/backend contract to decide whether a success, unauthenticated, wrong-role, validation, or ownership case applies. `endpoint-test-matrix.csv` retains the exact route row and available test evidence.\n\n")
        for key in ("success", "unauthenticated", "wrong_role", "validation", "ownership", "role_contract_review"):
            stream.write(f"- Missing linked {key.replace('_', ' ')} assertion: **{report_counts[key]}** route rows\n")
        stream.write("\n## Required follow-up\n\n")
        if success_gap_routes == [("quiz", "POST", "/api/assignments/quizzes/ai/generate")]:
            stream.write("1. AI quiz generation is the only public route without a linked automated positive assertion. The user reports manual verification and requested no AI tests; keep it manual-only. No public non-AI success-assertion gap remains in this source audit.\n")
        else:
            stream.write("1. Review rows with a missing linked success assertion against the SRS. Add a safe positive-path test where the endpoint contract defines success and the route currently has only negative evidence.\n")
        stream.write("2. Review authorization, validation, and ownership flags against the effective Gateway and service contracts. Add only the applicable missing scenarios; do not infer public access or ownership coverage from a generic status code.\n")
        stream.write("3. Keep internal routes in `backend-internal-route-auth-matrix.csv`; service-to-service identities and mutation effects need their specific contracts and approved fixtures.\n")
        stream.write("4. Execute the live suites against the approved non-production deployment and retain JUnit evidence before claiming runtime coverage.\n\n")
        stream.write("## Scope and status\n\n")
        stream.write("A zero category flag means the audit could not link a matching request/assertion string; it is not a product failure by itself. A nonzero flag is a review item, not proof that related behavior has no test. This matrix is static source evidence only and does not certify all business semantics, Gateway behavior, or deployed service health.\n")
    print(f"Wrote {OUT} ({len(public)} endpoint rows); {REPORT}")


if __name__ == "__main__":
    main()
