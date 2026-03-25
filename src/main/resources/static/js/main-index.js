(function () {
  "use strict";

  function formatLocalDateISO(date) {
    var year = date.getFullYear();
    var month = String(date.getMonth() + 1).padStart(2, "0");
    var day = String(date.getDate()).padStart(2, "0");
    return year + "-" + month + "-" + day;
  }

  function initDateMinConstraints() {
    var today = formatLocalDateISO(new Date());
    var startDateEl = document.getElementById("startDate");
    var endDateEl = document.getElementById("endDate");

    if (startDateEl) {
      startDateEl.min = today;
      if (startDateEl.value && startDateEl.value < today) {
        startDateEl.value = today;
      }
    }

    if (endDateEl) {
      endDateEl.min = today;
      if (endDateEl.value && endDateEl.value < today) {
        endDateEl.value = today;
      }
    }

    if (startDateEl && endDateEl) {
      startDateEl.addEventListener("change", function () {
        var minEnd = startDateEl.value && startDateEl.value > today ? startDateEl.value : today;
        endDateEl.min = minEnd;
        if (endDateEl.value && endDateEl.value < minEnd) {
          endDateEl.value = minEnd;
        }
      });
    }
  }

  function normalizeText(text) {
    return String(text || "")
      .replace(/\s+/g, " ")
      .trim()
      .toLowerCase();
  }

  function matchRegionKeyFromTitle(titleText, regionKeywords) {
    var title = normalizeText(titleText);
    if (!title) {
      return "";
    }

    var bestKey = "";
    var bestKeywordLen = 0;

    Object.keys(regionKeywords || {}).forEach(function (regionKey) {
      var keywords = regionKeywords[regionKey] || [];
      keywords.forEach(function (keyword) {
        var k = normalizeText(keyword);
        if (!k) {
          return;
        }
        if (title.indexOf(k) >= 0) {
          if (k.length > bestKeywordLen) {
            bestKey = regionKey;
            bestKeywordLen = k.length;
          }
        }
      });
    });

    return bestKey;
  }

  function buildMapDetailUrl(regionKey) {
    var params = new URLSearchParams();
    params.set("region", regionKey);

    // If the main search form already has values, forward them.
    var form = document.querySelector("form.main-index-form-01");
    if (form) {
      var checkInEl = form.querySelector('[name="checkIn"]');
      var checkOutEl = form.querySelector('[name="checkOut"]');
      var guestsEl = form.querySelector('[name="guests"]');

      if (checkInEl && checkInEl.value) {
        params.set("checkIn", checkInEl.value);
      }
      if (checkOutEl && checkOutEl.value) {
        params.set("checkOut", checkOutEl.value);
      }
      if (guestsEl && guestsEl.value) {
        params.set("guests", guestsEl.value);
      }
    }

    var qs = params.toString();
    return "/bookings/map-detail" + (qs ? "?" + qs : "");
  }

  function initRegionCardLinks() {
    document.addEventListener("click", function (event) {
      // Support multiple card types on the main page.
      // - Featured big card: .main-index-div-12
      // - Small media cards: .media-card (overlay contains h3)
      // - Wide feature card: .main-index-div-14
      var card = event.target.closest(".main-index-div-12, .media-card, .main-index-div-14");
      if (!card) {
        return;
      }

      var heading = card.querySelector("h3");
      var title = heading ? heading.textContent : "";
      var regionKeywords =
        (window.TRAVEL_PLATFORM && window.TRAVEL_PLATFORM.REGION_KEYWORDS) || {};
      var regionKey = matchRegionKeyFromTitle(title, regionKeywords);
      if (!regionKey) {
        return;
      }

      event.preventDefault();
      window.location.href = buildMapDetailUrl(regionKey);
    });
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", function () {
      initDateMinConstraints();
      initRegionCardLinks();
    });
  } else {
    initDateMinConstraints();
    initRegionCardLinks();
  }
})();
