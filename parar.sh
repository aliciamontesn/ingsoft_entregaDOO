#!/usr/bin/env bash
# Para todos los procesos del proyecto
# Uso: bash parar.sh

echo "Parando servicios..."

for f in pub vot fe; do
  if [ -f "/tmp/${f}.pid" ]; then
    pid=$(cat "/tmp/${f}.pid")
    kill "$pid" 2>/dev/null && echo "  parado PID $pid ($f)" || echo "  PID $pid ($f) ya no estaba corriendo"
    rm -f "/tmp/${f}.pid"
  fi
done

# Forzar cierre de los puertos por si acaso
for port in 8081 8082 5500; do
  pid=$(netstat -ano 2>/dev/null | grep ":${port}.*LISTENING" | awk '{print $NF}' | head -1)
  if [ -n "$pid" ] && [ "$pid" != "0" ]; then
    taskkill //F //PID "$pid" 2>/dev/null || true
    echo "  liberado puerto $port (PID $pid)"
  fi
done

echo "Hecho."
