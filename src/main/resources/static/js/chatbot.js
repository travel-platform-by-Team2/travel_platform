(function () {
  var panel = document.querySelector('[data-chatbot-panel]');
  var toggleButton = document.querySelector('[data-chatbot-open]');

  if (!panel || !toggleButton) {
    return;
  }

  var closeButton = panel.querySelector('[data-chatbot-close]');
  var chatBody = panel.querySelector('.chatbot-body');
  var form = panel.querySelector('.chatbot-form');
  var input = form ? form.querySelector('input[name="message"]') : null;
  var sendButton = form ? form.querySelector('button') : null;
  var isSending = false;

  function isOpen() {
    return !panel.classList.contains('d-none');
  }

  function setOpen(open) {
    panel.classList.toggle('d-none', !open);
    toggleButton.setAttribute('aria-expanded', String(open));

    if (open && input) {
      input.focus();
    }
  }

  function scrollToBottom() {
    if (!chatBody) {
      return;
    }

    chatBody.scrollTop = chatBody.scrollHeight;
  }

  function appendMessage(role, text) {
    if (!chatBody || !text) {
      return;
    }

    var msg = document.createElement('div');
    msg.className = role === 'user' ? 'chat-msg user' : 'chat-msg bot';

    var bubble = document.createElement('span');
    bubble.className = 'bubble';
    bubble.textContent = text;

    msg.appendChild(bubble);
    chatBody.appendChild(msg);
    scrollToBottom();
  }

  function appendTypingIndicator() {
    if (!chatBody) {
      return null;
    }

    var msg = document.createElement('div');
    msg.className = 'chat-msg bot typing';

    var bubble = document.createElement('span');
    bubble.className = 'bubble';
    bubble.setAttribute('role', 'status');
    bubble.setAttribute('aria-label', '챗봇이 답변을 작성 중입니다.');

    var dots = document.createElement('span');
    dots.className = 'typing-dots';

    for (var i = 0; i < 3; i += 1) {
      dots.appendChild(document.createElement('span'));
    }

    bubble.appendChild(dots);
    msg.appendChild(bubble);
    chatBody.appendChild(msg);
    scrollToBottom();
    return msg;
  }

  function removeTypingIndicator(node) {
    if (node && node.parentNode) {
      node.parentNode.removeChild(node);
    }
  }

  function setSendingState(sending) {
    isSending = sending;

    if (input) {
      input.disabled = sending;
    }

    if (sendButton) {
      sendButton.disabled = sending;
    }
  }

  function extractAnswer(data) {
    if (!data || typeof data !== 'object') {
      return null;
    }

    if (typeof data.answer === 'string' && data.answer.trim()) {
      return data.answer.trim();
    }

    if (
      data.assistantMessage &&
      typeof data.assistantMessage.content === 'string' &&
      data.assistantMessage.content.trim()
    ) {
      return data.assistantMessage.content.trim();
    }

    return null;
  }

  function buildRequestBody(message) {
    return {
      message: message,
      context: {
        page: window.location.pathname
      }
    };
  }

  async function requestAnswer(message) {
    var resp = await fetch('/api/chatbot/messages', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(buildRequestBody(message))
    });

    if (!resp.ok) {
      throw new Error('CHATBOT_API_ERROR_' + resp.status);
    }

    return resp.json();
  }

  async function submitMessage() {
    if (!input || isSending) {
      return;
    }

    var message = input.value.trim();
    if (!message) {
      return;
    }

    appendMessage('user', message);
    input.value = '';
    setSendingState(true);
    var typingNode = appendTypingIndicator();

    try {
      var data = await requestAnswer(message);
      removeTypingIndicator(typingNode);
      typingNode = null;
      var answer = extractAnswer(data) || '응답 형식을 해석하지 못했어요. 잠시 후 다시 시도해주세요.';
      appendMessage('bot', answer);
    } catch (error) {
      removeTypingIndicator(typingNode);
      typingNode = null;
      appendMessage('bot', '일시적으로 답변을 가져오지 못했어요. 잠시 후 다시 시도해주세요.');
      console.error(error);
    } finally {
      removeTypingIndicator(typingNode);
      setSendingState(false);
      if (input && isOpen()) {
        input.focus();
      }
    }
  }

  toggleButton.addEventListener('click', function () {
    setOpen(!isOpen());
  });

  if (closeButton) {
    closeButton.addEventListener('click', function () {
      setOpen(false);
    });
  }

  document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape' && isOpen()) {
      setOpen(false);
    }
  });

  document.addEventListener('click', function (event) {
    if (!isOpen()) {
      return;
    }

    if (panel.contains(event.target) || toggleButton.contains(event.target)) {
      return;
    }

    setOpen(false);
  });

  if (form) {
    form.addEventListener('submit', function (event) {
      event.preventDefault();
      submitMessage();
    });
  }

  if (sendButton && sendButton.type === 'button') {
    sendButton.addEventListener('click', function () {
      submitMessage();
    });
  }

  if (input) {
    input.addEventListener('keydown', function (event) {
      if (event.key === 'Enter') {
        event.preventDefault();
        submitMessage();
      }
    });
  }
})();
