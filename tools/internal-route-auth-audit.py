#!/usr/bin/env python3
"""Expand all internal HTTP route rows with source-reviewed auth models."""

from __future__ import annotations

import csv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MATRIX = ROOT / "docs/backend-route-test-matrix.csv"
OUTPUT = ROOT / "docs/backend-internal-route-auth-matrix.csv"


AUTH_MODELS = {
    "AssignmentAnalyticsController": (
        "Bearer JWT",
        "ADMIN/SUPER_ADMIN for overview; ADMIN/SUPER_ADMIN/TEACHER for course summary",
        "Add role-scoped analytics requests; course summary requires a configured course fixture.",
    ),
    "AttendanceAnalyticsController": (
        "Bearer JWT",
        "ADMIN/SUPER_ADMIN for overview; ADMIN/SUPER_ADMIN/TEACHER for course summary",
        "Add role-scoped analytics requests; course summary requires a configured course fixture.",
    ),
    "CalendarProjectionReconciliationController": (
        "Bearer JWT",
        "ADMIN/SUPER_ADMIN",
        "Use test-created assignment/quiz/session data or configured fixtures; verify reconciliation idempotency.",
    ),
    "QuizAnalyticsContractController": (
        "Bearer JWT",
        "ADMIN/SUPER_ADMIN for overview; ADMIN/SUPER_ADMIN/TEACHER for course summary",
        "Add role-scoped analytics requests; course summary requires a configured course fixture.",
    ),
    "QuizAnalyticsController": (
        "Bearer JWT",
        "ADMIN/SUPER_ADMIN for overview; TEACHER/ADMIN/SUPER_ADMIN for today and course queries",
        "Add role-scoped analytics requests; course summary requires a configured course fixture.",
    ),
    "AuthController": (
        "X-Internal-Token shared-secret header",
        "Backend-configured trusted service token; no end-user role substitutes for it",
        "Add negative missing/invalid-token tests now; positive provision/password/delete tests require a disposable identity fixture and approved secret source.",
    ),
    "CalendarAssignmentQuizProjectionController": (
        "Bearer JWT plus X-Internal-Service-Name and X-Internal-Service-Token",
        "ADMIN/SUPER_ADMIN/TEACHER JWT plus a backend-allowlisted service identity/token pair",
        "Add negative missing/invalid service-token tests; positive projection tests require approved service credentials and test-owned projection IDs.",
    ),
    "CalendarExamProjectionController": (
        "Bearer JWT plus X-Internal-Service-Name and X-Internal-Service-Token",
        "ADMIN/SUPER_ADMIN/TEACHER JWT plus a backend-allowlisted service identity/token pair",
        "Add negative missing/invalid service-token tests; positive projection tests require approved service credentials and test-owned exam IDs.",
    ),
    "InternalAnalyticsController": (
        "Bearer JWT",
        "ADMIN/SUPER_ADMIN",
        "Add administrator success and student/anonymous denial checks.",
    ),
    "InternalLessonController": (
        "Bearer JWT",
        "STUDENT/TEACHER/ADMIN/SUPER_ADMIN; resource ownership/enrollment also applies",
        "Use a configured lesson/course fixture and test ownership plus response projection.",
    ),
    "InternalMaterialScopeController": (
        "Bearer JWT",
        "STUDENT/TEACHER/ADMIN/SUPER_ADMIN; resource scope/ownership also applies",
        "Use configured course/module/lesson fixtures and test scope boundaries.",
    ),
    "EnrollmentAnalyticsController": (
        "Bearer JWT; no explicit controller role annotation found",
        "Authenticated caller; intended service/role authorization needs backend contract confirmation",
        "Confirm supported caller identity before adding positive coverage; never infer public access from the path alone.",
    ),
    "ExamAnalyticsController": (
        "Bearer JWT",
        "ADMIN/SUPER_ADMIN/TEACHER",
        "Add role-scoped analytics requests; course summary requires a configured course fixture.",
    ),
    "ExamCalendarReconciliationController": (
        "Bearer JWT",
        "ADMIN/SUPER_ADMIN",
        "Use a test-created exam and verify calendar reconciliation behavior/idempotency.",
    ),
    "InternalMaterialController": (
        "Bearer JWT",
        "STUDENT/TEACHER/ADMIN/SUPER_ADMIN; material ownership applies",
        "Use configured or test-created material fixtures; test association access and ownership.",
    ),
    "MaterialMaintenanceController": (
        "Bearer JWT",
        "ADMIN/SUPER_ADMIN; maintenance authorization is explicitly restricted",
        "Maintenance audit and cleanup have admin-success and non-admin-denial coverage; cleanup is dry-run/controlled.",
    ),
    "PaymentController": (
        "Bearer JWT",
        "ADMIN/SUPER_ADMIN for internal successful-payment verification; students are denied",
        "Internal payment verification has admin-success, student-denial, and malformed-ID coverage.",
    ),
    "PaymentAnalyticsController": (
        "Bearer JWT",
        "ADMIN/SUPER_ADMIN",
        "Add administrator success and student/anonymous denial checks.",
    ),
    "InternalAuthProfileController": (
        "X-Internal-Token shared-secret header",
        "Backend-configured trusted service token; no end-user role substitutes for it",
        "Add negative missing/invalid-token checks; positive identity lifecycle tests require approved secret and disposable user fixture.",
    ),
    "UserAnalyticsController": (
        "Bearer JWT",
        "ADMIN/SUPER_ADMIN",
        "Add administrator success and student/anonymous denial checks for each exposed class-prefix route.",
    ),
}


