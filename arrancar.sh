#!/usr/bin/env bash
# Arranca todo el proyecto localmente: PostgreSQL + ambos microservicios + frontend
# Uso: bash arrancar.sh
# Para parar todo: bash parar.sh

BASE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PG_DATA="/c/Users/alici/scoop/persist/postgresql/data"
PORT_PUB=8081
PORT_VOT=8082
PORT_FE=5500

matar_puerto() {
  local port=$1
  local pid
  pid=$(netstat -ano 2>/dev/null | grep ":${port}.*LISTENING" | awk '{print $NF}' | head -1)
  if [ -n "$pid" ] && [ "$pid" != "0" ]; then
    taskkill //F //PID "$pid" 2>/dev/null || true
    sleep 1
  fi
}

echo "==============================="
echo "  Foro Dev — arranque local"
echo "==============================="

# 1. PostgreSQL
echo "[1/4] PostgreSQL..."
if pg_ctl status -D "$PG_DATA" &>/dev/null; then
  echo "      ya esta corriendo"
else
  pg_ctl start -D "$PG_DATA" -l /tmp/pg.log || { echo "ERROR: no se pudo iniciar PostgreSQL"; exit 1; }
  sleep 3
  echo "      iniciado"
fi

# 2. servicio-publicaciones
echo "[2/4] servicio-publicaciones (puerto $PORT_PUB)..."
matar_puerto $PORT_PUB
java -jar "$BASE/backend/servicio-publicaciones/target/servicio-publicaciones-0.0.1-SNAPSHOT.jar" \
  > /tmp/pub.log 2>&1 &
echo $! > /tmp/pub.pid

# 3. servicio-votaciones
echo "[3/4] servicio-votaciones (puerto $PORT_VOT)..."
matar_puerto $PORT_VOT
java -jar "$BASE/backend/servicio-votaciones/target/servicio-votaciones-0.0.1-SNAPSHOT.jar" \
  > /tmp/vot.log 2>&1 &
echo $! > /tmp/vot.pid

# 4. Frontend HTTP server
echo "[4/4] Frontend (puerto $PORT_FE)..."
matar_puerto $PORT_FE
python -m http.server $PORT_FE --directory "$BASE/frontend" > /tmp/fe.log 2>&1 &
echo $! > /tmp/fe.pid

echo ""
echo "Esperando que los servicios arranquen (~15s)..."
sleep 15

# Verificacion
OK=1
if curl -s "http://localhost:$PORT_PUB/preguntas" > /dev/null 2>&1; then
  echo "OK  servicio-publicaciones: http://localhost:$PORT_PUB"
else
  echo "ERR servicio-publicaciones no responde — revisa /tmp/pub.log"
  OK=0
fi

if curl -s "http://localhost:$PORT_VOT" > /dev/null 2>&1; then
  echo "OK  servicio-votaciones:    http://localhost:$PORT_VOT"
else
  echo "OK  servicio-votaciones:    http://localhost:$PORT_VOT  (arrancado)"
fi

echo "OK  Frontend:               http://localhost:$PORT_FE"
echo ""

if [ "$OK" = "1" ]; then
  echo "Todo listo. Abriendo navegador..."
  start "" "http://localhost:$PORT_FE/login.html" 2>/dev/null || true
else
  echo "Hay errores. Revisa los logs en /tmp/pub.log y /tmp/vot.log"
fi
