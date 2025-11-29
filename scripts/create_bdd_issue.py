"""Script para crear automáticamente un issue BDD en Jira a partir de un feature file Gherkin.

Requisitos:
  - Variables de entorno:
      JIRA_BASE_URL   (ej: https://faguero.atlassian.net)
      JIRA_EMAIL      (tu email de Atlassian)
      JIRA_API_TOKEN  (token API personal)
  - Paquetes: requests

Uso:
  python scripts/create_bdd_issue.py \
    --project TRAV \
    --type Tarea \
    --business TRAV-2 \
    --spec "specs/001- Registro Automático de Respuestas de Proveedores/spec.md" \
    --feature apps/services/features/email_reception.feature

Generará un issue con prefijo BDD: <Feature Name> y descripción con tabla de escenarios.
"""

from __future__ import annotations
import os
import re
import argparse
import requests
from typing import List, Tuple


SCENARIO_PATTERNS = [
    re.compile(r"^\s*Scenario:\s*(.+)$"),
    re.compile(r"^\s*Scenario Outline:\s*(.+)$"),
]

TAG_PATTERN = re.compile(r"^\s*@([\w:-]+)(?:\s+@[\w:-]+)*")


def parse_feature(path: str) -> Tuple[str, List[dict]]:
    with open(path, "r", encoding="utf-8") as f:
        lines = f.readlines()

    feature_name = ""
    scenarios: List[dict] = []
    current_tags: List[str] = []

    for i, line in enumerate(lines):
        if line.startswith("Feature:"):
            feature_name = line.split("Feature:", 1)[1].strip()
        elif line.strip().startswith("@"):
            # tags line
            tags = [t for t in line.strip().split() if t.startswith("@")]
            current_tags = tags
        else:
            for pattern in SCENARIO_PATTERNS:
                m = pattern.match(line)
                if m:
                    scenarios.append({
                        "name": m.group(1).strip(),
                        "type": "Scenario Outline" if "Outline" in pattern.pattern else "Scenario",
                        "tags": current_tags,
                    })
                    current_tags = []
                    break

    return feature_name, scenarios


def build_description(feature_path: str, spec_path: str, business_issue: str, feature_name: str, scenarios: List[dict]) -> str:
    rows = []
    for idx, sc in enumerate(scenarios, start=1):
        rows.append(f"| {idx} | {sc['type']} | {sc['name']} | {' '.join(sc['tags'])} |")
    table = "\n".join(["| # | Tipo | Nombre | Tags |", "|---|------|--------|------|", *rows])
    total = len(scenarios)
    return f"""## BDD: {feature_name}

Issue de test contenedor derivado de `{spec_path}` (Historia de negocio {business_issue}).

### Feature File
`{feature_path}`

### Escenarios
{table}

Total escenarios: {total}

### Comando de Ejecución Inicial
```bash
cd $(dirname {feature_path})/.. && behave features/$(basename {feature_path})
```

### Objetivo
Centralizar la trazabilidad BDD para la funcionalidad especificada.

### Definición de Done
- [ ] Steps definidos para todos los escenarios.
- [ ] Ejecución pasa local + CI.
- [ ] Comentario agregado en {business_issue} con referencia a este issue.

### Nota
Cada escenario debe incluir tag `@JIRA:{business_issue}` en el feature file.
""".strip()


def create_issue(base_url: str, email: str, token: str, project: str, issue_type: str, summary: str, description: str):
    url = f"{base_url}/rest/api/3/issue"
    payload = {
        "fields": {
            "project": {"key": project},
            "summary": summary,
            "issuetype": {"name": issue_type},
            "description": description,
        }
    }
    resp = requests.post(url, json=payload, auth=(email, token))
    if resp.status_code >= 300:
        raise SystemExit(f"Error creando issue: {resp.status_code} {resp.text}")
    data = resp.json()
    print(f"Issue creado: {data['key']}")
    return data['key']


def main():
    parser = argparse.ArgumentParser(description="Crear issue BDD en Jira desde feature file.")
    parser.add_argument("--project", required=True)
    parser.add_argument("--type", required=True, help="Nombre del tipo de issue (ej: Tarea, Historia)")
    parser.add_argument("--business", required=True, help="Clave del issue de negocio (ej: TRAV-2)")
    parser.add_argument("--spec", required=True, help="Ruta a spec.md fuente")
    parser.add_argument("--feature", required=True, help="Ruta al feature file .feature")
    args = parser.parse_args()

    base = os.getenv("JIRA_BASE_URL")
    email = os.getenv("JIRA_EMAIL")
    token = os.getenv("JIRA_API_TOKEN")
    if not all([base, email, token]):
        raise SystemExit("Faltan variables de entorno JIRA_BASE_URL, JIRA_EMAIL, JIRA_API_TOKEN")

    feature_name, scenarios = parse_feature(args.feature)
    description = build_description(args.feature, args.spec, args.business, feature_name, scenarios)
    summary = f"BDD: {feature_name}"
    create_issue(base, email, token, args.project, args.type, summary, description)


if __name__ == "__main__":
    main()
