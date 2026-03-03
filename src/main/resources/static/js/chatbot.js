(function () {
  var panel = document.querySelector('[data-chatbot-panel]');
  var toggleButton = document.querySelector('[data-chatbot-open]');

  if (!panel || !toggleButton) {
    return;
  }

  var closeButton = panel.querySelector('[data-chatbot-close]');

  function isOpen() {
    return !panel.classList.contains('d-none');
  }

  function setOpen(open) {
    panel.classList.toggle('d-none', !open);
    toggleButton.setAttribute('aria-expanded', String(open));
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
})();
