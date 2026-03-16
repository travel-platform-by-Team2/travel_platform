(function () {
  "use strict";

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
    document.addEventListener("DOMContentLoaded", initRegionCardLinks);
  } else {
    initRegionCardLinks();
  }
})();
