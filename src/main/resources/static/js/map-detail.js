(function () {
  "use strict";

  function initMapDragScroll() {
    var dragScrollArea = document.querySelector("[data-map-drag-scroll]");
    if (!dragScrollArea) {
      return;
    }

    var pointerActive = false;
    var dragging = false;
    var suppressClick = false;
    var pointerId = null;
    var startY = 0;
    var startScrollTop = 0;
    var dragThreshold = 6;

    function resetDragState() {
      pointerActive = false;
      dragging = false;
      pointerId = null;
      dragScrollArea.classList.remove("is-grabbed");
      dragScrollArea.classList.remove("is-dragging");
    }

    dragScrollArea.addEventListener("pointerdown", function (event) {
      if (event.pointerType === "mouse" && event.button !== 0) {
        return;
      }

      if (event.target.closest("a, button, input, select, textarea, label")) {
        return;
      }

      pointerActive = true;
      dragging = false;
      pointerId = event.pointerId;
      startY = event.clientY;
      startScrollTop = dragScrollArea.scrollTop;
      dragScrollArea.classList.add("is-grabbed");
      dragScrollArea.setPointerCapture(pointerId);
    });

    dragScrollArea.addEventListener("pointermove", function (event) {
      if (!pointerActive || event.pointerId !== pointerId) {
        return;
      }

      var deltaY = event.clientY - startY;
      if (!dragging && Math.abs(deltaY) > dragThreshold) {
        dragging = true;
        dragScrollArea.classList.add("is-dragging");
      }

      if (!dragging) {
        return;
      }

      dragScrollArea.scrollTop = startScrollTop - deltaY;
      event.preventDefault();
    });

    function endDrag(event) {
      if (!pointerActive) {
        return;
      }
      if (event && event.pointerId !== undefined && event.pointerId !== pointerId) {
        return;
      }
      if (dragging) {
        suppressClick = true;
      }
      resetDragState();
    }

    dragScrollArea.addEventListener("pointerup", endDrag);
    dragScrollArea.addEventListener("pointercancel", endDrag);
    dragScrollArea.addEventListener("lostpointercapture", endDrag);

    dragScrollArea.addEventListener(
      "click",
      function (event) {
        if (!suppressClick) {
          return;
        }
        suppressClick = false;
        event.preventDefault();
        event.stopPropagation();
      },
      true
    );
  }

  function initMapDetailPanelToggle() {
    var toggleButton = document.querySelector("[data-map-panel-toggle]");
    var panel = document.querySelector("[data-map-result-panel]");

    if (!toggleButton || !panel) {
      return;
    }

    function setExpanded(expanded) {
      panel.hidden = !expanded;
      toggleButton.setAttribute("aria-expanded", String(expanded));
    }

    setExpanded(!panel.hidden);

    toggleButton.addEventListener("click", function (event) {
      event.preventDefault();
      setExpanded(panel.hidden);
    });
  }

  function initMapPoiPanelToggle() {
    var toggleButton = document.querySelector("[data-map-poi-toggle]");
    var panel = document.querySelector("[data-map-poi-panel]");

    if (!toggleButton || !panel) {
      return;
    }

    var closeButton = panel.querySelector("[data-map-poi-close]");

    function setOpen(open) {
      panel.hidden = !open;
      toggleButton.setAttribute("aria-expanded", String(open));
    }

    // map-detail에서는 관광지 버튼 클릭 시에만 패널을 노출한다.
    setOpen(false);

    toggleButton.addEventListener("click", function (event) {
      event.preventDefault();
      setOpen(panel.hidden);
    });

    if (closeButton) {
      closeButton.addEventListener("click", function (event) {
        event.preventDefault();
        setOpen(false);
      });
    }

    document.addEventListener("keydown", function (event) {
      if (event.key === "Escape" && !panel.hidden) {
        setOpen(false);
      }
    });
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", function () {
      initMapDetailPanelToggle();
      initMapPoiPanelToggle();
      initMapDragScroll();
    });
  } else {
    initMapDetailPanelToggle();
    initMapPoiPanelToggle();
    initMapDragScroll();
  }
})();
