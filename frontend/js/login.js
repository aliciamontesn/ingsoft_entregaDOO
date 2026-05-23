const AUTH_KEY = 'auth_uid';

// If already logged in, skip to main page
if (localStorage.getItem(AUTH_KEY)) {
  location.href = 'index.html';
}

document.getElementById('form-login').addEventListener('submit', e => {
  e.preventDefault();
  const uid = parseInt(document.getElementById('uid').value, 10);
  if (uid > 0) {
    localStorage.setItem(AUTH_KEY, uid);
    location.href = 'index.html';
  }
});
