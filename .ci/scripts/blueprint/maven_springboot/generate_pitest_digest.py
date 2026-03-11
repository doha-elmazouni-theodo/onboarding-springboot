#!/usr/bin/env python3

"""Generate an order-insensitive digest for PIT mutation CSV reports."""

from __future__ import annotations

import argparse
import csv
import json
from pathlib import Path
from typing import List


def load_rows(csv_path: Path) -> List[List[str]]:
    with csv_path.open(newline="") as handle:
        reader = csv.reader(handle)
        rows = []
        for row in reader:
            if not row:
                continue
            trimmed = row[:-1] if len(row) > 0 else row
            rows.append(trimmed)
    if not rows:
        raise ValueError(f"Mutation report {csv_path} is empty")
    return rows


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate digest for PIT mutations CSV reports")
    parser.add_argument("--input", required=True, action="append", type=Path,
                        help="Path to a PIT mutations CSV report (pass once per report)")
    args = parser.parse_args()

    for path in args.input:
        if not path.is_file():
            raise FileNotFoundError(f"PIT report not found: {path}")

    for path in args.input:
        rows = load_rows(path)
        output = path.with_suffix(path.suffix + ".digest")
        with output.open("w", encoding="utf-8") as handle:
            for row in sorted(rows):
                handle.write(json.dumps(row, separators=(",", ":")))
                handle.write("\n")
        print(f"Wrote PIT digest with {len(rows)} entries to {output}")
    return 0


if __name__ == "__main__":  # pragma: no cover - CLI entry point
    raise SystemExit(main())
