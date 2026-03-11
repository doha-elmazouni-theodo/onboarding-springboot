#!/usr/bin/env python3

"""Generate an order-insensitive digest for Surefire XML reports."""

from __future__ import annotations

import argparse
import json
from pathlib import Path
from typing import Dict, List
import xml.etree.ElementTree as ET


def normalise_text(value: str | None) -> str:
    if value is None:
        return ""
    stripped = value.strip()
    return stripped if stripped else ""


def element_to_dict(element: ET.Element, *, drop_time: bool) -> Dict:
    attrs = dict(element.attrib)
    if drop_time:
        attrs.pop("time", None)
    if element.tag in {"system-out", "system-err"}:
        return None
    ordered_attrs = {key: attrs[key] for key in sorted(attrs)}
    children = []
    for child in element:
        normalised = element_to_dict(child, drop_time=False)
        if normalised is not None:
            children.append(normalised)
    return {
        "tag": element.tag,
        "attributes": ordered_attrs,
        "text": normalise_text(element.text),
        "children": children,
    }


def collect_testcases(surefire_dir: Path) -> List[str]:
    entries: List[str] = []
    for xml_file in sorted(surefire_dir.glob("*.xml")):
        tree = ET.parse(xml_file)
        root = tree.getroot()
        for testcase in root.findall(".//testcase"):
            testcase_dict = element_to_dict(testcase, drop_time=True)
            if testcase_dict is None:
                continue
            payload = {
                "file": str(xml_file.relative_to(surefire_dir.parent)),
                "testcase": testcase_dict,
            }
            entries.append(json.dumps(payload, sort_keys=True, separators=(",", ":")))
    if not entries:
        raise ValueError(f"No testcase elements found under {surefire_dir}")
    return entries


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate digest for Surefire XML reports")
    parser.add_argument("--input", required=True, action="append", type=Path,
                        help="Path to a Surefire reports directory (pass once per report set)")
    args = parser.parse_args()

    for path in args.input:
        if not path.is_dir():
            raise FileNotFoundError(f"Surefire directory not found: {path}")

    for path in args.input:
        entries = collect_testcases(path)
        output = path / ".digest"
        with output.open("w", encoding="utf-8") as handle:
            for entry in sorted(entries):
                handle.write(entry)
                handle.write("\n")
        print(f"Wrote Surefire digest with {len(entries)} testcases to {output}")
    return 0


if __name__ == "__main__":  # pragma: no cover - CLI entry point
    raise SystemExit(main())
