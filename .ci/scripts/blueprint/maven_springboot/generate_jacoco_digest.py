#!/usr/bin/env python3

"""Generate an order-insensitive digest for Jacoco CSV reports."""

from __future__ import annotations

import argparse
import csv
import json
from pathlib import Path
from typing import List


def load_rows(csv_path: Path) -> tuple[List[str], List[List[str]]]:
    with csv_path.open(newline="") as handle:
        reader = csv.reader(handle)
        try:
            header = next(reader)
        except StopIteration as exc:  # pragma: no cover - defensive
            raise ValueError(f"Jacoco report {csv_path} is empty") from exc
        rows = [row for row in reader]
    return header, rows


def write_digest(header: List[str], rows: List[List[str]], output_path: Path) -> None:
    output_path.parent.mkdir(parents=True, exist_ok=True)
    with output_path.open("w", encoding="utf-8") as handle:
        handle.write("header::" + json.dumps(header, separators=(",", ":")) + "\n")
        for row in sorted(rows):
            handle.write("row::" + json.dumps(row, separators=(",", ":")) + "\n")
    print(f"Wrote Jacoco digest with {len(rows)} rows to {output_path}")


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate digest for Jacoco CSV reports")
    parser.add_argument("--input", required=True, action="append", type=Path,
                        help="Path to a Jacoco CSV report (pass once per report)")
    args = parser.parse_args()

    for path in args.input:
        if not path.is_file():
            raise FileNotFoundError(f"Jacoco report not found: {path}")

    for path in args.input:
        header, rows = load_rows(path)
        output = path.with_suffix(path.suffix + ".digest")
        write_digest(header, rows, output)
    return 0


if __name__ == "__main__":  # pragma: no cover - CLI entry point
    raise SystemExit(main())
