const TAG_NOMBRES = { 1:'Java', 2:'Spring Boot', 3:'SQL', 4:'JPA', 5:'REST', 6:'Docker', 7:'Testing', 8:'Frontend' };

let detalle = null;

function getPreguntaId() {
  return new URLSearchParams(location.search).get('id');
}

document.addEventListener('DOMContentLoaded', () => {
  if (!requireAuth()) return;
  initHeader();
  const id = getPreguntaId();
  if (!id) { location.href = 'index.html'; return; }
  cargar(id);

  document.getElementById('form-respuesta').addEventListener('submit', async e => {
    e.preventDefault();
    const contenido = document.getElementById('nueva-respuesta').value.trim();
    if (!contenido) return;
    const btn = e.target.querySelector('button[type=submit]');
    btn.disabled = true;
    try {
      await api.publicarRespuesta({ usuarioId: getCurrentUserId(), preguntaId: parseInt(id), contenido });
      document.getElementById('nueva-respuesta').value = '';
      toast('Respuesta publicada', 'ok');
      await cargar(id);
    } catch (err) {
      toast(err.message, 'err');
    } finally {
      btn.disabled = false;
    }
  });
});

async function cargar(id) {
  try {
    detalle = await api.obtenerPregunta(id);
    renderPregunta();
    renderRespuestas();
  } catch (e) {
    document.getElementById('main').innerHTML = `<div class="empty"><p>Error: ${esc(e.message)}</p></div>`;
  }
}

function renderPregunta() {
  const q   = detalle.pregunta;
  const uid = getCurrentUserId();
  document.title = q.titulo + ' — Foro Dev';
  document.getElementById('pregunta-titulo').textContent = q.titulo;
  document.getElementById('pregunta-contenido').textContent = q.contenido;
  document.getElementById('breadcrumb-titulo').textContent =
    q.titulo.length > 60 ? q.titulo.slice(0, 60) + '…' : q.titulo;
  document.getElementById('pregunta-tags').innerHTML = (q.etiquetaIds || [])
    .map(t => `<span class="tag">${TAG_NOMBRES[t] || 'tag-' + t}</span>`).join('');

  const footer = document.getElementById('pregunta-footer');
  footer.innerHTML =
    `<button class="btn btn-ghost btn-sm" onclick="reportarPregunta()">⚑ Reportar</button>` +
    (q.autorId === uid
      ? `<button class="btn btn-ghost btn-sm" style="color:var(--danger)"
           onclick="borrarPregunta(${q.id})">✕ Eliminar pregunta</button>`
      : '') +
    `<span class="author-chip">usuario&nbsp;<strong>#${q.autorId}</strong></span>`;
}

function renderRespuestas() {
  const q   = detalle.pregunta;
  const rs  = detalle.respuestas || [];
  const uid = getCurrentUserId();
  const esAutorPregunta = q.autorId === uid;
  const hayAceptada     = !!q.acceptedAnswerId;

  document.getElementById('num-respuestas').textContent =
    `${rs.length} respuesta${rs.length !== 1 ? 's' : ''}`;

  const cont = document.getElementById('respuestas');
  if (!rs.length) {
    cont.innerHTML = '<div class="empty"><p>Aún no hay respuestas. ¡Sé el primero!</p></div>';
    return;
  }

  cont.innerHTML = rs.map(r => {
    const aceptada    = r.esAceptada || r.id === q.acceptedAnswerId;
    const puedeAceptar = esAutorPregunta && !hayAceptada && !aceptada;
    const puedeBorrar  = r.autorId === uid || esAutorPregunta;
    return `
      <div class="answer-card ${aceptada ? 'accepted-answer' : ''}" id="ans-${r.id}">
        ${aceptada ? '<div class="accepted-label">✓ Respuesta aceptada</div>' : ''}
        <div class="post-layout">
          <div class="vote-col">
            <button class="vote-btn up" onclick="votar(${r.id},1)" title="+1">▲</button>
            <span class="vote-score" id="score-${r.id}">${r.score}</span>
            <button class="vote-btn down" onclick="votar(${r.id},-1)" title="-1">▼</button>
          </div>
          <div class="post-body">
            <div class="post-content">${esc(r.contenido)}</div>
            <div class="post-footer">
              <button class="btn btn-ghost btn-sm" onclick="reportar(${r.id})">⚑ Reportar</button>
              ${puedeAceptar
                ? `<button class="btn btn-accept btn-sm" onclick="aceptar(${r.id})">✓ Aceptar</button>`
                : ''}
              ${puedeBorrar
                ? `<button class="btn btn-ghost btn-sm" style="color:var(--danger)"
                     onclick="borrarRespuesta(${r.id})">✕ Eliminar</button>`
                : ''}
              <span class="author-chip">usuario&nbsp;<strong>#${r.autorId}</strong></span>
            </div>
          </div>
        </div>
      </div>`;
  }).join('');
}

