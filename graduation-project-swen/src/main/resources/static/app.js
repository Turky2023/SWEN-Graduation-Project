
function apiCall(url, method, body, isFormData) {
    var options = { method: method || 'GET', headers: {} };
    if (body && !isFormData) {
        options.headers['Content-Type'] = 'application/json';
        options.body = JSON.stringify(body);
    }
    if (isFormData) options.body = body;

    return fetch(url, options).then(function(response) {
        if (response.status === 401) {

            if (window.location.pathname === '/' || window.location.pathname === '/index.html') {
                return { error: 'Not authenticated' };
            }
            window.location.href = '/';
            throw new Error('Not authenticated');
        }
        var ct = response.headers.get('Content-Type') || '';
        return ct.indexOf('application/json') === -1 ? response : response.json();
    });
}

function showMsg(elementId, message, cssClass) {
    var el = document.getElementById(elementId);
    if (el) { el.textContent = message; el.className = cssClass || ''; }
}

function showError(id, msg) { showMsg(id, msg, 'error-msg'); }
function showSuccess(id, msg) { showMsg(id, msg, 'success-msg'); }
function clearMessage(id) { showMsg(id, '', ''); }

function formatDate(dateStr) {
    return dateStr ? new Date(dateStr).toLocaleString() : '';
}

function isDeadlinePassed(deadlineStr) {
    return new Date(deadlineStr) < new Date();
}

function getCurrentUser() {
    return apiCall('/api/auth/me', 'GET');
}

function redirectByRole(role) {
    if (role === 'STUDENT') window.location.href = '/student.html';
    else if (role === 'SUPERVISOR') window.location.href = '/supervisor.html';
}

function logout() {
    fetch('/api/auth/logout', { method: 'POST' }).finally(function() {
        window.location.href = '/';
    });
}

var chatPollers = {};

function escapeHtml(str) {
    if (str == null) return '';
    var div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

function appendMessage(chatBoxId, msg) {
    var box = document.getElementById(chatBoxId);
    if (!box) return;

    if (box.querySelector('[data-msg-id="' + msg.id + '"]')) return;
    var div = document.createElement('div');
    div.className = 'chat-message';
    div.setAttribute('data-msg-id', msg.id);
    var roleClass = msg.senderRole === 'SUPERVISOR' ? 'sender sender-supervisor' : 'sender';
    div.innerHTML = '<div><span class="' + roleClass + '">' + escapeHtml(msg.senderName) + '</span>' +
                    '<span class="time">' + formatDate(msg.sentAt) + '</span></div>' +
                    '<div class="content">' + escapeHtml(msg.content) + '</div>';
    box.appendChild(div);
    box.scrollTop = box.scrollHeight;
}

function loadChat(groupId, chatBoxId) {
    var poller = chatPollers[groupId];
    var url = '/api/messages/group/' + groupId;
    if (poller && poller.lastMessageId) url += '?after=' + poller.lastMessageId;

    return apiCall(url).then(function(messages) {
        if (!messages || messages.error) return;
        var box = document.getElementById(chatBoxId);
        if (!box) return;

        if (!poller || !poller.lastMessageId) {

            box.innerHTML = '';
            if (messages.length === 0) {
                box.innerHTML = '<p class="info-msg">No messages yet. Start the conversation.</p>';
                return;
            }
        }

        messages.forEach(function(m) {

            var info = box.querySelector('.info-msg');
            if (info) info.remove();
            appendMessage(chatBoxId, m);

            if (!chatPollers[groupId]) chatPollers[groupId] = {};
            if (chatPollers[groupId].lastMessageId == null || m.id > chatPollers[groupId].lastMessageId) {
                chatPollers[groupId].lastMessageId = m.id;
            }
        });
    }).catch(function() {

        stopChatPolling(groupId);
    });
}

function startChatPolling(groupId, chatBoxId) {
    stopChatPolling(groupId);
    chatPollers[groupId] = { lastMessageId: null };
    loadChat(groupId, chatBoxId);
    chatPollers[groupId].intervalId = setInterval(function() {
        loadChat(groupId, chatBoxId);
    }, 10000);
}

function stopChatPolling(groupId) {
    if (chatPollers[groupId] && chatPollers[groupId].intervalId) {
        clearInterval(chatPollers[groupId].intervalId);
    }
    delete chatPollers[groupId];
}

function sendChatMessage(groupId, inputId, chatBoxId, errorId) {
    clearMessage(errorId);
    var input = document.getElementById(inputId);
    var content = input.value.trim();
    if (!content) return;

    apiCall('/api/messages/group/' + groupId, 'POST', { content: content }).then(function(result) {
        if (result.error) {
            showError(errorId, result.error);
            return;
        }
        input.value = '';

        var info = document.getElementById(chatBoxId).querySelector('.info-msg');
        if (info) info.remove();
        appendMessage(chatBoxId, result);
    }).catch(function() {
        showError(errorId, 'Could not send message. Please try again.');
    });
}
