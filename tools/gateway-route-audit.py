#!/usr/bin/env python3
"""Join backend HTTP mappings to explicit Gateway routes or discovery fallbacks."""

from __future__ import annotations

import csv
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BACKEND_ROUTES = ROOT / "docs/backend-effective-route-inventory.csv"
GATEWAY_ROUTES = ROOT / "docs/backend-gateway-route-definitions.csv"
OUTPUT = ROOT / "docs/backend-gateway-effective-route-matrix.csv"


def predicates(value: str) -> list[str]:
    return [item.strip() for item in value.split(",") if item.strip()]


def path_regex(pattern: str) -> re.Pattern[str]:
    escaped = re.escape(pattern)
    escaped = escaped.replace(r"\*\*", ".*").replace(r"\*", "[^/]+")
    return re.compile("^" + escaped + "$")


def controller_path_matches(pattern: str, backend_path: str) -> bool:
    optional_tail = pattern.endswith("/**")
    base = pattern[:-3] if optional_tail else pattern
    regex = path_regex(base)
    candidate = re.sub(r"\{[^/{}]+\}", "segment", backend_path)
    if regex.fullmatch(candidate):
        return True
    if optional_tail:
        return candidate.startswith(base + "/")
    return False


def rewritten_ingress(gateway_route: dict[str, str], backend_path: str) -> str | None:
    match = re.search(r"RewritePath=([^, ]+),\s*([^ ]+)", gateway_route["filters"])
    if not match:
        return None
    incoming, upstream = match.groups()
    incoming_prefix = incoming.split("(?<", 1)[0]
    upstream_prefix = upstream.split("${", 1)[0].rstrip("$")
    if not upstream_prefix.endswith("/"):
        return None
    if backend_path.startswith(upstream_prefix):
        return incoming_prefix + backend_path[len(upstream_prefix):]
    return None


def main() -> int:
    with BACKEND_ROUTES.open(newline="", encoding="utf-8-sig") as stream:
        routes = list(csv.DictReader(stream))
    with GATEWAY_ROUTES.open(newline="", encoding="utf-8-sig") as stream:
        gateway = list(csv.DictReader(stream))
    fields = [
        "service", "http_method", "controller_path", "controller", "java_method", "source_file",
        "exposure", "explicit_gateway_route_ids", "explicit_gateway_ingress_paths",
        "explicit_gateway_uris", "explicit_gateway_filters", "service_discovery_fallback_path",
        "upstream_path_for_explicit_routes", "conflicting_gateway_route_ids",
        "conflicting_gateway_services", "resolution_status",
    ]
    with OUTPUT.open("w", newline="", encoding="utf-8-sig") as stream:
        writer = csv.DictWriter(stream, fieldnames=fields)
        writer.writeheader()
        unresolved = 0
        for backend in routes:
            backend_path = backend["effective_path"]
            matches: list[tuple[dict[str, str], str]] = []
            conflicts: list[dict[str, str]] = []
            if backend_path:
                for route in gateway:
                    matched_ingress: list[str] = []
                    for pattern in predicates(route["path_predicates"]):
                        if controller_path_matches(pattern, backend_path):
                            matched_ingress.append(backend_path)
                            break
                    if route["filters"].startswith("RewritePath="):
                        ingress = rewritten_ingress(route, backend_path)
                        if ingress:
                            matched_ingress.append(ingress)
                    if route["service"] == backend["service"]:
                        matches.extend((route, ingress) for ingress in dict.fromkeys(matched_ingress))
                    elif matched_ingress:
                        conflicts.append(route)
            discovery = next((row["service_id"] for row in gateway if row["service"] == backend["service"]), None)
            if discovery is None:
                raise SystemExit(f"Missing Gateway service alias for backend service {backend['service']}")
            fallback = f"/{discovery}/" if backend_path == "/" else f"/{discovery}{backend_path}"
            if not matches:
                unresolved += 1
            writer.writerow({
                "service": backend["service"], "http_method": backend["http_method"],
                "controller_path": backend_path, "controller": backend["controller"],
                "java_method": backend["java_method"], "source_file": backend["source_file"],
                "exposure": backend["exposure"],
                "explicit_gateway_route_ids": ";".join(dict.fromkeys(row[0]["route_id"] for row in matches)),
                "explicit_gateway_ingress_paths": ";".join(dict.fromkeys(ingress for _, ingress in matches)),
                "explicit_gateway_uris": ";".join(dict.fromkeys(row[0]["uri"] for row in matches)),
                "explicit_gateway_filters": ";".join(dict.fromkeys(row[0]["filters"] or "none" for row in matches)),
                "service_discovery_fallback_path": fallback,
                "upstream_path_for_explicit_routes": backend_path if matches else "not applicable",
                "conflicting_gateway_route_ids": ";".join(dict.fromkeys(row["route_id"] for row in conflicts)),
                "conflicting_gateway_services": ";".join(dict.fromkeys(row["service"] for row in conflicts)),
                "resolution_status": (
                    "DISCOVERY_FALLBACK_ONLY; PATH_COLLISION_TO_OTHER_SERVICE" if not matches and conflicts
                    else "NO_EXPLICIT_GATEWAY_RULE; DISCOVERY_FALLBACK_CONFIGURED" if not matches
                    else "MULTIPLE_EXPLICIT_RULES" if len(matches) > 1
                    else "EXPLICIT_GATEWAY_RULE"
                ),
            })
    print(f"backend routes={len(routes)}; rows reconciled={len(routes)}; discovery-fallback-only={unresolved}; no service aliases=0")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
