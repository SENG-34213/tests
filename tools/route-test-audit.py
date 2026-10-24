#!/usr/bin/env python3
"""Correlate backend route rows with test-client requests and JUnit callers.

This is a source-level audit aid, not a runtime coverage report. A route is only
reported as exercised when a test method calls a client method whose HTTP verb
and path expression match the route. Assertions are reported separately.
"""

from __future__ import annotations

import csv
import json
import re
import argparse
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
TEST_ROOT = ROOT / "src/test/java"
ROUTES = ROOT / "docs/backend-effective-route-inventory.csv"
CASES = ROOT / "docs/test-case-mappings.csv"
GATEWAY_MATRIX = ROOT / "docs/backend-gateway-effective-route-matrix.csv"
HTTP_METHODS = {"get", "post", "put", "patch", "delete", "postMultipart", "putMultipart", "postWithHeaders"}
ASSERTION = re.compile(r"\b(assert\w*|assertEquals|assertTrue|assertFalse|assertNotNull|assertNull|assertThrows)\s*\(|\.statusCode\s*\(|ApiResponseAssertions\.")
MULTIPART_ROUTES = {
    ("QuizGenerationController", "generateQuiz"),
    ("AssignmentAttachmentController", "uploadAttachment"),
    ("AiQuizGenerationController", "generateQuizDraft"),
    ("SubmissionFileController", "uploadFile"),
    ("ExamAttachmentController", "upload"),
    ("ExamController", "createExamWithInstructions"),
    ("ExamController", "updateExamWithInstructions"),
    ("MaterialController", "uploadMaterial"),
    ("MaterialController", "uploadMarketplaceMaterial"),
    ("ProfileImageController", "upload"),
}


def blocks(source: str, declaration: re.Pattern[str]):
    """Yield (name, body) for simple Java method declarations."""
    for match in declaration.finditer(source):
        opening = source.find("{", match.start(), match.end())
        if opening < 0:
            continue
        depth = 0
        quoted = None
        escaped = False
        for index in range(opening, len(source)):
            char = source[index]
            if quoted:
                if escaped:
                    escaped = False
                elif char == "\\":
                    escaped = True
                elif char == quoted:
                    quoted = None
                continue
            if char in "\"'":
                quoted = char
            elif char == "{":
                depth += 1
            elif char == "}":
                depth -= 1
                if depth == 0:
                    yield match.group(1), source[opening + 1 : index]
                    break


def split_arguments(text: str) -> list[str]:
    result, start, depth, quoted, escaped = [], 0, 0, None, False
    for index, char in enumerate(text):
        if quoted:
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == quoted:
                quoted = None
        elif char in "\"'":
            quoted = char
        elif char in "([{":
            depth += 1
        elif char in ")]}":
            depth -= 1
        elif char == "," and depth == 0:
            result.append(text[start:index].strip())
            start = index + 1
    result.append(text[start:].strip())
    return result


def call_arguments(body: str, name: str):
    pattern = re.compile(r"\b(?:apiClient|client)\." + re.escape(name) + r"\s*\(")
    for match in pattern.finditer(body):
        start = match.end()
        depth, quoted, escaped = 1, None, False
        for index in range(start, len(body)):
            char = body[index]
            if quoted:
                if escaped:
                    escaped = False
                elif char == "\\":
                    escaped = True
                elif char == quoted:
                    quoted = None
            elif char in "\"'":
                quoted = char
            elif char == "(":
                depth += 1
            elif char == ")":
                depth -= 1
                if depth == 0:
                    yield split_arguments(body[start:index])
                    break


def expand_helpers(expression: str, helpers: dict[str, tuple[list[str], str]]) -> str:
    """Expand simple local String path helper calls, e.g. eventPath(id)."""
    for _ in range(8):
        changed = False
        for name, (parameters, returned) in helpers.items():
            match = re.search(r"\b" + re.escape(name) + r"\s*\(", expression)
            if not match:
                continue
            opening = expression.find("(", match.start())
            depth, quoted, escaped, end = 1, None, False, None
            for index in range(opening + 1, len(expression)):
                char = expression[index]
                if quoted:
                    if escaped:
                        escaped = False
                    elif char == "\\":
                        escaped = True
                    elif char == quoted:
                        quoted = None
                elif char in "\"'":
                    quoted = char
                elif char == "(":
                    depth += 1
                elif char == ")":
                    depth -= 1
                    if depth == 0:
                        end = index
                        break
            if end is None:
                continue
            values = split_arguments(expression[opening + 1 : end])
            if returned.startswith("__PATH_SEGMENTS__:"):
                base = returned.split(":", 1)[1]
                replacement = base + "".join(" + \"/\" + (" + value + ")" for value in values)
            else:
                replacement = returned
                for parameter, value in zip(parameters, values):
                    replacement = re.sub(r"\b" + re.escape(parameter) + r"\b", "(" + value + ")", replacement)
            expression = expression[: match.start()] + "(" + replacement + ")" + expression[end + 1 :]
            changed = True
        if not changed:
            break
    return expression


