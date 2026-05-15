#!/usr/bin/env python3
"""Generuje src/main/resources/db/migration/V12__seed_catalog_products.sql z plików txt."""

from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TXT_DIR = ROOT / "Informacje do bazy danych"
OUT = ROOT / "src/main/resources/db/migration/V12__seed_catalog_products.sql"

FILE_CATEGORY = {
    "opisy_wódek.txt": "vodka",
    "opisy_whisky.txt": "whisky",
    "opisy_win.txt": "wine",
    "opisy_piw.txt": "beer",
    "opisy_likierów.txt": "liqueur",
    "opisy_rum.txt": "rum",
}

BASE_PRICE = {
    "vodka": 54.99,
    "whisky": 149.99,
    "wine": 69.99,
    "beer": 4.49,
    "liqueur": 59.99,
    "rum": 79.99,
}

ABV_RE = re.compile(r"(\d+[,.]?\d*)\s*%", re.IGNORECASE)
VOL_ML_RE = re.compile(r"(\d+)\s*ml", re.IGNORECASE)
VOL_L_RE = re.compile(r"0[,.]?\s*5\s*l", re.IGNORECASE)
VOL_1L_RE = re.compile(r"1\s*litr", re.IGNORECASE)
VOL_330_RE = re.compile(r"0[,.]?\s*330", re.IGNORECASE)


def sql_str(value: str) -> str:
    return "'" + value.replace("'", "''") + "'"


def parse_abv(line: str) -> float | None:
    m = ABV_RE.search(line)
    if not m:
        return None
    return float(m.group(1).replace(",", "."))


def parse_volume_ml(line: str) -> int:
    for m in VOL_ML_RE.finditer(line):
        val = int(m.group(1))
        if val >= 100:
            return val
    if VOL_330_RE.search(line):
        return 330
    if VOL_L_RE.search(line):
        return 500
    if VOL_1L_RE.search(line):
        return 1000
    return 700


def parse_name(line: str, abv: float | None, volume_ml: int) -> str:
    name = line
    name = ABV_RE.sub("", name)
    name = VOL_ML_RE.sub("", name)
    name = re.sub(r"0[,.]?\s*5\s*l", "", name, flags=re.IGNORECASE)
    name = re.sub(r"1\s*litr", "", name, flags=re.IGNORECASE)
    name = re.sub(r"0[,.]?\s*330\s*litr?", "", name, flags=re.IGNORECASE)
    name = re.sub(r"\s*\|\s*", " ", name)
    name = re.sub(
        r"\b(ma|może mieć|mieć|lub|oraz|,)\b.*$",
        "",
        name,
        flags=re.IGNORECASE,
    )
    name = re.sub(r"\s+", " ", name).strip(" -|,")
    if len(name) < 3:
        return line.strip()[:255]
    return name[:255]


def price_for(category: str, index: int, abv: float | None) -> float:
    base = BASE_PRICE[category]
    bump = (index % 7) * 7.5
    if category == "whisky" and abv and abv >= 45:
        bump += 40
    if category == "beer":
        return round(base + (index % 5) * 0.35, 2)
    return round(base + bump, 2)


def load_products() -> list[dict]:
    products: list[dict] = []
    for filename, category in FILE_CATEGORY.items():
        path = TXT_DIR / filename
        if not path.exists():
            raise FileNotFoundError(path)
        idx = 0
        for raw in path.read_text(encoding="utf-8").splitlines():
            line = raw.strip()
            if not line:
                continue
            abv = parse_abv(line)
            volume_ml = parse_volume_ml(line)
            name = parse_name(line, abv, volume_ml)
            price = price_for(category, idx, abv)
            image = f"/products/{category}-mockup-{(idx % 3) + 1}.png"
            products.append(
                {
                    "name": name,
                    "description": line[:5000],
                    "category": category,
                    "price": price,
                    "volume_ml": volume_ml,
                    "abv": abv,
                    "image_url": image,
                    "stock": 50,
                }
            )
            idx += 1
    return products


def main() -> None:
    products = load_products()
    lines = [
        "-- Katalog produktów z folderu Informacje do bazy danych",
        "-- Uruchamiane raz przez Flyway (V12)",
        "",
        "UPDATE products SET is_active = FALSE WHERE name = 'Demo Vodka 500ml';",
        "",
    ]
    for p in products:
        abv_sql = "NULL" if p["abv"] is None else str(p["abv"])
        lines.append(
            "INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)\n"
            f"VALUES ({sql_str(p['name'])}, {sql_str(p['description'])}, {sql_str(p['category'])}, "
            f"{p['price']:.2f}, {p['volume_ml']}, {abv_sql}, {sql_str(p['image_url'])}, TRUE, NOW(), NOW());"
        )
        lines.append(
            "INSERT INTO product_stock (product_id, quantity, warehouse_zone)\n"
            f"VALUES (currval(pg_get_serial_sequence('products', 'id')), {p['stock']}, 'A1');"
        )
        lines.append("")

    OUT.write_text("\n".join(lines), encoding="utf-8")
    print(f"Wrote {len(products)} products to {OUT}")


if __name__ == "__main__":
    main()
