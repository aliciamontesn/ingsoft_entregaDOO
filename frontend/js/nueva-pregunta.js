const ETIQUETAS = [
  { id:1, nombre:'Java' },
  { id:2, nombre:'Spring Boot' },
  { id:3, nombre:'SQL' },
  { id:4, nombre:'JPA' },
  { id:5, nombre:'REST' },
  { id:6, nombre:'Docker' },
  { id:7, nombre:'Testing' },
  { id:8, nombre:'Frontend' },
];

document.addEventListener('DOMContentLoaded', () => {
  if (!requireAuth()) return;
  initHeader();
  renderEtiquetas();

  document.getElementById('form-pregunta').addEventListener('submit', async e => {
    e.preventDefault();
    const titulo    = document.getElementById('titulo').value.trim();
    const contenido = document.getElementById('contenido').value.trim();
    const etiquetaIds = [...document.querySelectorAll('.tag-cb:checked')].map(cb => parseInt(cb.value));

    if (!titulo)    { toast('El título es obligatorio', 'err'); return; }
    if (!contenido) { toast('El contenido es obligatorio', 'err'); return; }

    const btn = document.getElementById('btn-publicar');
    btn.disabled = true;
    btn.textContent = 'Publicando…';

    try {
      const nueva = await api.publicarPregunta({
        usuarioId: getCurrentUserId(),
        titulo,
        contenido,
        etiquetaIds
      });
      toast('Pregunta publicada', 'ok');
      setTimeout(() => { location.href = `pregunta.html?id=${nueva.id}`; }, 700);
    } catch (err) {
      toast(err.message, 'err');
      btn.disabled = false;
      btn.textContent = 'Publicar pregunta';
    }
  });
});

function renderEtiquetas() {
  document.getElementById('etiquetas').innerHTML = ETIQUETAS.map(e =>
    `<input type="checkbox" class="tag-cb" id="tag-${e.id}" value="${e.id}">
     <label class="tag-lbl" for="tag-${e.id}">${e.nombre}</label>`
  ).join('');
}
