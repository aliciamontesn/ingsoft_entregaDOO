// URLs inyectadas por config.js (permite cambiarlas para produccion sin tocar este fichero)
const API_PUB = (window.CONF && window.CONF.API_PUB) || 'http://localhost:8081';
const API_VOT = (window.CONF && window.CONF.API_VOT) || 'http://localhost:8082';
const AUTH_KEY = 'auth_uid';

/* ── Auth ── */
function getCurrentUserId() {
  return parseInt(localStorage.getItem(AUTH_KEY) || '0', 10);
}

function requireAuth() {
  if (!getCurrentUserId()) {
    location.href = 'login.html';
    return false;
  }
  return true;
}

function logout() {
  localStorage.removeItem(AUTH_KEY);
  location.href = 'login.html';
}

/* ── HTTP helper ── */
async function apiFetch(url, options = {}) {
  try {
    const res = await fetch(url, {
      headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
      ...options
    });
    const text = await res.text();
    if (!res.ok) {
      let msg;
      try { msg = JSON.parse(text).error; } catch { msg = text; }
      throw new Error(msg || `Error ${res.status}`);
    }
    if (!text) return null;
    return JSON.parse(text);
  } catch (e) {
    if (e instanceof TypeError) throw new Error('No se puede conectar con el backend. ¿Está arrancado?');
    throw e;
  }
}

/* ── API calls ── */
const api = {
  listarPreguntas:  ()        => apiFetch(`${API_PUB}/preguntas`),
  obtenerPregunta:  (id)      => apiFetch(`${API_PUB}/preguntas/${id}`),
  publicarPregunta: (data)    => apiFetch(`${API_PUB}/preguntas`, { method:'POST', body:JSON.stringify(data) }),
  eliminarPregunta: (id, uid) => apiFetch(`${API_PUB}/preguntas/${id}?usuarioId=${uid}`, { method:'DELETE' }),

  publicarRespuesta:  (data)      => apiFetch(`${API_PUB}/respuestas`, { method:'POST', body:JSON.stringify(data) }),
  eliminarRespuesta:  (id, uid)   => apiFetch(`${API_PUB}/respuestas/${id}?usuarioId=${uid}`, { method:'DELETE' }),

  aceptarRespuesta:    (id, uid) => apiFetch(`${API_PUB}/respuestas/${id}/aceptar`,
    { method:'PATCH', body:JSON.stringify({ usuarioId: uid }) }),
  desaceptarRespuesta: (id, uid) => apiFetch(`${API_PUB}/respuestas/${id}/desaceptar`,
    { method:'PATCH', body:JSON.stringify({ usuarioId: uid }) }),

  reportar: (pubId, uid, motivo) => apiFetch(`${API_PUB}/publicaciones/${pubId}/reportes`,
    { method:'POST', body:JSON.stringify({ usuarioId: uid, motivo }) }),

  votar: (uid, respuestaId, valor) => apiFetch(`${API_VOT}/votos`,
    { method:'POST', body:JSON.stringify({ usuarioId: uid, respuestaId, valor }) }),
};

/* ── Toast ── */
function toast(msg, type = 'info') {
  let el = document.getElementById('_toast');
  if (!el) {
    el = document.createElement('div');
    el.id = '_toast';
    el.className = 'toast';
    document.body.appendChild(el);
  }
  el.textContent = msg;
  el.className = `toast toast-${type} visible`;
  clearTimeout(el._t);
  el._t = setTimeout(() => { el.className = 'toast'; }, 3200);
}

/* ── Escape HTML ── */
function esc(str) {
  return String(str ?? '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

/* ── Format date ── */
function formatFecha(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  return d.toLocaleDateString('es-ES', { day:'2-digit', month:'short', year:'numeric' });
}

/* ── Header: muestra usuario logueado + botón salir ── */
function initHeader() {
  if (!requireAuth()) return;
  const container = document.getElementById('header-user');
  if (!container) return;
  const uid = getCurrentUserId();
  container.innerHTML =
    `<span class="uid-label">usuario&nbsp;<strong>#${uid}</strong></span>` +
    `<button onclick="logout()" class="btn btn-outline btn-sm">Salir</button>`;
}
