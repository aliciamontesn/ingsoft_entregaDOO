document.addEventListener('DOMContentLoaded', () => {
  if (!requireAuth()) return;
  initHeader();
  cargarPreguntas();
});

async function cargarPreguntas() {
  const lista = document.getElementById('lista');
  lista.innerHTML = '<div class="loading">Cargando preguntas…</div>';
  try {
    const preguntas = await api.listarPreguntas();
    document.getElementById('total').textContent = `(${preguntas.length})`;
    if (!preguntas.length) {
      lista.innerHTML = `
        <div class="empty">
          <p>Todavía no hay preguntas.</p>
          <a href="nueva-pregunta.html" class="btn btn-primary">Haz la primera pregunta</a>
        </div>`;
      return;
    }
    lista.innerHTML = preguntas.map(q => `
      <div class="question-card">
        <div class="q-stats">
          <span class="score">${q.score ?? 0}</span>
          <span>puntos</span>
          <span style="margin-top:6px;font-size:12px">${q.numRespuestas ?? 0}</span>
          <span>resp.</span>
          ${q.acceptedAnswerId ? '<span class="resolved-badge">✓</span>' : ''}
        </div>
        <div class="q-body">
          <a class="q-title" href="pregunta.html?id=${q.id}">${esc(q.titulo)}</a>
          <div class="q-excerpt">${esc(q.contenido)}</div>
          <div class="q-meta">
            ${(q.etiquetaIds || []).map(t => `<span class="tag">${tagNombre(t)}</span>`).join('')}
            <span class="spacer"></span>
            <span class="author">usuario&nbsp;#${q.autorId} · ${formatFecha(q.fechaCreacion)}</span>
          </div>
        </div>
      </div>`).join('');
  } catch (e) {
    lista.innerHTML = `<div class="empty"><p>Error: ${esc(e.message)}</p></div>`;
  }
}

function tagNombre(id) {
  const nombres = { 1:'Java', 2:'Spring Boot', 3:'SQL', 4:'JPA', 5:'REST', 6:'Docker', 7:'Testing', 8:'Frontend' };
  return nombres[id] || `tag-${id}`;
}
