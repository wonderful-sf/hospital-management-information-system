const form = document.querySelector('#loginForm');
const message = document.querySelector('#loginMessage');

form.addEventListener('submit', async (event) => {
    event.preventDefault();
    message.textContent = '';

    const payload = {
        username: form.username.value.trim(),
        password: form.password.value
    };

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const result = await readJsonResponse(response);

        if (response.ok && result.code === 200) {
            window.location.href = homePathForRole(result.data?.role);
            return;
        }

        message.textContent = result.message || '登录失败';
    } catch (error) {
        message.textContent = '网络异常，请稍后重试';
    }
});

function homePathForRole(role) {
    if (role === 'ADMIN') {
        return '/admin';
    }
    if (role === 'DOCTOR') {
        return '/doctor';
    }
    if (role === 'PATIENT') {
        return '/patient';
    }
    return '/index';
}

async function readJsonResponse(response) {
    const contentType = response.headers.get('content-type') || '';
    if (!contentType.includes('application/json')) {
        return { message: '服务器响应异常' };
    }
    return response.json();
}