/* CU1 — votar */
async function votar(respuestaId, valor) {
  try {
    const res = await api.votar(getCurrentUserId(), respuestaId, valor);
    document.getElementById(`score-${respuestaId}`).textContent = res.nuevoScore;
    toast('Voto registrado', 'ok');
  } catch (e) {
    toast(e.message, 'err');
  }
}

/* CU4 — aceptar respuesta */
async function aceptar(respuestaId) {
  if (!confirm('¿Marcar esta respuesta como aceptada? No podrás cambiarlo.')) return;
  try {
    await api.aceptarRespuesta(respuestaId, getCurrentUserId());
    toast('Respuesta aceptada ✓', 'ok');
    await cargar(getPreguntaId());
  } catch (e) {
    toast(e.message, 'err');
  }
}

/* CU2 — reportar */
async function reportarPregunta() {
  const motivo = prompt('Motivo del reporte:');
  if (!motivo?.trim()) return;
  try {
    const res = await api.reportar(detalle.pregunta.id, getCurrentUserId(), motivo);
    mostrarToastReporte(res);
  } catch (e) {
    toast(e.message, 'err');
  }
}

async function reportar(publicacionId) {
  const motivo = prompt('Motivo del reporte:');
  if (!motivo?.trim()) return;
  try {
    const res = await api.reportar(publicacionId, getCurrentUserId(), motivo);
    mostrarToastReporte(res);
  } catch (e) {
    toast(e.message, 'err');
  }
}

function mostrarToastReporte(res) {
  if (res.publicacionOculta) {
    toast('La publicación ha sido ocultada por exceso de reportes', 'ok');
    setTimeout(() => cargar(getPreguntaId()), 800);
  } else if (res.reportesRestantes === 1) {
    toast('Reporte enviado. Con 1 reporte más esta publicación será ocultada.', 'ok');
  } else if (res.reportesRestantes === 2) {
    toast('Reporte enviado. Faltan ' + res.reportesRestantes + ' reportes para ocultar la publicación.', 'ok');
  } else {
    toast('Reporte enviado', 'ok');
  }
}

/* Eliminar pregunta */
async function borrarPregunta(preguntaId) {
  if (!confirm('¿Eliminar esta pregunta? Esta acción no se puede deshacer.')) return;
  try {
    await api.eliminarPregunta(preguntaId, getCurrentUserId());
    toast('Pregunta eliminada', 'ok');
    setTimeout(() => { location.href = 'index.html'; }, 700);
  } catch (e) {
    toast(e.message, 'err');
  }
}

/* Eliminar respuesta */
async function borrarRespuesta(respuestaId) {
  if (!confirm('¿Eliminar esta respuesta?')) return;
  try {
    await api.eliminarRespuesta(respuestaId, getCurrentUserId());
    toast('Respuesta eliminada', 'ok');
    await cargar(getPreguntaId());
  } catch (e) {
    toast(e.message, 'err');
  }
}
