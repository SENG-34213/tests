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
        ("HelloController", "helloCourse"): "Public service-root endpoint",
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
    with OUT.open("w", encoding="utf-8-sig", newline="") as stream:
        writer = csv.DictWriter(stream, fieldnames=FIELDS)
        writer.writeheader()
        for row in public:
            # Quiz APIs live in assignment-service in the backend, but are a distinct
            # functional service/category in the user's 15-service Step 1 plan.
            service = "quiz" if re.search(r"quiz", row["controller"], re.I) else row["service"]
            evidence = row["test_evidence"]
            evidence_items = re.split(r";\s*(?=(?:TC-[A-Z0-9-]+|NO_CASE_ID):)", evidence)
            cases = re.findall(r"(?:TC-[A-Z0-9-]+|NO_CASE_ID):([^#;]+)#([^ ;]+)", evidence)
            classes = sorted({c for c, _ in cases})
            methods = sorted({m for _, m in cases})
            def linked_assertions(codes=None, ownership=False):
                linked = []
                for item in evidence_items:
                    identity = re.search(r"(?:TC-[A-Z0-9-]+|NO_CASE_ID):[^#;]+#[^ ;]+", item)
                    if not identity:
                        continue
                    if ownership and not re.search(r"owner|ownership|foreign|other user|different.*(owner|user)|belongs to another|cannot access another", item, re.I):
                        continue
                    assertions = [int(x) for x in re.findall(r"assertEquals\s*\(\s*(\d{3})", item)]
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
        stream.write("# Remaining endpoint test work\n\n")
        stream.write("This report is generated from `backend-route-test-matrix.csv` and linked assertion text. A linked status-code assertion is evidence of a response check, not proof that the full business contract, payload, persistence, or security semantics are correct. Do not treat `NOT EVIDENCED` as proof that no test exists; it means this route audit does not link one.\n\n")
        stream.write(f"Public/user-facing, admin-facing, teacher-facing and service-root rows audited: **{len(public)}** of {len(rows)} source route rows. Internal/operational routes excluded from this public endpoint matrix: **{len(rows)-len(public)}**; track them in `backend-internal-route-auth-matrix.csv`.\n\n")
        stream.write("## Route-by-route gap counts\n\n")
        for key in ("no_linked_case", "success", "unauthenticated", "wrong_role", "validation", "ownership", "role_contract_review"):
            stream.write(f"- {key.replace('_', ' ')}: **{report_counts[key]}** route rows\n")
        stream.write("\n## Required follow-up\n\n")
        stream.write("1. Review each `required_role` entry against the effective service security filter chain, method/class annotations, Gateway rules, and SRS. Entries explicitly asking for security-chain review are unresolved, not assumed public.\n")
        stream.write("2. For every `NOT EVIDENCED` cell in the CSV, inspect the referenced test code and either link the exact request plus assertion or add a test. A mere case name is insufficient.\n")
        stream.write("3. Add explicit ownership/isolation checks for resource routes (foreign user/course/assignment/enrollment/etc.); generic 403/404 checks are not ownership evidence.\n")
        stream.write("4. Confirm success payload/schema and state changes, validation field/path, and role matrix per SRS; the matrix intentionally does not infer these from status codes.\n")
        stream.write("5. Review every endpoint row and every service-root/Gateway mapping for effective ingress path correctness. WebSocket routes are tracked separately in `backend-websocket-route-test-matrix.csv`.\n\n")
        stream.write("## Scope and caveats\n\n")
        stream.write("This artifact is a traceable audit baseline, not a declaration that Step 1 is complete: every row retains source and test evidence, while unproven categories remain visibly open. The 48 internal/operational route mappings are listed separately because they are not public product endpoints; their authentication/role evidence remains in the internal auth matrix.\n")
    print(f"Wrote {OUT} ({len(public)} endpoint rows); {REPORT}")


if __name__ == "__main__":
    main()
