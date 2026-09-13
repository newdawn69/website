// Dark mode toggle - persists choice in localStorage so it survives page navigation/reloads
document.addEventListener('DOMContentLoaded', function () {
    const toggleButton = document.getElementById('theme-toggle');
    if (!toggleButton) return;

    function updateButtonLabel() {
        const current = document.documentElement.getAttribute('data-bs-theme');
        toggleButton.textContent = current === 'dark' ? '☀️ Light mode' : '🌙 Dark mode';
    }

    updateButtonLabel();

    toggleButton.addEventListener('click', function () {
        const current = document.documentElement.getAttribute('data-bs-theme');
        const next = current === 'dark' ? 'light' : 'dark';
        document.documentElement.setAttribute('data-bs-theme', next);
        localStorage.setItem('theme', next);
        updateButtonLabel();
    });
});

// Toggles a password field between hidden and visible when its "eye" button is clicked
document.addEventListener('click', function (e) {
    const btn = e.target.closest('.toggle-password');
    if (!btn) return;

    const targetId = btn.getAttribute('data-target');
    const input = document.getElementById(targetId);
    if (!input) return;

    const icon = btn.querySelector('i');

    if (input.type === 'password') {
        input.type = 'text';
        if (icon) {
            icon.classList.remove('bi-eye');
            icon.classList.add('bi-eye-slash');
        } else {
            btn.textContent = 'Hide';
        }
    } else {
        input.type = 'password';
        if (icon) {
            icon.classList.remove('bi-eye-slash');
            icon.classList.add('bi-eye');
        } else {
            btn.textContent = 'Show';
        }
    }
});

// Live password strength hint on the register page
document.addEventListener('input', function (e) {
    if (e.target.id === 'password') {
        const value = e.target.value;
        const hint = document.getElementById('password-strength-hint');
        if (!hint) return;

        const hasUpper = /[A-Z]/.test(value);
        const hasNumber = /[0-9]/.test(value);
        const longEnough = value.length >= 8;

        if (longEnough && hasUpper && hasNumber) {
            hint.textContent = 'Strong password';
            hint.className = 'form-text text-success';
        } else {
            hint.textContent = 'At least 8 characters, one uppercase letter, one number';
            hint.className = 'form-text text-muted';
        }
    }

    // Live "passwords match" check on register page
    if (e.target.id === 'confirmPassword' || e.target.id === 'password') {
        const password = document.getElementById('password');
        const confirm = document.getElementById('confirmPassword');
        const matchHint = document.getElementById('password-match-hint');
        if (!password || !confirm || !matchHint) return;

        if (confirm.value.length === 0) {
            matchHint.textContent = '';
            return;
        }

        if (password.value === confirm.value) {
            matchHint.textContent = 'Passwords match';
            matchHint.className = 'form-text text-success';
        } else {
            matchHint.textContent = 'Passwords do not match';
            matchHint.className = 'form-text text-danger';
        }
    }
});
