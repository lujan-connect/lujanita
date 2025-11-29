#!/usr/bin/env bash
set -euo pipefail

# scripts/dev-setup.sh
# Uso: ./scripts/dev-setup.sh [--install] [--no-start]
# --install   : crea .venv e instala requirements en apps/api, apps/services, apps/scrapper
# --no-start  : no arranca `npm run dev` al final

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CREDENTIALS_PATH="$REPO_ROOT/apps/api/credentials.json"
INSTALL=0
NO_START=0

print_usage() {
  cat <<EOF
Usage: $0 [--install] [--no-start]

Options:
  --install   Create virtualenvs and install Python requirements for api, services and scrapper
  --no-start  Do not run 'npm run dev' at the end

This script will:
 - check that $CREDENTIALS_PATH exists
 - set restrictive permissions on the credentials file
 - export GOOGLE_APPLICATION_CREDENTIALS pointing to that file
 - (optional) create .venv and install requirements
 - (optional) run npm run dev from repository root
EOF
}

# Parse args
while [[ ${#} -gt 0 ]]; do
  case "$1" in
    --install)
      INSTALL=1
      shift
      ;;
    --no-start)
      NO_START=1
      shift
      ;;
    -h|--help)
      print_usage
      exit 0
      ;;
    *)
      echo "Unknown arg: $1"
      print_usage
      exit 1
      ;;
  esac
done

# 1) Credenciales
if [ ! -f "$CREDENTIALS_PATH" ]; then
  echo "ERROR: No se encontró el archivo de credenciales: $CREDENTIALS_PATH"
  echo "Crea el archivo con el JSON de la cuenta de servicio y vuelve a ejecutar."
  exit 2
fi

# 2) Restrict permissions
chmod 600 "$CREDENTIALS_PATH" || true

# 3) Export variable para esta sesión (y para los procesos lanzados por este script)
export GOOGLE_APPLICATION_CREDENTIALS="$CREDENTIALS_PATH"
echo "Exported GOOGLE_APPLICATION_CREDENTIALS=$GOOGLE_APPLICATION_CREDENTIALS"

# 4) Optional: crear .venv e instalar requirements
if [ "$INSTALL" -eq 1 ]; then
  echo "==> Creando virtualenvs e instalando requirements (esto puede tardar)..."
  for svc in api services scrapper; do
    SVC_PATH="$REPO_ROOT/apps/$svc"
    if [ ! -d "$SVC_PATH" ]; then
      echo "Directory $SVC_PATH does not exist, skipping"
      continue
    fi
    echo "--> Procesando $svc"
    pushd "$SVC_PATH" >/dev/null
    PY_CMD=""
    if command -v python3.11 >/dev/null 2>&1; then
      PY_CMD=python3.11
    elif command -v python3 >/dev/null 2>&1; then
      PY_CMD=python3
    elif command -v python >/dev/null 2>&1; then
      PY_CMD=python
    else
      echo "No Python interpreter found for $svc, skipping virtualenv creation"
      popd >/dev/null
      continue
    fi

    if [ ! -d .venv ]; then
      echo "Creating virtualenv with $PY_CMD"
      $PY_CMD -m venv .venv
    fi
    # Upgrade pip and install
    ./.venv/bin/python -m pip install --upgrade pip setuptools wheel
    if [ -f requirements.txt ]; then
      ./.venv/bin/python -m pip install -r requirements.txt
    else
      echo "No requirements.txt in $SVC_PATH, skipping pip install"
    fi
    popd >/dev/null
  done
fi

# 5) Start monorepo (unless --no-start)
if [ "$NO_START" -eq 0 ]; then
  echo "==> Arrancando monorepo: npm run dev (desde $REPO_ROOT)"
  pushd "$REPO_ROOT" >/dev/null
  npm run dev
  popd >/dev/null
else
  echo "--no-start especificado: se exportó la variable y (opcional) se instaló, pero no se arrancó npm run dev"
fi

