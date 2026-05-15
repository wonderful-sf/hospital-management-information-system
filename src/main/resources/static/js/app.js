function redirectToLogin() {
    window.location.href = '/login.html';
}

function setPageError(message) {
    const existingError = document.querySelector('#pageError');
    if (existingError) {
        existingError.textContent = message;
        return;
    }

    const contentPanel = document.querySelector('.content-panel');
    if (!contentPanel) {
        window.alert(message);
        return;
    }

    const errorMessage = document.createElement('p');
    errorMessage.id = 'pageError';
    errorMessage.className = 'form-message';
    errorMessage.textContent = message;
    contentPanel.prepend(errorMessage);
}

async function requestJson(url, options = {}) {
    let response;
    try {
        response = await fetch(url, options);
    } catch {
        setPageError('网络异常，请检查连接后重试。');
        return null;
    }

    if (response.status === 401) {
        redirectToLogin();
        return null;
    }

    let result;
    try {
        result = await response.json();
    } catch {
        setPageError('服务返回数据格式错误，请稍后重试。');
        return null;
    }

    if (!response.ok) {
        setPageError(result.message || '请求失败，请稍后重试。');
        return null;
    }

    return result;
}

async function loadCurrentUser() {
    const result = await requestJson('/api/auth/me');
    if (!result) {
        return null;
    }
    return result.data;
}

async function loadMenus() {
    const result = await requestJson('/api/auth/menus');
    if (!result) {
        return [];
    }
    return result.data || [];
}

function renderMenus(menus) {
    const menuList = document.querySelector('#menuList');
    menuList.innerHTML = '';
    for (const menu of menus) {
        const link = document.createElement('a');
        link.href = menu.href;
        link.textContent = menu.title;
        if (window.location.pathname === menu.href) {
            link.classList.add('active');
        }
        menuList.appendChild(link);
    }
}

async function logout() {
    const result = await requestJson('/api/auth/logout', { method: 'POST' });
    if (!result) {
        return;
    }
    redirectToLogin();
}

async function init() {
    const user = await loadCurrentUser();
    if (!user) {
        return;
    }
    document.querySelector('#currentUser').textContent = `${user.username}（${user.role}）`;
    renderMenus(await loadMenus());
    document.querySelector('#logoutButton').addEventListener('click', logout);
}

init();
