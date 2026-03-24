(function () {
  "use strict";

  var CALENDAR_MEMO_MAX_LENGTH = 500;
  var MAX_VISIBLE_DAY_CHIPS = 2;

  // 사용자가 선택한 날짜 
  var selectedDate = "";

  // 서버에서 가져온 현재 월 일정 캐시
  var cachedEvents = [];
  // Current-page session memory for monthly lane placement.
  var monthLayoutCache = {};
  
  // 현재 보고 있는 달
  var currentMonthDate = new Date();

  // 캘린더 진입점 함수
  // 버튼 이벤트 연결, 입력창 이벤트 연결, 월 이동 이벤트 연결, 저장 / 삭제 이벤트 연결
  function initCalendarAddEventPanel() {
    var root = document.querySelector(".calendar-add-event-div-03");
    if (!root) return;

    var openButton = root.querySelector("[data-calendar-event-open]");
    // 일정 입력 패널
    var panel = root.querySelector("[data-calendar-event-panel]");
    var closeButtons = root.querySelectorAll("[data-calendar-event-close]");
    var saveButton = root.querySelector("[data-calendar-event-save]");
    var deleteButton = panel.querySelector("[data-calendar-event-delete]");
    var deleteAction = panel.querySelector("[data-calendar-event-action]");
    var startDateWrap = panel.querySelector("[data-calendar-start-date-wrap]");
    var endDateWrap = panel.querySelector("[data-calendar-end-date-wrap]");
    var startDateField = panel.querySelector("[data-calendar-start-date]");
    var endDateField = panel.querySelector("[data-calendar-end-date]");
    var startTimeWrap = panel.querySelector("[data-calendar-start-time-wrap]");
    var endTimeWrap = panel.querySelector("[data-calendar-end-time-wrap]");
    var startTimeField = panel.querySelector("[data-calendar-start-time]");
    var endTimeField = panel.querySelector("[data-calendar-end-time]");
    var memoField = panel.querySelector("[data-calendar-memo]");
    var memoGuide = panel.querySelector("[data-calendar-memo-guide]");
    var prevMonthButton = document.querySelector("[data-calendar-prev-month]");
    var nextMonthButton = document.querySelector("[data-calendar-next-month]");
    if (!openButton || !panel) return;

    var fields = panel.querySelectorAll("input, textarea, select");
    var initialState = new Map();
    var hasShownMemoLimitAlert = false;
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
      updateMemoGuide();
      hasShownMemoLimitAlert = false;
    }

    function updateMemoGuide() {
      if (!memoGuide || !memoField) return;
      memoGuide.textContent = memoField.value.length + " / " + CALENDAR_MEMO_MAX_LENGTH;
    }

    function handleMemoInput() {
      if (!memoField) return;
      updateMemoGuide();
      if (memoField.value.length >= CALENDAR_MEMO_MAX_LENGTH) {
        if (!hasShownMemoLimitAlert) {
          alert("메모는 공백 포함 500자까지 입력할 수 있습니다.");
          hasShownMemoLimitAlert = true;
        }
        return;
      }
      hasShownMemoLimitAlert = false;
    }

    // 패널 열기/닫기 함수
    function getDefaultFormDate() {
      return dateFromYmd(selectedDate) || currentMonthDate || new Date();
    }

    function openPanel() {
      if (!currentEditId) {
        syncDefaultFormDate(getDefaultFormDate());
      }
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

    // 날짜/시간 문자열 처리 함수
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

    // 날짜/시간 picker 열기 함수
    function bindDatePickerOpen(wrapper, input) {
      if (!wrapper || !input) return;
      function openDatePicker(event) {
        if (event) {
          event.preventDefault();
        }
        if (typeof input.showPicker === "function") {
          input.showPicker();
          return;
        }
        input.focus();
      }

      wrapper.addEventListener("pointerdown", openDatePicker);
      input.addEventListener("pointerdown", openDatePicker);
    }

    function bindTimePickerOpen(wrapper, input) {
      if (!wrapper || !input) return;
      function openTimePicker(event) {
        if (event) {
          event.preventDefault();
        }
        if (typeof input.showPicker === "function") {
          input.showPicker();
          return;
        }
        input.focus();
      }

      wrapper.addEventListener("pointerdown", openTimePicker);
      input.addEventListener("pointerdown", openTimePicker);
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

    // 현재 보고 있는 달의 시작일과 마지막일을 계산하는 함수
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

    // 선택한 날짜에 해당하는 일정 목록을 오른쪽 패널에 그리는 함수
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

    // 오른쪽 패널 일정 시작/종료 시간 문자열열
    function formatRange(startAt, endAt) {
      if (!startAt || !endAt) return "";
      return startAt.replace("T", " ").slice(0, 16) + " ~ " + endAt.replace("T", " ").slice(0, 16);
    }

    // 달력 일정 표시를 새로 그리기 전에 화면을 초기화하는 함수
    function clearGridChips() {
      var grid = document.querySelector("[data-calendar-grid]");
      if (!grid) return;
      var chips = grid.querySelectorAll("[data-calendar-chip], [data-calendar-chip-overflow]");
      chips.forEach(function (chip) {
        chip.remove();
      });
    }

    function getMonthKey(date) {
      if (!date) return "";
      return date.getFullYear() + "-" + String(date.getMonth() + 1).padStart(2, "0");
    }

    function getEventStartDateKey(event) {
      if (!event || !event.startAt) return "";
      return event.startAt.split("T")[0];
    }

    function getEventEndDateKey(event) {
      if (!event || !event.startAt) return "";
      return event.endAt ? event.endAt.split("T")[0] : getEventStartDateKey(event);
    }

    function getEventStartTimeValue(event) {
      if (!event || !event.startAt || event.startAt.indexOf("T") === -1) return 0;
      var timeText = event.startAt.split("T")[1] || "";
      var parts = timeText.split(":");
      var hour = parseInt(parts[0], 10);
      var minute = parseInt(parts[1], 10);
      if (Number.isNaN(hour)) hour = 0;
      if (Number.isNaN(minute)) minute = 0;
      return hour * 60 + minute;
    }

    function getDateRangeKeys(startDateText, endDateText) {
      var startDate = dateFromYmd(startDateText);
      var endDate = dateFromYmd(endDateText || startDateText);
      if (!startDate || !endDate) return [];

      // Expand a multi-day event into each occupied calendar date.
      var dateRangeKeys = [];
      var cursor = new Date(startDate);
      while (cursor.getTime() <= endDate.getTime()) {
        dateRangeKeys.push(ymdFromDate(cursor));
        cursor.setDate(cursor.getDate() + 1);
      }
      return dateRangeKeys;
    }

    function canUseLaneForRange(dateRangeKeys, laneIndex, occupiedByDate) {
      for (var i = 0; i < dateRangeKeys.length; i += 1) {
        var dateKey = dateRangeKeys[i];
        var usedLaneMap = occupiedByDate[dateKey];
        if (usedLaneMap && usedLaneMap[laneIndex]) {
          return false;
        }
      }
      return true;
    }

    function reserveLaneForRange(dateRangeKeys, laneIndex, occupiedByDate) {
      dateRangeKeys.forEach(function (dateKey) {
        if (!occupiedByDate[dateKey]) {
          occupiedByDate[dateKey] = {};
        }
        occupiedByDate[dateKey][laneIndex] = true;
      });
    }

    function findAvailableLaneForRange(dateRangeKeys, occupiedByDate) {
      var laneIndex = 0;
      while (!canUseLaneForRange(dateRangeKeys, laneIndex, occupiedByDate)) {
        laneIndex += 1;
      }
      return laneIndex;
    }

    function compareEventsForGridLayout(a, b, previousLaneByEventId) {
      var aStartDateKey = getEventStartDateKey(a);
      var bStartDateKey = getEventStartDateKey(b);
      if (aStartDateKey !== bStartDateKey) {
        return aStartDateKey < bStartDateKey ? -1 : 1;
      }

      var aStartTime = getEventStartTimeValue(a);
      var bStartTime = getEventStartTimeValue(b);
      if (aStartTime !== bStartTime) {
        return aStartTime - bStartTime;
      }

      var aIsMultiDay = getEventStartDateKey(a) !== getEventEndDateKey(a);
      var bIsMultiDay = getEventStartDateKey(b) !== getEventEndDateKey(b);
      if (aIsMultiDay !== bIsMultiDay) {
        return aIsMultiDay ? -1 : 1;
      }

      var aHasPreviousLane = Object.prototype.hasOwnProperty.call(previousLaneByEventId, a.id);
      var bHasPreviousLane = Object.prototype.hasOwnProperty.call(previousLaneByEventId, b.id);
      if (aHasPreviousLane !== bHasPreviousLane) {
        return aHasPreviousLane ? -1 : 1;
      }

      return (a.id || 0) - (b.id || 0);
    }

    function createEventChip(event, dateKey) {
      var chip = document.createElement("div");
      var categoryKey = getCategoryKey(event.eventType);
      var startDateText = getEventStartDateKey(event);
      var endDateText = getEventEndDateKey(event);
      var singleDay = startDateText === endDateText;

      chip.setAttribute("data-calendar-chip", "true");
      if (singleDay) {
        chip.className = "event-chip event-chip--" + categoryKey;
        chip.textContent = event.title || "일정";
        return chip;
      }

      var isStart = dateKey === startDateText;
      var isEnd = dateKey === endDateText;
      var position = isStart ? "start" : isEnd ? "end" : "mid";
      chip.className = "event-range event-range-" + position + " event-range--" + categoryKey;
      chip.textContent = isStart ? (event.title || "일정") : "";
      return chip;
    }

    function computeStableMonthLayout(events) {
      var monthKey = getMonthKey(currentMonthDate);
      var previousState = monthLayoutCache[monthKey] || { laneByEventId: {} };
      var previousLaneByEventId = previousState.laneByEventId || {};
      var sortedEvents = (events || []).filter(function (event) {
        return event && event.startAt;
      }).slice().sort(function (a, b) {
        return compareEventsForGridLayout(a, b, previousLaneByEventId);
      });

      var assignedItems = [];
      var laneByEventId = {};
      var occupiedByDate = {};
      var dateBuckets = {};

      sortedEvents.forEach(function (event) {
        var startDateText = getEventStartDateKey(event);
        var endDateText = getEventEndDateKey(event);
        var dateRangeKeys = getDateRangeKeys(startDateText, endDateText);
        if (!dateRangeKeys.length) return;

        var preferredLane = Object.prototype.hasOwnProperty.call(previousLaneByEventId, event.id)
          ? previousLaneByEventId[event.id]
          : null;
        var laneIndex = preferredLane !== null && canUseLaneForRange(dateRangeKeys, preferredLane, occupiedByDate)
          ? preferredLane
          : findAvailableLaneForRange(dateRangeKeys, occupiedByDate);

        reserveLaneForRange(dateRangeKeys, laneIndex, occupiedByDate);
        assignedItems.push({
          event: event,
          dateRangeKeys: dateRangeKeys,
          laneIndex: laneIndex
        });
      });

      // Re-pack downward after deletes so surviving events can reclaim empty upper lanes.
      laneByEventId = {};
      occupiedByDate = {};
      dateBuckets = {};

      assignedItems.forEach(function (item) {
        var compactedLaneIndex = findAvailableLaneForRange(item.dateRangeKeys, occupiedByDate);
        laneByEventId[item.event.id] = compactedLaneIndex;
        reserveLaneForRange(item.dateRangeKeys, compactedLaneIndex, occupiedByDate);

        item.dateRangeKeys.forEach(function (dateKey) {
          if (!dateBuckets[dateKey]) {
            dateBuckets[dateKey] = [];
          }
          dateBuckets[dateKey].push({
            laneIndex: compactedLaneIndex,
            event: item.event
          });
        });
      });

      Object.keys(dateBuckets).forEach(function (dateKey) {
        dateBuckets[dateKey].sort(function (a, b) {
          return a.laneIndex - b.laneIndex;
        });
      });

      monthLayoutCache[monthKey] = {
        laneByEventId: laneByEventId
      };

      return dateBuckets;
    }

    // 달력 칸 안에 일정 표시를 그리는 핵심 함수
    function renderEventsOnGridLegacy(events) {
      var grid = document.querySelector("[data-calendar-grid]");
      if (!grid) return;
      clearGridChips();
      if (!events || !events.length) return;
      var chipCountByDate = {};
      var overflowByDate = {};

      events.forEach(function (event) {
        if (!event.startAt) return;
        var startDateText = event.startAt.split("T")[0];
        var endDateText = event.endAt ? event.endAt.split("T")[0] : startDateText;
        var dateCursor = dateFromYmd(startDateText);
        var endDate = dateFromYmd(endDateText);
        // 날짜 변환에 실패 시 해당 일정 건너뛰기
        if (!dateCursor || !endDate) return;
        var categoryKey = getCategoryKey(event.eventType);
        var singleDay = startDateText === endDateText;
        while (dateCursor.getTime() <= endDate.getTime()) {
          // 현재 반복 중인 날짜를 YYYY-MM-DD 문자열로 바꾸는 함수
          var dateKey = ymdFromDate(dateCursor);
          var dayNode = findDayCardByDate(dateKey);

          // 해당 날짜 있으면 칩 추가
          if (dayNode) {
            var currentChipCount = chipCountByDate[dateKey] || 0;
            if (currentChipCount >= MAX_VISIBLE_DAY_CHIPS) {
              overflowByDate[dateKey] = true;
              dateCursor.setDate(dateCursor.getDate() + 1);
              continue;
            }

            var chip = document.createElement("div");
            chip.setAttribute("data-calendar-chip", "true");
            // 하루짜리 일정, 여러 날 일정 분기기
            if (singleDay) {
              chip.className = "event-chip event-chip--" + categoryKey;
              chip.textContent = event.title || "일정";
            } else {
              // 현재 날짜가 시작일인지 종료일인지 확인
              var isStart = dateKey === startDateText;
              var isEnd = dateKey === endDateText;
              // 시작일이면 start, 종료일이면 end, 그 사이 날짜면 mid
              var position = isStart ? "start" : isEnd ? "end" : "mid";
              // 연속 일정 스타일일
              chip.className = "event-range event-range-" + position + " event-range--" + categoryKey;
              // 시작일 칸에만 제목 표시 나머지 일자는 바 형태태
              chip.textContent = isStart ? (event.title || "일정") : "";
            }
            // 만든 칩을 해당 날짜 칸에 붙임임
            dayNode.appendChild(chip);
            chipCountByDate[dateKey] = currentChipCount + 1;
          }
          // 다음 칸에도 일정 표시 이어 그리기 위한 코드
          dateCursor.setDate(dateCursor.getDate() + 1);
        }
      });

      Object.keys(overflowByDate).forEach(function (dateKey) {
        var dayNode = findDayCardByDate(dateKey);
        if (!dayNode) return;

        var overflowChip = document.createElement("div");
        overflowChip.setAttribute("data-calendar-chip-overflow", "true");
        overflowChip.className = "event-chip";
        overflowChip.textContent = "...";
        dayNode.appendChild(overflowChip);
      });
    }

    function renderEventsOnGrid(events) {
      var grid = document.querySelector("[data-calendar-grid]");
      if (!grid) return;
      clearGridChips();
      if (!events || !events.length) return;

      var dateBuckets = computeStableMonthLayout(events);
      Object.keys(dateBuckets).forEach(function (dateKey) {
        var dayNode = findDayCardByDate(dateKey);
        if (!dayNode) return;

        var laneMap = {};
        dateBuckets[dateKey].forEach(function (layoutItem) {
          laneMap[layoutItem.laneIndex] = layoutItem;
        });

        var renderedCount = 0;
        for (var laneIndex = 0; laneIndex < MAX_VISIBLE_DAY_CHIPS; laneIndex += 1) {
          var layoutItem = laneMap[laneIndex];
          if (layoutItem) {
            dayNode.appendChild(createEventChip(layoutItem.event, dateKey));
            renderedCount += 1;
            continue;
          }

          // Keep empty upper rows visible so a lower-lane multi-day event does not jump upward mid-span.
          var spacer = document.createElement("div");
          spacer.className = "event-chip";
          spacer.setAttribute("data-calendar-chip", "true");
          spacer.setAttribute("aria-hidden", "true");
          spacer.style.visibility = "hidden";
          spacer.style.pointerEvents = "none";
          spacer.textContent = ".";
          dayNode.appendChild(spacer);
        }

        var overflowCount = dateBuckets[dateKey].length - renderedCount;
        if (overflowCount > 0) {
          var overflowChip = document.createElement("div");
          overflowChip.setAttribute("data-calendar-chip-overflow", "true");
          overflowChip.className = "event-chip";
          overflowChip.textContent = "...";
          dayNode.appendChild(overflowChip);
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

    // YYYY-MM-DD 문자열을 Date 객체로 바꾸는 함수
    function ymdFromDate(date) {
      return date.getFullYear()
        + "-" + String(date.getMonth() + 1).padStart(2, "0")
        + "-" + String(date.getDate()).padStart(2, "0");
    }

    // 날짜 부분만 잘라 input value에 넣는 함수수
    function formatDateInput(value) {
      if (!value) return "";
      return value.split("T")[0];
    }

    // 시간만 잘라 input value에 넣는 함수
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

    // 우측 일정 목록 클릭 시 실행
    function applyEventToForm(event) {
      if (!event || !panel) return;
      var titleInput = panel.querySelector("[data-calendar-title]");
      var startDateInput = panel.querySelector("[data-calendar-start-date]");
      var startTimeInput = panel.querySelector("[data-calendar-start-time]");
      var endDateInput = panel.querySelector("[data-calendar-end-date]");
      var endTimeInput = panel.querySelector("[data-calendar-end-time]");
      var memoInput = panel.querySelector("[data-calendar-memo]");

      if (titleInput) titleInput.value = event.title || "";
      if (startDateInput) startDateInput.value = formatDateInput(event.startAt);
      if (startTimeInput) startTimeInput.value = formatTimeInput(event.startAt);
      if (endDateInput) endDateInput.value = formatDateInput(event.endAt);
      if (endTimeInput) endTimeInput.value = formatTimeInput(event.endAt);

      // 수정할 일정의 카테고리 타입을 넣는다.
      if (event && event.eventType) {
        eventType = event.eventType;
        categoryButtons.forEach(function (btn) {                                                                                                                                                                                                                               
          var match = btn.getAttribute("data-calendar-category") === eventType;                                                                                                                                                                                                
          if (match) {                                                                                                                                                                                                                                                         
            btn.classList.add("is-active");                                                                                                                                                                                                                                    
          } else {                                                                                                                                                                                                                                                             
            btn.classList.remove("is-active");                                                                                                                                                                                                                                 
          }                                                                                                                                                                                                                                                                    
        });
      }

      if (memoInput) {
        memoInput.value = event.memo || "";
      }
      updateMemoGuide();
      // 메모 글자 수를 체크해서 표시하는 함수
      hasShownMemoLimitAlert = memoInput ? memoInput.value.length >= CALENDAR_MEMO_MAX_LENGTH : false;
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

    // 달력 6주 42칸 생성
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
      if (!date) return;
      var dateText = ymdFromDate(date);
      var startDateInput = panel.querySelector("[data-calendar-start-date]");
      var endDateInput = panel.querySelector("[data-calendar-end-date]");
      if (startDateInput) startDateInput.value = dateText;
      if (endDateInput) endDateInput.value = dateText;
    }

    // 월 단위 새로고침 총괄 함수
    function refreshMonthView(options) {
      var shouldSyncFormDate = !options || options.syncFormDate !== false;
      setMonthTitle(currentMonthDate);
      buildCalendarGrid(currentMonthDate);
      if (shouldSyncFormDate) {
        syncDefaultFormDate(getDefaultFormDate());

      }
      return fetchEventList();
    }

    // 월 이동 버튼 클릭 함수수
    function changeMonth(diff) {
      currentMonthDate = new Date(currentMonthDate.getFullYear(), currentMonthDate.getMonth() + diff, 1);
      selectedDate = ymdFromDate(currentMonthDate);
      clearEditMode();
      closePanel({ reset: true });
      refreshMonthView().catch(function () {
        alert("월별 일정 조회에 실패했습니다.");
      });
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
        .then(function (resp) {
          var body = resp ? resp.body : null;
          if (Array.isArray(body)) {
            cachedEvents = body;
          } else if (body && body.events) {
            cachedEvents = body.events;
          } else {
            cachedEvents = [];
          }
          renderEventsOnGrid(cachedEvents);
          renderSelectedDate(renderEventList, applyEventToForm);
          return cachedEvents;
        })
        .catch(function () {
          return [];
        });
    }

    // 신규 저장과 수정 저장 처리 함수
    function handleSave() {
      var titleInput = panel.querySelector("[data-calendar-title]");
      var startDateInput = panel.querySelector("[data-calendar-start-date]");
      var startTimeInput = panel.querySelector("[data-calendar-start-time]");
      var endDateInput = panel.querySelector("[data-calendar-end-date]");
      var endTimeInput = panel.querySelector("[data-calendar-end-time]");
      var memoInput = panel.querySelector("[data-calendar-memo]");

      var memoText = "";
      if (memoInput) {
        memoText = memoInput.value.trim();
      }
      // payload 객체 만들기
      var payload = {
        tripPlanId: null,
        title: "",
        startAt: buildDateTime("", ""),
        endAt: buildDateTime("", ""),
        eventType: eventType,
        memo: memoText
      };

      if (titleInput) {
        payload.title = titleInput.value.trim();
      }


      // 제목/시작/종료 필수값 검증
      if (startDateInput && startTimeInput) {
        payload.startAt = buildDateTime(startDateInput.value, startTimeInput.value);
      } else if (startDateInput) {
        payload.startAt = buildDateTime(startDateInput.value, "");
      } else if (startTimeInput) {
        payload.startAt = buildDateTime("", startTimeInput.value);
      }

      if (endDateInput && endTimeInput) {
        payload.endAt = buildDateTime(endDateInput.value, endTimeInput.value);
      } else if (endDateInput) {
        payload.endAt = buildDateTime(endDateInput.value, "");
      } else if (endTimeInput) {
        payload.endAt = buildDateTime("", endTimeInput.value);
      }

      if (!payload.title || !payload.startAt || !payload.endAt) {
        alert("일정 제목과 일시를 입력해주세요.");
        return;
      }

      // 메모 길이 검증증
      if (memoText.length > CALENDAR_MEMO_MAX_LENGTH) {
        alert("메모는 공백 포함 500자까지 입력할 수 있습니다.");
        return;
      }

      // 신규인지 수정인지에 따라 URL과 method 결정 함수
      var url = "/api/calendar/create";
      var method = "POST";
      if (currentEditId) {
        url = "/api/calendar/update/" + currentEditId;
        method = "PUT";
      }

      // fetch로 저장 요청
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
          return response.json();
        })
        .then(function (resp) {
          var savedEvent = resp ? resp.body : null;
          var savedStartAt = savedEvent && savedEvent.startAt ? savedEvent.startAt : payload.startAt;
          var savedDate = dateFromYmd(savedStartAt.split("T")[0]);
          if (savedDate) {
            currentMonthDate = savedDate;
            setMonthTitle(currentMonthDate);
            buildCalendarGrid(currentMonthDate);
            syncDefaultFormDate(currentMonthDate);
          }
          closePanel({ reset: true });
          clearEditMode();
          return fetchEventList().catch(function () {
            alert("일정은 저장됐지만 목록 갱신에 실패했습니다.");
          });
        })
        .catch(function () { // 500
          if (currentEditId) {
            alert("일정 수정에 실패했습니다.");
          } else {
            alert("일정 저장에 실패했습니다.");
          }
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
          return response.json();
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

    // 카테고리 선택 상태 관리 코드 
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


    if (memoField) {
      memoField.addEventListener("input", handleMemoInput);
      updateMemoGuide(); 
    }

    bindDatePickerOpen(startDateWrap, startDateField);
    bindDatePickerOpen(endDateWrap, endDateField);
    bindTimePickerOpen(startTimeWrap, startTimeField);
    bindTimePickerOpen(endTimeWrap, endTimeField);

    if (prevMonthButton) {
      prevMonthButton.addEventListener("click", function (event) {
        event.preventDefault();
        changeMonth(-1);
      });
    }

    if (nextMonthButton) {
      nextMonthButton.addEventListener("click", function (event) {
        event.preventDefault();
        changeMonth(1);
      });
    }

    document.addEventListener("keydown", function (event) {
      if (event.key === "Escape" && !panel.hidden) {
        closePanel({ reset: true });
        clearEditMode();
      }
    });

    refreshMonthView();
    syncDeleteAction();
    bindDaySelection(renderEventList, applyEventToForm);
  }

  function appendMemoCard(container, event, onMemoClick) {
    var card = document.createElement("div");
    card.className = "info-card-amber";
    var header = document.createElement("div");
    header.className = "row-between-start-mb2";
    var titleSpan = document.createElement("span");
    titleSpan.className = "calendar-add-event-span-03";
    titleSpan.textContent = event.title || "메모";
    header.appendChild(titleSpan);
    var body = document.createElement("p");
    body.className = "text-body-sm-relaxed";
    body.textContent = event.memo;
    card.appendChild(header);
    card.appendChild(body);
    if (typeof onMemoClick === "function") {
      card.addEventListener("click", function () {
        onMemoClick(event);
      });
    }
    container.appendChild(card);
  }

  function renderMemoList(filteredEvents, onMemoClick) {
    var listRoot = document.querySelector("[data-calendar-memo-list]");
    if (!listRoot) return;
    listRoot.innerHTML = "";
    var memEvents = (filteredEvents || []).filter(function (event) {
      return event.memo && event.memo.trim();
    });
    if (!memEvents.length) {
      var empty = document.createElement("p");
      empty.className = "text-body-sm-relaxed";
      empty.textContent = "등록된 메모가 없습니다.";
      listRoot.appendChild(empty);
      return;
    }
    memEvents.forEach(function (event) {

      appendMemoCard(listRoot, event, onMemoClick);
    });
  }

  function renderSelectedDate(renderEventList, onMemoClick) {
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

    renderMemoList(filtered, onMemoClick);
  }

  function bindDaySelection(renderEventList, onMemoClick) {
    var grid = document.querySelector("[data-calendar-grid]");
    if (!grid) return;
    grid.addEventListener("click", function (event) {
      var cell = event.target.closest("[data-calendar-date]");
      if (!cell) return;
      var previous = grid.querySelector(".calendar-day-selected");
      if (previous) previous.classList.remove("calendar-day-selected");
      cell.classList.add("calendar-day-selected");
      selectedDate = cell.getAttribute("data-calendar-date");
      renderSelectedDate(renderEventList, onMemoClick);
    });
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initCalendarAddEventPanel);
  } else {
    initCalendarAddEventPanel();
  }
})();
