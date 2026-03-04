(function () {
  "use strict";

  var CATEGORY = {
    STAY: "AD5", // 숙박
    ATTRACTION: "AT4" // 관광명소
  };
  var REGION_VIEW = {
    seoul: { lat: 37.5665, lng: 126.978, level: 7 },
    busan: { lat: 35.1796, lng: 129.0756, level: 6 },
    jeju: { lat: 33.4996, lng: 126.5312, level: 8 },
    gyeongju: { lat: 35.8562, lng: 129.2247, level: 6 },
    gangwon: { lat: 37.8228, lng: 128.1555, level: 9 }
  };
  var REGION_KEYWORDS = {
    seoul: ["서울"],
    busan: ["부산"],
    jeju: ["제주"],
    gyeongju: ["경주"],
    gangwon: ["강원", "춘천", "원주", "강릉", "속초", "동해", "삼척", "태백"]
  };

  function initKakaoMap() {
    var mapElement = document.getElementById("kakaoMap");
    if (!mapElement) {
      return;
    }

    function showMapError(message) {
      mapElement.style.display = "flex";
      mapElement.style.alignItems = "center";
      mapElement.style.justifyContent = "center";
      mapElement.style.background = "#e5e7eb";
      mapElement.style.color = "#0f172a";
      mapElement.style.fontSize = "14px";
      mapElement.style.fontWeight = "600";
      mapElement.textContent = message;
    }

    function createMap() {
      try {
        kakao.maps.load(function () {
          var map = new kakao.maps.Map(mapElement, {
            center: new kakao.maps.LatLng(35.1795543, 129.0756416),
            level: 4
          });

          var regionSelect = document.getElementById("mapRegion");
          var state = {
            map: map,
            places: new kakao.maps.services.Places(map),
            overlays: [],
            items: [],
            hasSearched: false,
            syncByViewport: false,
            idleTimer: null,
            loading: false,
            currentRegionKey: regionSelect ? regionSelect.value : "busan",
            panelRequestSeq: 0,
            imageCache: {},
            nearbyById: {},
            focusOverlay: null
          };

          initializeResultPanel(state);
          bindViewportSync(state);
          bindTopSearchBar(state);
          initPoiCardMapLink(state);
          initNearbyPoiLink(state);

          window.addEventListener("resize", function () {
            map.relayout();
            if (state.syncByViewport) {
              fetchAndRenderVisiblePois(state);
            }
          });
        });
      } catch (error) {
        console.error(error);
        showMapError("지도를 불러오지 못했습니다. 앱 키/도메인 설정을 확인하세요.");
      }
    }

    if (typeof window.kakao !== "undefined" && window.kakao.maps && window.kakao.maps.load) {
      createMap();
      return;
    }

    var tries = 0;
    var timer = window.setInterval(function () {
      tries += 1;
      if (typeof window.kakao !== "undefined" && window.kakao.maps && window.kakao.maps.load) {
        window.clearInterval(timer);
        createMap();
      } else if (tries > 60) {
        window.clearInterval(timer);
        showMapError("지도 SDK 로드 실패: Kakao 키/도메인(localhost:8080) 설정 확인");
      }
    }, 100);
  }

  function getTypeFromCategoryCode(code) {
    if (code === CATEGORY.STAY) {
      return "hotel";
    }
    return "attraction";
  }

  function getTypeIcon(type) {
    if (type === "hotel") {
      return "hotel";
    }
    return "attractions";
  }

  function createPoiMarkerNode(item) {
    var node = document.createElement("div");
    node.className = "stay-marker stay-marker--" + item.type;
    node.innerHTML =
      '<div class="stay-marker__pin"><span class="icon-ms-16">' + getTypeIcon(item.type) + "</span></div>" +
      '<div class="stay-marker__label">' + item.name + "</div>";

    node.addEventListener("mouseenter", function () {
      node.classList.add("is-hover");
    });
    node.addEventListener("mouseleave", function () {
      node.classList.remove("is-hover");
    });

    return node;
  }

  function normalizeNameKey(name) {
    return String(name || "")
      .replace(/\s+/g, "")
      .toLowerCase();
  }

  function createFallbackImageDataUri(item) {
    var isHotel = item.type === "hotel";
    var label = isHotel ? "HOTEL" : "ATTRACTION";
    var title = String(item.name || "Place").substring(0, 24);
    var start = isHotel ? "#1d4ed8" : "#ea580c";
    var end = isHotel ? "#0ea5e9" : "#f59e0b";
    var icon = isHotel ? "\ud83c\udfe8" : "\ud83d\udccd";
    var svg =
      '<svg xmlns="http://www.w3.org/2000/svg" width="1200" height="800">' +
      '<defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1">' +
      '<stop offset="0%" stop-color="' +
      start +
      '"/><stop offset="100%" stop-color="' +
      end +
      '"/></linearGradient></defs>' +
      '<rect width="1200" height="800" fill="url(#g)"/>' +
      '<circle cx="600" cy="325" r="120" fill="rgba(255,255,255,0.2)"/>' +
      '<text x="600" y="350" text-anchor="middle" font-size="96" fill="#ffffff">' +
      icon +
      "</text>" +
      '<text x="600" y="515" text-anchor="middle" font-size="56" font-weight="700" fill="#ffffff">' +
      label +
      "</text>" +
      '<text x="600" y="585" text-anchor="middle" font-size="42" fill="#ffffff">' +
      title.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;") +
      "</text>" +
      "</svg>";
    return "data:image/svg+xml;charset=UTF-8," + encodeURIComponent(svg);
  }

  function getPoiImageUrl(item) {
    return createFallbackImageDataUri(item);
  }

  function parseGuestCountLabel(rawValue) {
    var value = String(rawValue || "");
    var match = value.match(/\d+/);
    var count = match ? parseInt(match[0], 10) : 2;
    if (!Number.isFinite(count) || count < 1) {
      count = 2;
    }
    return "성인 " + count + "명";
  }

  function formatDateInputValue(raw) {
    var text = String(raw || "").trim();
    if (!/^\d{4}-\d{2}-\d{2}$/.test(text)) {
      return "";
    }
    return text;
  }

  function buildCheckoutUrl(item, imageUrl) {
    var params = new URLSearchParams();
    params.set("lodgingName", item.name || "숙소");
    params.set("address", item.roadAddress || item.address || "주소 정보 없음");
    if (imageUrl) {
      params.set("imageUrl", imageUrl);
    }

    var startDateEl = document.getElementById("mapStartDate");
    var endDateEl = document.getElementById("mapEndDate");
    var guestsEl = document.getElementById("mapGuests");
    var checkIn = formatDateInputValue(startDateEl ? startDateEl.value : "");
    var checkOut = formatDateInputValue(endDateEl ? endDateEl.value : "");
    var guests = parseGuestCountLabel(guestsEl ? guestsEl.value : "");
    if (checkIn) {
      params.set("checkIn", checkIn);
    }
    if (checkOut) {
      params.set("checkOut", checkOut);
    }
    params.set("guests", guests);

    var pricing = getPricing(item);
    var nightlyPrice = pricing.roomPrice;
    var fee = pricing.fee;
    params.set("roomPrice", String(nightlyPrice));
    params.set("fee", String(fee));

    return "/bookings/checkout?" + params.toString();
  }

  async function goToBookingCheckout(state, item) {
    var fallback = getPoiImageUrl(item);
    var imageUrl = fallback;
    try {
      var serverImage = await fetchPlaceImageFromServer(state, item);
      if (serverImage) {
        imageUrl = serverImage;
      }
    } catch (error) {
      console.error(error);
    }
    window.location.href = buildCheckoutUrl(item, imageUrl);
  }

  async function fetchPlaceImageFromServer(state, item) {
    var cacheKey = (item.placeUrl || "") + "|" + normalizeNameKey(item.name);
    if (cacheKey in state.imageCache) {
      return state.imageCache[cacheKey];
    }

    try {
      var params = new URLSearchParams();
      if (item.placeUrl) {
        params.set("placeUrl", item.placeUrl);
      }
      if (item.name) {
        params.set("name", item.name);
      }

      var response = await fetch("/bookings/place-image?" + params.toString(), {
        method: "GET",
        headers: { Accept: "application/json" }
      });

      if (!response.ok) {
        state.imageCache[cacheKey] = "";
        return "";
      }

      var data = await response.json();
      var imageUrl = data && typeof data.imageUrl === "string" ? data.imageUrl : "";
      state.imageCache[cacheKey] = imageUrl;
      return imageUrl;
    } catch (error) {
      state.imageCache[cacheKey] = "";
      return "";
    }
  }

  function getPricing(item) {
    var roomPrice = 150000 + (hashText(item.name || item.id || "hotel") % 260000);
    var fee = Math.round(roomPrice * 0.18);
    var total = roomPrice + fee;
    return {
      roomPrice: roomPrice,
      fee: fee,
      total: total
    };
  }

  function formatWon(value) {
    return Number(value || 0).toLocaleString("ko-KR") + "원";
  }

  function hashText(text) {
    var s = String(text || "");
    var hash = 0;
    for (var i = 0; i < s.length; i += 1) {
      hash = (hash << 5) - hash + s.charCodeAt(i);
      hash |= 0;
    }
    return Math.abs(hash);
  }

  function distanceKm(lat1, lng1, lat2, lng2) {
    var toRad = Math.PI / 180;
    var dLat = (lat2 - lat1) * toRad;
    var dLng = (lng2 - lng1) * toRad;
    var a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(lat1 * toRad) * Math.cos(lat2 * toRad) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
    return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  function createNearbyId(item) {
    return [item.id || "", item.name || "", item.lat || "", item.lng || ""].join("|");
  }

  function clearFocusOverlay(state) {
    if (state.focusOverlay) {
      state.focusOverlay.setMap(null);
      state.focusOverlay = null;
    }
  }

  function showFocusOverlay(state, item) {
    clearFocusOverlay(state);

    var position = new kakao.maps.LatLng(item.lat, item.lng);
    var node = createPoiMarkerNode({
      name: item.name || "장소",
      type: item.type || "attraction"
    });
    node.classList.add("is-hover");

    var overlay = new kakao.maps.CustomOverlay({
      position: position,
      content: node,
      yAnchor: 1.05,
      zIndex: 10
    });
    overlay.setMap(state.map);
    state.focusOverlay = overlay;
  }

  function buildFacilityRows(item) {
    if (item.type === "hotel") {
      return [
        ["체크인", "15:00 이후"],
        ["주차", "가능"],
        ["조식", "문의 필요"],
        ["와이파이", "무료"]
      ];
    }

    var category = item.categoryName || "";
    if (category.indexOf("박물관") >= 0 || category.indexOf("미술관") >= 0) {
      return [
        ["관람시간", "현장 확인"],
        ["주차", "현장 확인"],
        ["해설", "가능"],
        ["실내관람", "가능"]
      ];
    }

    return [
      ["운영시간", "현장 확인"],
      ["주차", "현장 확인"],
      ["추천코스", "1~2시간"],
      ["가족방문", "추천"]
    ];
  }

  function updatePanelFacilities(panel, item) {
    var rows = panel.querySelectorAll(".map-poi-panel-div-18 .panel-neutral-row");
    var values = buildFacilityRows(item);
    rows.forEach(function (row, index) {
      var titleEl = row.querySelector(".text-neutral-xs");
      var valueEl = row.querySelector(".label-sm-neutral-strong");
      var pair = values[index] || values[values.length - 1];
      if (titleEl) {
        titleEl.textContent = pair[0];
      }
      if (valueEl) {
        valueEl.textContent = pair[1];
      }
    });
  }

  function buildReviewSummary(item) {
    var seed = hashText(item.id || item.name);
    var rating = (4.1 + (seed % 9) * 0.1).toFixed(1);
    var reviewCount = 120 + (seed % 5400);
    var p5 = 55 + (seed % 35);
    var p4 = 8 + ((seed >> 2) % 26);
    var p3 = Math.max(2, 100 - p5 - p4);
    return {
      rating: rating,
      reviewCount: reviewCount,
      percentages: [p5, p4, p3]
    };
  }

  function updatePanelReviewSummary(panel, item) {
    var summary = buildReviewSummary(item);
    var ratingEl = panel.querySelector(".map-poi-panel-span-01");
    var reviewCountEl = panel.querySelector(".map-poi-panel-span-02");
    if (ratingEl) {
      ratingEl.textContent = summary.rating;
    }
    if (reviewCountEl) {
      reviewCountEl.textContent = "(" + summary.reviewCount.toLocaleString("ko-KR") + " 리뷰)";
    }

    var percentEls = panel.querySelectorAll(".map-poi-panel-div-19 .text-neutral-xs-right");
    var barEls = [
      panel.querySelector(".map-poi-panel-div-20"),
      panel.querySelector(".map-poi-panel-div-21"),
      panel.querySelector(".map-poi-panel-div-22")
    ];
    summary.percentages.forEach(function (p, index) {
      if (percentEls[index]) {
        percentEls[index].textContent = p + "%";
      }
      if (barEls[index]) {
        barEls[index].style.width = Math.max(6, Math.min(100, p)) + "%";
      }
    });

    var reviewerEls = panel.querySelectorAll(".map-poi-panel-span-07");
    var reviewTextEls = panel.querySelectorAll(".map-poi-panel-p-05");
    var nicknameBase = (item.name || "장소").replace(/\s+/g, "");
    if (reviewerEls[0]) {
      reviewerEls[0].textContent = nicknameBase.slice(0, 3) + " 방문객";
    }
    if (reviewerEls[1]) {
      reviewerEls[1].textContent = nicknameBase.slice(0, 2) + " 여행자";
    }
    if (reviewTextEls[0]) {
      reviewTextEls[0].textContent =
        item.name + " 주변 동선이 좋고 접근성이 좋아 다시 방문하고 싶은 장소였습니다.";
    }
    if (reviewTextEls[1]) {
      reviewTextEls[1].textContent =
        "현장 분위기가 좋고 사진 촬영 포인트가 많습니다. 혼잡 시간만 피하면 만족도가 높습니다.";
    }
  }

  function normalizeKeywordPlace(place) {
    var lat = parseFloat(place.y);
    var lng = parseFloat(place.x);
    if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
      return null;
    }
    var categoryName = place.category_name || "";
    var isStay = (place.category_group_code || "") === CATEGORY.STAY || categoryName.indexOf("숙박") >= 0;
    return {
      id: place.id || "",
      name: place.place_name || "이름 없음",
      lat: lat,
      lng: lng,
      address: place.address_name || "",
      roadAddress: place.road_address_name || "",
      placeUrl: place.place_url || "",
      categoryName: categoryName,
      type: isStay ? "hotel" : "attraction"
    };
  }

  function searchKeywordAround(state, keyword, lat, lng) {
    return new Promise(function (resolve) {
      state.places.keywordSearch(
        keyword,
        function (data, status) {
          if (status !== kakao.maps.services.Status.OK || !Array.isArray(data)) {
            resolve([]);
            return;
          }
          resolve(data.map(normalizeKeywordPlace).filter(Boolean));
        },
        {
          location: new kakao.maps.LatLng(lat, lng),
          radius: 3000,
          sort: "distance",
          size: 10
        }
      );
    });
  }

  async function fetchNearbyAttractions(state, item) {
    var queries = ["관광지", "명소", "액티비티"];
    var results = await Promise.all(
      queries.map(function (q) {
        return searchKeywordAround(state, q, item.lat, item.lng);
      })
    );
    var merged = dedupeById([].concat(results[0], results[1], results[2]))
      .filter(function (poi) {
        if (poi.name === item.name) {
          return false;
        }
        return poi.type === "attraction";
      })
      .slice(0, 3);
    return merged;
  }

  function updateNearbySection(state, panel, baseItem, nearbyItems) {
    var cards = panel.querySelectorAll(".map-poi-panel-div-28 .poi-suggest-card");
    state.nearbyById = {};
    var visibleCards = [];
    cards.forEach(function (card, index) {
      var item = nearbyItems[index];
      var titleEl = card.querySelector("h4");
      var descEl = card.querySelector("p");
      var imgEl = card.querySelector("img");
      var tagEl = card.querySelector(".gallery-tag");

      if (!item) {
        card.style.display = "none";
        card.removeAttribute("data-nearby-id");
        return;
      }

      var nearbyId = createNearbyId(item);
      state.nearbyById[nearbyId] = item;
      card.setAttribute("data-nearby-id", nearbyId);
      visibleCards.push({ card: card, item: item, imgEl: imgEl });
      card.style.display = "";
      if (titleEl) {
        titleEl.textContent = item.name;
      }
      if (descEl) {
        var km = distanceKm(baseItem.lat, baseItem.lng, item.lat, item.lng).toFixed(1);
        var shortCategory = (item.categoryName.split(">").pop() || "명소").trim();
        descEl.textContent = shortCategory + " · " + km + "km";
      }
      if (imgEl) {
        imgEl.src = getPoiImageUrl(item);
        imgEl.alt = item.name;
      }
      if (tagEl) {
        tagEl.textContent = "추천";
      }
    });
    return visibleCards;
  }

  async function updateNearbySectionImages(state, visibleCards, requestSeq) {
    await Promise.all(
      visibleCards.map(async function (entry) {
        if (!entry.imgEl) {
          return;
        }
        var serverImage = await fetchPlaceImageFromServer(state, entry.item);
        if (state.panelRequestSeq !== requestSeq) {
          return;
        }
        if (serverImage) {
          entry.imgEl.src = serverImage;
        }
      })
    );
  }

  async function fillPoiPanelDynamicSections(state, item, requestSeq) {
    var panel = document.querySelector("[data-map-poi-panel]");
    if (!panel) {
      return;
    }

    updatePanelFacilities(panel, item);
    updatePanelReviewSummary(panel, item);

    var imageEl = panel.querySelector(".map-poi-panel-div-03");
    try {
      var exactImageUrl = await fetchPlaceImageFromServer(state, item);
      if (state.panelRequestSeq === requestSeq && imageEl && exactImageUrl) {
        imageEl.style.backgroundImage = "url('" + exactImageUrl + "')";
      }
    } catch (error) {
      console.error(error);
    }

    try {
      var nearbyItems = await fetchNearbyAttractions(state, item);
      if (state.panelRequestSeq !== requestSeq) {
        return;
      }
      var visibleCards = updateNearbySection(state, panel, item, nearbyItems);
      await updateNearbySectionImages(state, visibleCards, requestSeq);
    } catch (error) {
      console.error(error);
    }
  }

  function openPoiDetailPanel(state, item) {
    var panel = document.querySelector("[data-map-poi-panel]");
    if (!panel) {
      return;
    }

    var titleEl = panel.querySelector(".map-poi-panel-h1-01");
    var subtitleEl = panel.querySelector(".map-poi-panel-p-01");
    var badgeEl = panel.querySelector(".map-poi-panel-div-10");
    var addressEl = panel.querySelector(".map-poi-panel-p-02");
    var openEl = panel.querySelector(".map-poi-panel-p-03");
    var descEl = panel.querySelector(".map-poi-panel-p-04");
    var imageEl = panel.querySelector(".map-poi-panel-div-03");
    var chipsWrapEl = panel.querySelector(".map-poi-panel-div-14");
    var toggleButton = document.querySelector("[data-map-poi-toggle]");

    if (titleEl) {
      titleEl.textContent = item.name || "장소";
    }
    if (subtitleEl) {
      subtitleEl.textContent = (item.categoryName || "관광명소") + " · " + (item.phone || "전화번호 정보 없음");
    }
    if (badgeEl) {
      badgeEl.textContent = item.type === "hotel" ? "숙소" : "관광명소";
    }
    if (addressEl) {
      var road = item.roadAddress || "";
      var jibun = item.address || "";
      addressEl.innerHTML = escapeHtml(road || jibun) + (road && jibun ? "<br/>(" + escapeHtml(jibun) + ")" : "");
    }
    if (openEl) {
      openEl.textContent = "운영시간 정보 없음";
    }
    if (descEl) {
      var urlText = item.placeUrl ? "자세히 보기: " + item.placeUrl : "상세 링크 정보 없음";
      descEl.textContent = "카테고리: " + (item.categoryName || "정보 없음") + " · " + urlText;
    }
    if (imageEl) {
      imageEl.style.backgroundImage = "url('" + getPoiImageUrl(item) + "')";
      imageEl.setAttribute("data-alt", item.name || "place image");
    }
    if (chipsWrapEl) {
      var chipValues = (item.categoryName || "")
        .split(">")
        .map(function (v) {
          return v.trim();
        })
        .filter(Boolean)
        .slice(-3);

      if (!chipValues.length) {
        chipValues = [item.type === "hotel" ? "숙소" : "관광지"];
      }

      chipsWrapEl.innerHTML = chipValues
        .map(function (chip) {
          return '<span class="chip-neutral-action">#' + escapeHtml(chip) + "</span>";
        })
        .join("");
    }
    panel.hidden = false;
    if (toggleButton) {
      toggleButton.setAttribute("aria-expanded", "true");
    }

    state.panelRequestSeq += 1;
    fillPoiPanelDynamicSections(state, item, state.panelRequestSeq);
  }

  function clearOverlays(state) {
    state.overlays.forEach(function (overlay) {
      overlay.setMap(null);
    });
    state.overlays = [];
  }

  function renderOverlays(state) {
    clearOverlays(state);

    state.items.forEach(function (item) {
      var position = new kakao.maps.LatLng(item.lat, item.lng);
      var node = createPoiMarkerNode(item);
      var overlay = new kakao.maps.CustomOverlay({
        position: position,
        content: node,
        yAnchor: 1.05,
        zIndex: item.type === "hotel" ? 3 : 2
      });

      node.addEventListener("click", function (event) {
        event.preventDefault();
        state.map.panTo(position);
        if (item.type === "hotel") {
          goToBookingCheckout(state, item);
        } else if (item.type === "attraction") {
          openPoiDetailPanel(state, item);
        }
      });

      overlay.setMap(state.map);
      state.overlays.push(overlay);
    });
  }

  function updateResultCount(count) {
    var countEl = document.querySelector(".map-detail-span-02");
    if (countEl) {
      countEl.textContent = String(count);
    }
  }

  function updateRegionLabel(regionSelect) {
    var regionEl = document.querySelector(".map-detail-region-label");
    if (!regionEl || !regionSelect) {
      return;
    }
    var selectedOption = regionSelect.options[regionSelect.selectedIndex];
    regionEl.textContent = selectedOption ? selectedOption.text : "";
  }

  function initializeResultPanel(state) {
    var panel = document.querySelector("[data-map-result-panel]");
    var container = document.querySelector("[data-map-drag-scroll]");
    var regionSelect = document.getElementById("mapRegion");
    if (panel) {
      panel.hidden = true;
    }
    if (container) {
      container.hidden = true;
    }
    updateRegionLabel(regionSelect);
    updateResultCount(0);
  }

  function escapeHtml(text) {
    return String(text || "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/\"/g, "&quot;")
      .replace(/'/g, "&#39;");
  }

  function renderList(state) {
    var container = document.querySelector("[data-map-drag-scroll]");
    if (!container) {
      return;
    }

    var panel = document.querySelector("[data-map-result-panel]");
    if (!state.hasSearched) {
      if (panel) {
        panel.hidden = true;
      }
      container.hidden = true;
      updateResultCount(0);
      return;
    }

    if (panel) {
      panel.hidden = false;
    }
    container.hidden = false;

    var listItems = state.items.filter(function (item) {
      return item.type === "hotel";
    });

    if (!listItems.length) {
      container.innerHTML = '<div class="panel-muted-center-sm">현재 지도 영역에 검색 결과가 없습니다.</div>';
      updateResultCount(0);
      return;
    }

    var html = listItems
      .map(function (item) {
        var badge = item.type === "hotel" ? "숙소" : "관광지";
        var subtitle = item.roadAddress || item.address || "주소 정보 없음";
        var pricing = getPricing(item);
        var totalPriceText = item.type === "hotel" ? formatWon(pricing.total) : "정보 없음";
        var imageUrl = getPoiImageUrl(item);
        return (
          '<div class="poi-card" data-map-card data-name="' +
          escapeHtml(item.name) +
          '" data-lat="' +
          item.lat +
          '" data-lng="' +
          item.lng +
          '" data-place-url="' +
          escapeHtml(item.placeUrl || "") +
          '">' +
          '<div class="poi-card-media">' +
          '<img alt="' +
          escapeHtml(item.name) +
          '" class="media-cover-zoom105" src="' +
          imageUrl +
          '" loading="lazy" data-map-list-image data-name="' +
          escapeHtml(item.name) +
          '" data-place-url="' +
          escapeHtml(item.placeUrl || "") +
          '"/>' +
          '<div class="poi-card-tag">' +
          badge +
          "</div>" +
          "</div>" +
          '<div class="p-4">' +
          '<div class="row-between-start-mb1">' +
          '<h4 class="poi-card-title">' +
          escapeHtml(item.name) +
          '</h4>' +
          '<div class="poi-rating"><span class="icon-ms-14-fill">place</span></div>' +
          '</div>' +
          '<p class="text-neutral-sm-truncate-mb3">' +
          escapeHtml(subtitle) +
          '</p>' +
          '<div class="map-detail-div-08"><span class="badge-neutral-xs">' +
          badge +
          '</span></div>' +
          '<div class="row-between-price">' +
          '<div class="text-neutral-xs-subtle">총 결제 금액</div>' +
          '<div class="text-right-align"><p class="price-strong">' +
          totalPriceText +
          "</p></div>" +
          "</div>" +
          '</div>' +
          '</div>'
        );
      })
      .join("");

    container.innerHTML = html;
    hydrateListImages(state);
    updateResultCount(listItems.length);
  }

  async function hydrateListImages(state) {
    var images = document.querySelectorAll("[data-map-list-image]");
    await Promise.all(
      Array.from(images).map(async function (imgEl) {
        var name = imgEl.getAttribute("data-name") || "";
        var placeUrl = imgEl.getAttribute("data-place-url") || "";
        var item = {
          name: name,
          placeUrl: placeUrl,
          type: "hotel"
        };
        var serverImage = await fetchPlaceImageFromServer(state, item);
        if (serverImage) {
          imgEl.src = serverImage;
        }
      })
    );
  }

  function normalizePlace(place, categoryCode) {
    var lat = parseFloat(place.y);
    var lng = parseFloat(place.x);
    if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
      return null;
    }

    return {
      id: place.id,
      name: place.place_name,
      lat: lat,
      lng: lng,
      phone: place.phone || "",
      address: place.address_name || "",
      roadAddress: place.road_address_name || "",
      placeUrl: place.place_url || "",
      categoryName: place.category_name || "",
      type: getTypeFromCategoryCode(categoryCode)
    };
  }

  function isInSelectedRegion(state, item) {
    var keywords = REGION_KEYWORDS[state.currentRegionKey] || [];
    if (!keywords.length) {
      return true;
    }

    var haystack = (item.roadAddress + " " + item.address + " " + item.categoryName).toLowerCase();
    return keywords.some(function (k) {
      return haystack.indexOf(String(k).toLowerCase()) >= 0;
    });
  }

  function searchCategoryInView(state, categoryCode) {
    return new Promise(function (resolve) {
      state.places.categorySearch(
        categoryCode,
        function (data, status) {
          if (status !== kakao.maps.services.Status.OK || !Array.isArray(data)) {
            resolve([]);
            return;
          }

          resolve(
            data
              .map(function (p) {
                return normalizePlace(p, categoryCode);
              })
              .filter(Boolean)
          );
        },
        {
          useMapBounds: true,
          size: 15,
          sort: "accuracy"
        }
      );
    });
  }

  function dedupeById(items) {
    var seen = new Set();
    return items.filter(function (item) {
      var key = item.id || item.name + "_" + item.lat + "_" + item.lng;
      if (seen.has(key)) {
        return false;
      }
      seen.add(key);
      return true;
    });
  }

  function readMapBounds(state) {
    if (!state || !state.map) {
      return null;
    }
    var bounds = state.map.getBounds();
    if (!bounds) {
      return null;
    }
    var sw = bounds.getSouthWest();
    var ne = bounds.getNorthEast();
    return {
      swLat: sw.getLat(),
      swLng: sw.getLng(),
      neLat: ne.getLat(),
      neLng: ne.getLng()
    };
  }

  function normalizeMergedPoi(raw) {
    if (!raw) {
      return null;
    }
    var lat = Number(raw.lat);
    var lng = Number(raw.lng);
    if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
      return null;
    }
    var groupCode = raw.categoryGroupCode || "";
    var type = raw.type || (groupCode === CATEGORY.STAY ? "hotel" : "attraction");
    return {
      id: raw.externalPlaceId || raw.id || "",
      name: raw.name || "이름 없음",
      lat: lat,
      lng: lng,
      phone: raw.phone || "",
      address: raw.address || "",
      roadAddress: raw.roadAddress || "",
      placeUrl: raw.placeUrl || "",
      categoryName: raw.categoryName || "",
      type: type
    };
  }

  async function mergePoisWithServer(state, kakaoItems) {
    var payload = {
      regionKey: state.currentRegionKey || "",
      bounds: readMapBounds(state),
      kakaoPois: (kakaoItems || []).map(function (item) {
        return {
          externalPlaceId: item.id || "",
          name: item.name || "",
          phone: item.phone || "",
          address: item.address || "",
          roadAddress: item.roadAddress || "",
          placeUrl: item.placeUrl || "",
          categoryName: item.categoryName || "",
          categoryGroupCode: item.type === "hotel" ? CATEGORY.STAY : CATEGORY.ATTRACTION,
          lat: Number(item.lat),
          lng: Number(item.lng),
          type: item.type || ""
        };
      })
    };

    try {
      var response = await fetch("/bookings/map-pois/merge", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json"
        },
        body: JSON.stringify(payload)
      });
      if (!response.ok) {
        return kakaoItems;
      }
      var data = await response.json();
      var items = data && Array.isArray(data.items) ? data.items.map(normalizeMergedPoi).filter(Boolean) : [];
      return items.length ? items : kakaoItems;
    } catch (error) {
      console.error(error);
      return kakaoItems;
    }
  }

  async function fetchAndRenderVisiblePois(state) {
    if (state.loading) {
      return;
    }

    state.loading = true;
    try {
      var results = await Promise.all([
        searchCategoryInView(state, CATEGORY.STAY),
        searchCategoryInView(state, CATEGORY.ATTRACTION)
      ]);

      var merged = dedupeById([].concat(results[0], results[1])).filter(function (item) {
        return isInSelectedRegion(state, item);
      });
      var unified = await mergePoisWithServer(state, merged);
      state.items = dedupeById(unified).filter(function (item) {
        return isInSelectedRegion(state, item);
      });
      renderOverlays(state);
      renderList(state);
    } finally {
      state.loading = false;
    }
  }

  function bindTopSearchBar(state) {
    var regionSelect = document.getElementById("mapRegion");
    var submitButton = document.querySelector(".map-search-submit");
    if (!regionSelect || !submitButton) {
      return;
    }

    function searchBySelectedRegion() {
      var regionKey = regionSelect.value;
      var view = REGION_VIEW[regionKey];
      if (!view) {
        return;
      }

      state.currentRegionKey = regionKey;
      updateRegionLabel(regionSelect);
      state.map.setLevel(view.level);
      state.map.panTo(new kakao.maps.LatLng(view.lat, view.lng));
      state.hasSearched = true;
      state.syncByViewport = true;
      fetchAndRenderVisiblePois(state);
    }

    submitButton.addEventListener("click", function (event) {
      event.preventDefault();
      searchBySelectedRegion();
    });

    regionSelect.addEventListener("change", function () {
      var regionKey = regionSelect.value;
      var view = REGION_VIEW[regionKey];
      if (!view) {
        return;
      }
      state.currentRegionKey = regionKey;
      updateRegionLabel(regionSelect);
      state.map.setLevel(view.level);
      state.map.panTo(new kakao.maps.LatLng(view.lat, view.lng));
    });
  }

  function bindViewportSync(state) {
    kakao.maps.event.addListener(state.map, "idle", function () {
      if (!state.syncByViewport) {
        return;
      }

      if (state.idleTimer) {
        window.clearTimeout(state.idleTimer);
      }
      state.idleTimer = window.setTimeout(function () {
        fetchAndRenderVisiblePois(state);
      }, 220);
    });
  }

  function initPoiCardMapLink(state) {
    var listContainer = document.querySelector("[data-map-drag-scroll]");
    if (!listContainer) {
      return;
    }

    listContainer.addEventListener("click", function (event) {
      var card = event.target.closest("[data-map-card]");
      if (!card) {
        return;
      }

      var lat = parseFloat(card.dataset.lat);
      var lng = parseFloat(card.dataset.lng);
      if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
        return;
      }

      // Keep list order unchanged; position selected card slightly below the sticky header area.
      var header = document.querySelector("#mapResultPanel .map-detail-div-06");
      var topOffset = header ? header.offsetHeight + 10 : 10;
      listContainer.scrollTop = Math.max(0, card.offsetTop - topOffset);

      state.map.panTo(new kakao.maps.LatLng(lat, lng));
      state.syncByViewport = false;
    });
  }

  function initNearbyPoiLink(state) {
    var panel = document.querySelector("[data-map-poi-panel]");
    if (!panel) {
      return;
    }

    panel.addEventListener("click", function (event) {
      var card = event.target.closest(".map-poi-panel-div-28 .poi-suggest-card");
      if (!card) {
        return;
      }

      var nearbyId = card.getAttribute("data-nearby-id");
      if (!nearbyId) {
        return;
      }

      var item = state.nearbyById[nearbyId];
      if (!item) {
        return;
      }

      var position = new kakao.maps.LatLng(item.lat, item.lng);
      state.map.panTo(position);
      showFocusOverlay(state, item);
      openPoiDetailPanel(state, item);
    });
  }

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

      if (event.target.closest("a, button, input, select, textarea, label, [data-map-card]")) {
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
      initKakaoMap();
      initMapDetailPanelToggle();
      initMapPoiPanelToggle();
      initMapDragScroll();
    });
  } else {
    initKakaoMap();
    initMapDetailPanelToggle();
    initMapPoiPanelToggle();
    initMapDragScroll();
  }
})();
