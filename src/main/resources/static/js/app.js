function redirectToLogin() {
    window.location.href = '/login';
}

function getPageMessageElement() {
    const contentPanel = document.querySelector('.content-panel');
    if (!contentPanel) {
        return null;
    }

    let messageElement = contentPanel.querySelector('#pageMessage');
    if (!messageElement) {
        messageElement = document.createElement('p');
        messageElement.id = 'pageMessage';
        messageElement.className = 'form-message';
        contentPanel.prepend(messageElement);
    }

    return messageElement;
}

function clearPageMessage() {
    const existingMessage = document.querySelector('#pageMessage');
    if (existingMessage) {
        existingMessage.remove();
    }
}

function clearPageError() {
    const existingMessage = document.querySelector('#pageMessage');
    if (existingMessage && !existingMessage.classList.contains('form-message-success')) {
        existingMessage.remove();
    }
}

function setPageMessage(message, type) {
    const messageElement = getPageMessageElement();
    if (!messageElement) {
        window.alert(message);
        return;
    }

    messageElement.className = type === 'success' ? 'form-message form-message-success' : 'form-message';
    messageElement.textContent = message;
}

function setPageError(message) {
    setPageMessage(message, 'error');
}

function setPageSuccess(message) {
    setPageMessage(message, 'success');
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

    clearPageError();
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