def normalize_path(expression: str, constants: dict[str, str], parameters: set[str]) -> str:
    expression = re.sub(r"\s+", " ", expression.strip())
    for name, value in sorted(constants.items(), key=lambda item: -len(item[0])):
        expression = re.sub(r"\b" + re.escape(name) + r"\b", json.dumps(value), expression)
    expression = re.sub(r'id\((\w+),\s*"[^"]+"\)', r"\1", expression)
    pieces = re.findall(r'"((?:\\.|[^"\\])*)"|\b([A-Z][A-Z0-9_]+|[a-zA-Z_$][\w$]*)\b', expression)
    value = ""
    for string, identifier in pieces:
        if string:
            value += bytes(string, "utf-8").decode("unicode_escape")
        elif identifier in parameters:
            value += "{}"
        elif identifier not in {"null", "this"}:
            # Unknown expressions (for example conditional path selection) are
            # not accepted as route evidence.
            return ""
    return value


def path_matches(route: str, client_path: str) -> bool:
    if not route or not client_path:
        return False
    pattern = "^" + re.escape(client_path).replace(r"\{\}", r"[^/]+") + "$"
    return re.match(pattern, route.rstrip("/") or "/") is not None


def route_response_assertion(body: str, client_variable: str, client_method: str) -> str:
    """Return a concise assertion snippet tied to this client's response."""
    calls = list(re.finditer(r"\b" + re.escape(client_variable) + r"\." + re.escape(client_method) + r"\s*\(", body))
    for call in calls:
        opening = body.find("(", call.start())
        depth, quoted, escaped, closing = 1, None, False, None
        for index in range(opening + 1, len(body)):
            char = body[index]
            if quoted:
                if escaped:
                    escaped = False
                elif char == "\\":
                    escaped = True
                elif char == quoted:
                    quoted = None
            elif char in "\"'":
                quoted = char
            elif char == "(":
                depth += 1
            elif char == ")":
                depth -= 1
                if depth == 0:
                    closing = index
                    break
        if closing is None:
            continue
        statement_start = body.rfind(";", 0, call.start()) + 1
        prefix = body[statement_start : call.start()]
        statement_end = body.find(";", closing)
        statement = body[statement_start : statement_end if statement_end >= 0 else len(body)]
        if re.search(r"\bassertDenied\s*\(\s*" + re.escape(client_variable) + r"\." + re.escape(client_method) + r"\s*\(", statement):
            return "assertDenied(401/403): assertTrue(Set.of(401, 403).contains(response.statusCode()))"
        assignment = re.search(r"\b(?:Response|var)\s+(\w+)\s*=\s*[\w.$]*$", prefix.strip())
        if assignment:
            response_var = assignment.group(1)
            assertion = re.search(
                r"\b(assert\w*)\s*\(([^;]*?\b" + re.escape(response_var) + r"\b[^;]*?)\)",
                body[closing + 1 :],
                re.S,
            )
            if assertion:
                return re.sub(r"\s+", " ", assertion.group(0)).strip()[:180]
        else:
            if re.search(r"\bassert\w*\s*\(", statement) and re.search(r"\.statusCode\s*\(\s*\)", statement):
                return re.sub(r"\s+", " ", statement).strip()[:180]
    return ""


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path, help="Write the enriched route matrix to this CSV")
    parser.add_argument("--debug-client", help="Print resolved HTTP calls for one test client")
    cli = parser.parse_args()
    with ROUTES.open(newline="", encoding="utf-8-sig") as stream:
        routes = list(csv.DictReader(stream))
    with CASES.open(newline="", encoding="utf-8-sig") as stream:
        cases = list(csv.DictReader(stream))
    with GATEWAY_MATRIX.open(newline="", encoding="utf-8-sig") as stream:
        gateway_rows = list(csv.DictReader(stream))
    gateway_columns = [
        "explicit_gateway_route_ids", "explicit_gateway_ingress_paths", "explicit_gateway_uris",
        "explicit_gateway_filters", "service_discovery_fallback_path", "upstream_path_for_explicit_routes",
        "conflicting_gateway_route_ids", "conflicting_gateway_services", "resolution_status",
    ]
    gateway_by_route = {
        (row["service"], row["http_method"], row["controller_path"], row["controller"],
         row["java_method"], row["source_file"]): row
        for row in gateway_rows
    }
    case_ids = {(row["class_name"], row["method_name"]): row["test_case_id"] for row in cases}

    clients = []
    client_method = re.compile(r"\bpublic\s+Response\s+(\w+)\s*\(([^)]*)\)\s*\{")
    for path in TEST_ROOT.rglob("*Client.java"):
        if path.name == "ApiClient.java":
            continue
        source = path.read_text(encoding="utf-8", errors="replace")
        constants = dict(re.findall(r'\b(?:static\s+)?final\s+String\s+(\w+)\s*=\s*"([^"]*)"\s*;', source))
        helper_decl = re.compile(r"\bprivate\s+static\s+String\s+(\w+)\s*\(([^)]*)\)\s*\{")
        helpers = {}
        for helper_name, helper_body in blocks(source, helper_decl):
            signature = re.search(r"\bprivate\s+static\s+String\s+" + re.escape(helper_name) + r"\s*\(([^)]*)\)", source)
            returned = re.search(r"\breturn\s+(.+?);", helper_body, re.S)
            if signature:
                helper_params = re.findall(r"\b(?:String(?:\.\.\.)?|int|long|boolean)\s+(\w+)\b", signature.group(1))
                if "new StringBuilder(" in helper_body and ".append('/').append(" in helper_body:
                    base = re.search(r"new StringBuilder\((\w+)\)", helper_body)
                    if base:
                        helpers[helper_name] = (helper_params, "__PATH_SEGMENTS__:" + base.group(1))
                elif returned:
                    helpers[helper_name] = (helper_params, returned.group(1).strip())
        for name, body in blocks(source, client_method):
            for verb in HTTP_METHODS:
                for args in call_arguments(body, verb):
                    if not args:
                        continue
                    # Method parameters only: re-read the matching signature.
                    signature = re.search(r"\bpublic\s+Response\s+" + re.escape(name) + r"\s*\(([^)]*)\)", source)
                    params = set(re.findall(r"\b([A-Za-z_$][\w$]*)\s*(?:,|$)", signature.group(1))) if signature else set()
                    expanded = expand_helpers(args[0], helpers)
                    route_path = normalize_path(expanded, constants, params)
                    http = "POST" if verb in {"postMultipart", "postWithHeaders"} else "PUT" if verb == "putMultipart" else verb.upper()
                    clients.append((path.parent.name, path.stem, name, http, route_path, verb))

    client_types = {client[1] for client in clients}
    if cli.debug_client:
        for client in clients:
            if client[1] == cli.debug_client:
                print(client, file=sys.stderr)
    client_variables: dict[str, dict[str, set[str]]] = {}
    for path in TEST_ROOT.rglob("*.java"):
        source = path.read_text(encoding="utf-8", errors="replace")
        for client_type in client_types:
            variables = set(re.findall(r"\b" + re.escape(client_type) + r"\s+(\w+)\b", source))
            if variables:
                client_variables.setdefault(path.parent.name, {}).setdefault(client_type, set()).update(variables)

    test_decl = re.compile(r"\b(?:void)\s+(\w+)\s*\([^)]*\)\s*(?:throws[^{]+)?\{")
    tests = []
    for path in TEST_ROOT.rglob("*.java"):
        # Client unit tests verify local argument validation and must not count
        # as evidence that an HTTP endpoint was requested.
        if not path.name.endswith("IT.java"):
            continue
        source = path.read_text(encoding="utf-8", errors="replace")
        for name, body in blocks(source, test_decl):
            tests.append((path.parent.name, path.stem, name, body, bool(ASSERTION.search(body))))

    total = len(routes)
    linked = assertion_review = uncovered = unresolved = 0
    audited = []
    for route in routes:
        gateway_key = (
            route["service"], route["http_method"], route["effective_path"], route["controller"],
            route["java_method"], route["source_file"],
        )
        gateway = gateway_by_route.get(gateway_key)
        if gateway is None:
            raise SystemExit(f"Route has no Gateway matrix row: {gateway_key}")
        path = route["effective_path"]
        gateway_test_paths = [path]
        gateway_test_paths.extend(
            value for value in gateway["explicit_gateway_ingress_paths"].split(";") if value
        )
        gateway_test_paths.append(gateway["service_discovery_fallback_path"])
        consumes = "multipart/form-data" if (route["controller"], route["java_method"]) in MULTIPART_ROUTES else "not explicitly constrained"
        if not path:
            unresolved += 1
            audit_status = "UNRESOLVED_CONTROLLER_PATH"
            evidence = ""
            audited.append({**route, **{key: gateway[key] for key in gateway_columns},
                            "request_content_type": consumes, "test_evidence": evidence, "coverage_status": audit_status})
            continue
        matches = [
            client
            for client in clients
            if client[3] == route["http_method"]
            and any(path_matches(gateway_path, client[4]) for gateway_path in gateway_test_paths)
            and (((route["controller"], route["java_method"]) in MULTIPART_ROUTES)
                 == (client[5] in {"postMultipart", "putMultipart"}))
        ]
        evidence = []
        outcomes: set[str] = set()
        for _, client_class, client_method_name, request_verb, client_path, _ in matches:
            for test_package, test_class, test_method, body, has_assertion in tests:
                variables = client_variables.get(test_package, {}).get(client_class, set())
                if not variables or not re.search(r"\b(?:" + "|".join(map(re.escape, variables)) + r")\." + re.escape(client_method_name) + r"\s*\(", body):
                    continue
                case_id = case_ids.get((test_class, test_method), "NO_CASE_ID")
                for variable in variables:
                    assertion = route_response_assertion(body, variable, client_method_name)
                    if assertion:
                        expected = re.search(r"assertEquals\s*\(\s*(\d{3})", assertion)
                        if not expected:
                            expected = re.search(r"assertSuccessful\s*\([^,]+,\s*(\d{3})", assertion)
                        if expected and 200 <= int(expected.group(1)) < 300:
                            outcomes.add("success")
                        elif expected and int(expected.group(1)) >= 400:
                            outcomes.add("negative")
                        elif "assertDenied(401/403)" in assertion:
                            outcomes.add("negative")
                        else:
                            outcomes.add("other")
                        evidence.append(f"{case_id}:{test_class}#{test_method} {client_class}.{client_method_name} [{request_verb} {client_path}]; assertion: {assertion}")
                    elif re.search(r"\b" + re.escape(variable) + r"\." + re.escape(client_method_name) + r"\s*\(", body):
                        evidence.append(f"{case_id}:{test_class}#{test_method} {client_class}.{client_method_name} [{request_verb} {client_path}]; assertion not linked")
        if evidence and any("assertion:" in item for item in evidence):
            if {"success", "negative"}.issubset(outcomes):
                audit_status = "REQUEST_WITH_SUCCESS_AND_NEGATIVE_RESPONSE_ASSERTIONS_LINKED"
            elif "success" in outcomes:
                audit_status = "REQUEST_WITH_SUCCESS_RESPONSE_ASSERTION_LINKED"
            elif "negative" in outcomes:
                audit_status = "REQUEST_WITH_NEGATIVE_RESPONSE_ASSERTIONS_ONLY"
            else:
                audit_status = "REQUEST_WITH_ASSERTION_LINKED; EXPECTED OUTCOME NEEDS REVIEW"
            linked += 1
        elif evidence:
            audit_status = "REQUEST_FOUND; RESPONSE_ASSERTION_LINK_REQUIRES_REVIEW"
            assertion_review += 1
        elif route["exposure"].startswith("internal"):
            audit_status = "NO_TEST_REQUEST_FOUND; internal endpoint test gap"
            uncovered += 1
        else:
            audit_status = "NO_TEST_REQUEST_FOUND; endpoint test gap"
            uncovered += 1
        audited.append({**route, **{key: gateway[key] for key in gateway_columns},
                        "request_content_type": consumes, "test_evidence": "; ".join(evidence),
                        "coverage_status": audit_status})

    fields = list(routes[0].keys()) + gateway_columns + ["request_content_type", "test_evidence"]
    if cli.output:
        cli.output.parent.mkdir(parents=True, exist_ok=True)
        with cli.output.open("w", newline="", encoding="utf-8-sig") as stream:
            writer = csv.DictWriter(stream, fieldnames=fields)
            writer.writeheader()
            writer.writerows(audited)
    else:
        writer = csv.DictWriter(sys.stdout, fieldnames=fields)
        writer.writeheader()
        writer.writerows(audited)
    print(f"# totals: routes={total}; route requests with linked assertions={linked}; request present/assertion link review={assertion_review}; no request match={uncovered}; unresolved path={unresolved}", file=sys.stderr)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