def main() -> int:
    with MATRIX.open(newline="", encoding="utf-8-sig") as stream:
        routes = list(csv.DictReader(stream))
    internal = [row for row in routes if row["exposure"].startswith("internal")]
    unknown = sorted({row["controller"] for row in internal} - AUTH_MODELS.keys())
    if unknown:
        raise SystemExit("No reviewed authentication model for controllers: " + ", ".join(unknown))
    fields = [
        "service", "http_method", "effective_path", "controller", "java_method", "source_file",
        "auth_model", "required_identity_or_roles", "current_route_test_status", "test_evidence",
        "source_reviewed_test_action",
    ]
    with OUTPUT.open("w", newline="", encoding="utf-8-sig") as stream:
        writer = csv.DictWriter(stream, fieldnames=fields)
        writer.writeheader()
        for row in internal:
            auth, identity, action = AUTH_MODELS[row["controller"]]
            if row["controller"] == "AssignmentAnalyticsController" and row["java_method"] == "getOverview":
                identity = "ADMIN/SUPER_ADMIN"
            elif row["controller"] == "QuizAnalyticsContractController" and row["java_method"] == "getOverview":
                identity = "ADMIN/SUPER_ADMIN"
            elif row["controller"] == "QuizAnalyticsController" and row["java_method"] == "getOverview":
                identity = "ADMIN/SUPER_ADMIN"
            elif row["controller"] == "QuizAnalyticsController" and row["java_method"] == "getTodayQuizPerformance":
                identity = "TEACHER/ADMIN/SUPER_ADMIN"
            elif row["controller"] == "AttendanceAnalyticsController" and row["java_method"] == "getOverview":
                identity = "ADMIN/SUPER_ADMIN"
            writer.writerow({
                "service": row["service"], "http_method": row["http_method"],
                "effective_path": row["effective_path"], "controller": row["controller"],
                "java_method": row["java_method"], "source_file": row["source_file"], "auth_model": auth,
                "required_identity_or_roles": identity, "current_route_test_status": row["coverage_status"],
                "test_evidence": row["test_evidence"], "source_reviewed_test_action": action,
            })
    print(f"internal routes classified={len(internal)}; auth models={len(AUTH_MODELS)}; unknown controllers=0")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
