(function () {
  "use strict";

  var selectedDate = "";
  var cachedEvents = [];
  var memoStore = [];
  var currentMonthDate = new Date();

  function initCalendarAddEventPanel() {
    var root = document.querySelector(".calendar-add-event-div-03");
    if (!root) return;

    var openButton = root.querySelector("[data-calendar-event-open]");
    var panel = root.querySelector("[data-calendar-event-panel]");
    var closeButtons = root.querySelectorAll("[data-calendar-event-close]");
    var saveButton = root.querySelector("[data-calendar-event-save]");
    var deleteButton = document.querySelector("[data-calendar-event-delete]");
    var deleteAction = document.querySelector("[data-calendar-event-action]");
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

    function syncDeleteAction() {
      if (!deleteAction) return;
      deleteAction.hidden = !currentEditId;
    }

    openButton.addEventListener("click", function (event) {
      event.preventDefault();
      openPanel();
    });

    closeButtons.forEach(function (button) {
      button.addEventListener("click", function (event) {
        event.preventDefault();
        closePanel({ reset: true });
        clearEditMode();
      });
    });

    function normalizeDateInput(value) {
      if (!value) return "";
      return value.replace(/\s+/g, "").replace(/\./g, "-").replace(/\/+/g, "-");
    }

    function buildDateTime(dateValue, timeValue) {
      var dateText = normalizeDateInput(dateValue);
      if (!dateText) return "";
      var timeText = (timeValue || "00:00").trim();
      if (timeText.length === 4) timeText = "0" + timeText;
      return dateText + "T" + timeText + ":00";
    }

    function parseYearMonthFromDate(value) {
      var normalized = normalizeDateInput(value);
      var parts = normalized.split("-");
      if (parts.length < 2) return null;
      var year = parseInt(parts[0], 10);
      var month = parseInt(parts[1], 10);
      if (!year || !month) return null;
      return { year: year, month: month };
    }

    function getMonthRangeFromDate(baseDate) {
      if (!baseDate) return null;
      var year = baseDate.getFullYear();
      var month = baseDate.getMonth() + 1;
      var start = year + "-" + String(month).padStart(2, "0") + "-01";
      var lastDay = new Date(year, month, 0).getDate();
      var end = year + "-" + String(month).padStart(2, "0") + "-" + String(lastDay).padStart(2, "0");
      return { startDate: start, endDate: end };
    }

    var currentEditId = null;
    currentMonthDate = new Date();
    selectedDate = ymdFromDate(currentMonthDate);

    function renderEventList(events) {
      var listRoot = document.querySelector("[data-calendar-event-list]");
      if (!listRoot) return;
      listRoot.innerHTML = "";
      if (!events || !events.length) {
        var empty = document.createElement("p");
        empty.className = "text-body-sm-relaxed";
        empty.textContent = "등록된 일정이 없습니다.";
        listRoot.appendChild(empty);
        return;
      }
      events.forEach(function (event) {
        var item = document.createElement("div");
        item.className = "info-card-blue";
        item.setAttribute("data-calendar-event-id", event.id);
        var title = document.createElement("span");
        title.className = "calendar-add-event-span-04";
        title.textContent = event.title || "일정";
        var meta = document.createElement("p");
        meta.className = "text-body-sm-relaxed";
        meta.textContent = formatRange(event.startAt, event.endAt);
        item.appendChild(title);
        item.appendChild(meta);
        item.addEventListener("click", function () {
          applyEventToForm(event);
        });
        listRoot.appendChild(item);
      });
    }

    function formatRange(startAt, endAt) {
      if (!startAt || !endAt) return "";
      return startAt.replace("T", " ").slice(0, 16) + " ~ " + endAt.replace("T", " ").slice(0, 16);
    }

    function clearGridChips() {
      var grid = document.querySelector("[data-calendar-grid]");
      if (!grid) return;
      var chips = grid.querySelectorAll("[data-calendar-chip]");
      chips.forEach(function (chip) {
        chip.remove();
      });
    }

    function renderEventsOnGrid(events) {
      var grid = document.querySelector("[data-calendar-grid]");
      if (!grid) return;
      clearGridChips();
      if (!events || !events.length) return;

      events.forEach(function (event) {
        if (!event.startAt) return;
        var startDateText = event.startAt.split("T")[0];
        var endDateText = event.endAt ? event.endAt.split("T")[0] : startDateText;
        var dateCursor = dateFromYmd(startDateText);
        var endDate = dateFromYmd(endDateText);
        if (!dateCursor || !endDate) return;
        var categoryKey = getCategoryKey(event.eventType);
        var singleDay = startDateText === endDateText;
        while (dateCursor.getTime() <= endDate.getTime()) {
          var dateKey = ymdFromDate(dateCursor);
          var dayNode = findDayCardByDate(dateKey);
          if (dayNode) {
            var chip = document.createElement("div");
            chip.setAttribute("data-calendar-chip", "true");
            if (singleDay) {
              chip.className = "event-chip event-chip--" + categoryKey;
              chip.textContent = event.title || "일정";
            } else {
              var isStart = dateKey === startDateText;
              var isEnd = dateKey === endDateText;
              var position = isStart ? "start" : isEnd ? "end" : "mid";
              chip.className = "event-range event-range-" + position + " event-range--" + categoryKey;
              chip.textContent = isStart ? (event.title || "일정") : "";
            }
            dayNode.appendChild(chip);
          }
          dateCursor.setDate(dateCursor.getDate() + 1);
        }
      });
    }

    function getCategoryKey(eventType) {
      switch ((eventType || "").toUpperCase()) {
        case "PERSONAL":
          return "personal";
        case "FAMILY":
          return "family";
        case "WORK":
          return "work";
        case "TRIP":
        default:
          return "trip";
      }
    }

    function dateFromYmd(value) {
      if (!value) return null;
      var parts = value.split("-");
      if (parts.length !== 3) return null;
      var year = parseInt(parts[0], 10);
      var month = parseInt(parts[1], 10);
      var day = parseInt(parts[2], 10);
      if (!year || !month || !day) return null;
      return new Date(year, month - 1, day);
    }

    function ymdFromDate(date) {
      return date.getFullYear()
        + "-" + String(date.getMonth() + 1).padStart(2, "0")
        + "-" + String(date.getDate()).padStart(2, "0");
    }

    function formatDateInput(value) {
      if (!value) return "";
      var datePart = value.split("T")[0];
      var parts = datePart.split("-");
      if (parts.length !== 3) return datePart;
      return parts[0] + ". " + parts[1] + ". " + parts[2];
    }

    function formatTimeInput(value) {
      if (!value) return "";
      var timePart = value.split("T")[1] || "";
      return timePart.slice(0, 5);
    }

    function setEditMode(eventId) {
      currentEditId = eventId;
      if (saveButton) {
        saveButton.textContent = "수정하기";
      }
      syncDeleteAction();
    }

    function clearEditMode() {
      currentEditId = null;
      if (saveButton) {
        saveButton.textContent = "저장하기";
      }
      syncDeleteAction();
    }

    function applyEventToForm(event) {
      if (!event || !panel) return;
      var titleInput = panel.querySelector("[data-calendar-title]");
      var startDateInput = panel.querySelector("[data-calendar-start-date]");
      var startTimeInput = panel.querySelector("[data-calendar-start-time]");
      var endDateInput = panel.querySelector("[data-calendar-end-date]");
      var endTimeInput = panel.querySelector("[data-calendar-end-time]");

      if (titleInput) titleInput.value = event.title || "";
      if (startDateInput) startDateInput.value = formatDateInput(event.startAt);
      if (startTimeInput) startTimeInput.value = formatTimeInput(event.startAt);
      if (endDateInput) endDateInput.value = formatDateInput(event.endAt);
      if (endTimeInput) endTimeInput.value = formatTimeInput(event.endAt);

      if (event && event.eventType) {
        eventType = event.eventType;
        categoryButtons.forEach(function (btn) {
          var match = btn.getAttribute("data-calendar-category") === eventType;
          btn.classList.toggle("is-active", match);
        });
      }

      openPanel();
      setEditMode(event.id);
    }

    function findDayCardByDate(dateText) {
      if (!dateText) return null;
      return document.querySelector("[data-calendar-date=\"" + dateText + "\"]");
    }

    function setMonthTitle(date) {
      var title = document.querySelector("[data-calendar-month-title]");
      if (!title) return;
      title.textContent = date.getFullYear() + "년 " + (date.getMonth() + 1) + "월";
    }

    function buildCalendarGrid(date) {
      var grid = document.querySelector("[data-calendar-grid]");
      if (!grid) return;
      grid.innerHTML = "";

      var year = date.getFullYear();
      var month = date.getMonth();
      var firstDay = new Date(year, month, 1);
      var startOffset = firstDay.getDay();
      var startDate = new Date(year, month, 1 - startOffset);
      var today = new Date();

      for (var i = 0; i < 42; i++) {
        var cellDate = new Date(startDate);
        cellDate.setDate(startDate.getDate() + i);
        var isCurrentMonth = cellDate.getMonth() === month;
        var isToday = cellDate.toDateString() === today.toDateString();

        var card = document.createElement("div");
        card.className = isCurrentMonth ? "calendar-day-card" : "calendar-day-card-soft-muted";
        if (isToday && isCurrentMonth) {
          card.className = "calendar-day-active";
        }
        var dateText = cellDate.getFullYear() + "-" + String(cellDate.getMonth() + 1).padStart(2, "0") + "-" + String(cellDate.getDate()).padStart(2, "0");
        card.setAttribute("data-calendar-date", dateText);
        if (selectedDate && selectedDate === dateText) {
          card.classList.add("calendar-day-selected");
        }

        var span = document.createElement("span");
        span.className = isToday && isCurrentMonth ? "date-badge-primary" : isCurrentMonth ? "calendar-day-number" : "text-muted-sm-medium";
        span.textContent = String(cellDate.getDate());
        card.appendChild(span);
        grid.appendChild(card);
      }
    }

    function syncDefaultFormDate(date) {
      var dateText = date.getFullYear() + ". " + String(date.getMonth() + 1).padStart(2, "0") + ". " + String(date.getDate()).padStart(2, "0");
      var startDateInput = panel.querySelector("[data-calendar-start-date]");
      var endDateInput = panel.querySelector("[data-calendar-end-date]");
      if (startDateInput) startDateInput.value = dateText;
      if (endDateInput) endDateInput.value = dateText;
    }

    function fetchEventList() {
      var range = getMonthRangeFromDate(currentMonthDate);
      if (!range) return Promise.resolve([]);
      var url = "/api/calendar?startDate=" + range.startDate + "&endDate=" + range.endDate;
      return fetch(url)
        .then(function (response) {
          if (!response.ok) throw new Error("Failed to load calendar events.");
          return response.json();
        })
        .then(function (data) {
          cachedEvents = Array.isArray(data) ? data : data.events || [];
          renderEventsOnGrid(cachedEvents);
          renderSelectedDate(renderEventList);
          return cachedEvents;
        })
        .catch(function () {
          return [];
        });
    }

    function handleSave() {
      var titleInput = panel.querySelector("[data-calendar-title]");
      var startDateInput = panel.querySelector("[data-calendar-start-date]");
      var startTimeInput = panel.querySelector("[data-calendar-start-time]");
      var endDateInput = panel.querySelector("[data-calendar-end-date]");
      var endTimeInput = panel.querySelector("[data-calendar-end-time]");
      var memoInput = panel.querySelector("[data-calendar-memo]");

      var payload = {
        tripPlanId: null,
        title: titleInput ? titleInput.value.trim() : "",
        startAt: buildDateTime(startDateInput ? startDateInput.value : "", startTimeInput ? startTimeInput.value : ""),
        endAt: buildDateTime(endDateInput ? endDateInput.value : "", endTimeInput ? endTimeInput.value : ""),
        eventType: eventType
      };
      var memoText = memoInput ? memoInput.value.trim() : "";

      if (!payload.title || !payload.startAt || !payload.endAt) {
        alert("일정 제목과 일시를 입력해주세요.");
        return;
      }

      var url = currentEditId ? "/api/calendar/update/" + currentEditId : "/api/calendar/create";
      var method = currentEditId ? "PUT" : "POST";

      fetch(url, {
        method: method,
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
      })
        .then(function (response) {
          if (!response.ok) {
            throw new Error("Failed to save event.");
          }
        })
        .then(function () {
          var savedDate = dateFromYmd(payload.startAt.split("T")[0]);
          if (savedDate) {
            currentMonthDate = savedDate;
            setMonthTitle(currentMonthDate);
            buildCalendarGrid(currentMonthDate);
            syncDefaultFormDate(currentMonthDate);
          }
          closePanel({ reset: true });
          clearEditMode();
          if (memoText) {
            storeMemo(payload.title, memoText, payload.startAt.split("T")[0]);
          }
          return fetchEventList().catch(function () {
            alert("일정은 저장됐지만 목록 갱신에 실패했습니다.");
          });
        })
        .catch(function () {
          alert(currentEditId ? "일정 수정에 실패했습니다." : "일정 저장에 실패했습니다.");
        });
    }

    function handleDelete() {
      if (!currentEditId) {
        alert("삭제할 일정이 없습니다.");
        return;
      }

      fetch("/api/calendar/delete/" + currentEditId, {
        method: "POST"
      })
        .then(function (response) {
          if (!response.ok) {
            throw new Error("Failed to delete event.");
          }
        })
        .then(function () {
          closePanel({ reset: true });
          clearEditMode();
          return fetchEventList();
        })
        .catch(function () {
          alert("일정 삭제에 실패했습니다.");
        });
    }

    var eventType = "TRIP";
    var categoryButtons = panel.querySelectorAll("[data-calendar-category]");
    categoryButtons.forEach(function (button, index) {
      if (index === 0) button.classList.add("is-active");
      button.addEventListener("click", function () {
        eventType = button.getAttribute("data-calendar-category") || "TRIP";
        categoryButtons.forEach(function (btn) {
          btn.classList.remove("is-active");
        });
        button.classList.add("is-active");
      });
    });

    if (saveButton) {
      saveButton.addEventListener("click", function (event) {
        event.preventDefault();
        handleSave();
      });
    }

    if (deleteButton) {
      deleteButton.addEventListener("click", function (event) {
        event.preventDefault();
        handleDelete();
      });
    }

    document.addEventListener("keydown", function (event) {
      if (event.key === "Escape" && !panel.hidden) {
        closePanel({ reset: true });
        clearEditMode();
      }
    });

    setMonthTitle(currentMonthDate);
    buildCalendarGrid(currentMonthDate);
    syncDefaultFormDate(currentMonthDate);
    syncDeleteAction();
    bindDaySelection(renderEventList);
    fetchEventList();
  }

  function storeMemo(title, memo, dateKey) {
    memoStore.push({
      id: Date.now(),
      title: title || "메모",
      memo: memo,
      dateKey: dateKey
    });
    if (dateKey === selectedDate) {
      renderMemoList();
    }
  }

  function appendMemoCard(container, item) {
    var card = document.createElement("div");
    card.className = "info-card-amber";
    var header = document.createElement("div");
    header.className = "row-between-start-mb2";
    var titleSpan = document.createElement("span");
    titleSpan.className = "calendar-add-event-span-03";
    titleSpan.textContent = item.title || "메모";
    var closeBtn = document.createElement("button");
    closeBtn.className = "action-fade-danger";
    closeBtn.innerHTML = "<span class=\"icon-ms-sm\">close</span>";
    closeBtn.addEventListener("click", function () {
      memoStore = memoStore.filter(function (entry) {
        return entry.id !== item.id;
      });
      card.remove();
    });
    header.appendChild(titleSpan);
    header.appendChild(closeBtn);
    var body = document.createElement("p");
    body.className = "text-body-sm-relaxed";
    body.textContent = item.memo;
    card.appendChild(header);
    card.appendChild(body);
    container.appendChild(card);
  }

  function renderMemoList() {
    var listRoot = document.querySelector("[data-calendar-memo-list]");
    if (!listRoot) return;
    listRoot.innerHTML = "";
    var filtered = memoStore.filter(function (item) {
      return item.dateKey === selectedDate;
    });
    if (!filtered.length) {
      var empty = document.createElement("p");
      empty.className = "text-body-sm-relaxed";
      empty.textContent = "등록된 메모가 없습니다.";
      listRoot.appendChild(empty);
      return;
    }
    filtered.forEach(function (item) {
      appendMemoCard(listRoot, item);
    });
  }

  function renderSelectedDate(renderEventList) {
    var heading = document.querySelector("[data-calendar-selected-date]");
    if (heading) {
      heading.textContent = selectedDate;
    }
    var filtered = cachedEvents.filter(function (event) {
      if (!event.startAt) return false;
      var startKey = event.startAt.split("T")[0];
      var endKey = event.endAt ? event.endAt.split("T")[0] : startKey;
      return selectedDate >= startKey && selectedDate <= endKey;
    });
    renderEventList(filtered);
    renderMemoList();
  }

  function bindDaySelection(renderEventList) {
    var grid = document.querySelector("[data-calendar-grid]");
    if (!grid) return;
    grid.addEventListener("click", function (event) {
      var cell = event.target.closest("[data-calendar-date]");
      if (!cell) return;
      var previous = grid.querySelector(".calendar-day-selected");
      if (previous) previous.classList.remove("calendar-day-selected");
      cell.classList.add("calendar-day-selected");
      selectedDate = cell.getAttribute("data-calendar-date");
      renderSelectedDate(renderEventList);
    });
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initCalendarAddEventPanel);
  } else {
    initCalendarAddEventPanel();
  }
})();
