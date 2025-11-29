#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
WIDGET_DIR="$ROOT_DIR/apps/widget"
FEATURE=${1:-}
EXTRA_ARGS="${@:2}"
if [[ -z "$FEATURE" ]]; then
  echo "Uso: $0 <ruta-feature-relativa-o-tag> [args adicionales]"
  echo "Ejemplo: $0 features/chat_send_message.feature"
  echo "         $0 --name 'Enviar mensaje'"
  exit 1
fi
# Ejecutar cucumber usando npm prefix para evitar problemas de cd y zsh compinit
npm --prefix "$WIDGET_DIR" run test:bdd -- "$FEATURE" $EXTRA_ARGS

