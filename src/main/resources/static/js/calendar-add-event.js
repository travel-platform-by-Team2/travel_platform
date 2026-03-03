(function () {
  "use strict";

  function initCalendarAddEventPanel() {
    var root = document.querySelector(".calendar-add-event-div-03");
    if (!root) return;

    var openButton = root.querySelector("[data-calendar-event-open]");
    var panel = root.querySelector("[data-calendar-event-panel]");
    var closeButtons = root.querySelectorAll("[data-calendar-event-close]");
    if (!openButton || !panel) return;

    var fields = panel.querySelectorAll("input, textarea, select");
    var initialState = new Map();
    fields.forEach(function (field) {
      if (field.type === "checkbox" || field.type === "radio") {
        initialState.set(field, field.checked);
      } else {
        initialState.set(field, field.value);
      }
    });

    function resetPanelFields() {
      initialState.forEach(function (value, field) {
        if (!field.isConnected) return;
        if (field.type === "checkbox" || field.type === "radio") {
          field.checked = Boolean(value);
        } else {
          field.value = value;
        }
      });
    }

    function openPanel() {
      panel.hidden = false;
      openButton.setAttribute("aria-expanded", "true");
      var firstField = panel.querySelector("input, textarea, select");
      if (firstField) firstField.focus();
    }

    function closePanel(options) {
      var shouldReset = !options || options.reset !== false;
      if (shouldReset) resetPanelFields();
      panel.hidden = true;
      openButton.setAttribute("aria-expanded", "false");
    }

    openButton.addEventListener("click", function (event) {
      event.preventDefault();
      openPanel();
    });

    closeButtons.forEach(function (button) {
      button.addEventListener("click", function (event) {
        event.preventDefault();
        closePanel({ reset: true });
      });
    });

    document.addEventListener("keydown", function (event) {
      if (event.key === "Escape" && !panel.hidden) {
        closePanel({ reset: true });
      }
    });
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initCalendarAddEventPanel);
  } else {
    initCalendarAddEventPanel();
  }
})();
