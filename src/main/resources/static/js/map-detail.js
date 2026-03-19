(function () {
  "use strict";

  // Expose region maps for other pages (e.g. main-index) without duplicating constants.
  window.TRAVEL_PLATFORM = window.TRAVEL_PLATFORM || {};

  var CATEGORY = {
    STAY: "AD5", // 숙박
    ATTRACTION: "AT4" // 관광명소
  };
  var REGION_VIEW = {
    seoul: { lat: 37.5665, lng: 126.978, level: 7 },
    busan: { lat: 35.1796, lng: 129.0756, level: 6 },
    daegu: { lat: 35.8714, lng: 128.6014, level: 7 },
    incheon: { lat: 37.4563, lng: 126.7052, level: 7 },
    gwangju: { lat: 35.1595, lng: 126.8526, level: 7 },
    daejeon: { lat: 36.3504, lng: 127.3845, level: 7 },
    ulsan: { lat: 35.5384, lng: 129.3114, level: 7 },
    sejong: { lat: 36.4801, lng: 127.2891, level: 8 },
    gyeonggi: { lat: 37.2636, lng: 127.0286, level: 9 },
    gangwon: { lat: 37.8813, lng: 127.7298, level: 9 },
    chungbuk: { lat: 36.6424, lng: 127.489, level: 9 },
    chungnam: { lat: 36.6013, lng: 126.6608, level: 9 },
    jeonbuk: { lat: 35.8242, lng: 127.148, level: 9 },
    jeonnam: { lat: 34.9913, lng: 126.4789, level: 9 },
    gyeongbuk: { lat: 36.5684, lng: 128.7294, level: 9 },
    gyeongnam: { lat: 35.2279, lng: 128.6811, level: 9 },
    jeju: { lat: 33.4996, lng: 126.5312, level: 8 }
  };
  var REGION_KEYWORDS = {
    seoul: ["서울", "서울특별시"],
    busan: ["부산", "부산광역시"],
    daegu: ["대구", "대구광역시"],
    incheon: ["인천", "인천광역시"],
    gwangju: ["광주", "광주광역시"],
    daejeon: ["대전", "대전광역시"],
    ulsan: ["울산", "울산광역시"],
    sejong: ["세종", "세종특별자치시"],
    gyeonggi: ["경기도", "경기", "수원", "성남", "용인", "고양", "부천", "안산", "화성", "평택", "의정부", "남양주"],
    gangwon: ["강원", "강원도", "강원특별자치도", "춘천", "원주", "강릉", "속초", "동해", "삼척", "태백"],
    chungbuk: ["충북", "충청북도", "청주", "충주", "제천"],
    chungnam: ["충남", "충청남도", "천안", "아산", "서산", "당진", "공주", "보령", "홍성"],
    jeonbuk: ["전북", "전라북도", "전주", "군산", "익산", "정읍", "남원"],
    jeonnam: ["전남", "전라남도", "여수", "순천", "목포", "나주", "광양", "무안"],
    gyeongbuk: ["경북", "경상북도", "포항", "경주", "구미", "안동", "김천", "영주", "경산"],
    gyeongnam: ["경남", "경상남도", "창원", "김해", "진주", "거제", "통영", "양산", "사천"],
    jeju: ["제주", "제주특별자치도", "제주시", "서귀포"]
  };

  window.TRAVEL_PLATFORM.REGION_VIEW = REGION_VIEW;
  window.TRAVEL_PLATFORM.REGION_KEYWORDS = REGION_KEYWORDS;

  function formatLocalDateISO(date) {
    var year = date.getFullYear();
    var month = String(date.getMonth() + 1).padStart(2, "0");
    var day = String(date.getDate()).padStart(2, "0");
    return year + "-" + month + "-" + day;
  }

  function initDateMinConstraints() {
    var today = formatLocalDateISO(new Date());
    var startDateEl = document.getElementById("mapStartDate");
    var endDateEl = document.getElementById("mapEndDate");

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

  function applySearchParamsFromUrl() {
    var params = new URLSearchParams(window.location.search || "");
    var regionSelect = document.getElementById("mapRegion");
    var startDateEl = document.getElementById("mapStartDate");
    var endDateEl = document.getElementById("mapEndDate");
    var guestsEl = document.getElementById("mapGuests");
    var regionKey = params.get("region");
    var checkIn = params.get("checkIn");
    var checkOut = params.get("checkOut");
    var guests = params.get("guests");

    if (regionSelect && regionKey && REGION_VIEW[regionKey]) {
      regionSelect.value = regionKey;
    }
    if (startDateEl && checkIn && /^\d{4}-\d{2}-\d{2}$/.test(checkIn)) {
      startDateEl.value = checkIn;
    }
    if (endDateEl && checkOut && /^\d{4}-\d{2}-\d{2}$/.test(checkOut)) {
      endDateEl.value = checkOut;
    }
    if (guestsEl && guests) {
      var guestOption = Array.from(guestsEl.options).find(function (option) {
        return option.value === guests || option.text === guests;
      });
      if (guestOption) {
        guestsEl.value = guestOption.value;
      }
    }
  }

  function hasSearchParamsInUrl() {
    var params = new URLSearchParams(window.location.search || "");
    return Boolean(params.get("region") || params.get("checkIn") || params.get("checkOut") || params.get("guests"));
  }

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
          applySearchParamsFromUrl();

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
          CURRENT_MAP_STATE = state;

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

  function getSelectedNights() {
    var startDateEl = document.getElementById("mapStartDate");
    var endDateEl = document.getElementById("mapEndDate");
    var checkIn = formatDateInputValue(startDateEl ? startDateEl.value : "");
    var checkOut = formatDateInputValue(endDateEl ? endDateEl.value : "");
    if (!checkIn || !checkOut) {
      return 1;
    }

    var inDate = new Date(checkIn + "T00:00:00");
    var outDate = new Date(checkOut + "T00:00:00");
    if (!Number.isFinite(inDate.getTime()) || !Number.isFinite(outDate.getTime())) {
      return 1;
    }
    var diffDays = Math.round((outDate.getTime() - inDate.getTime()) / 86400000);
    return Math.max(1, diffDays);
  }

  function buildCheckoutUrl(item, imageUrl, roomName) {
    var params = new URLSearchParams();
    params.set("lodgingName", item.name || "숙소");
    params.set("address", item.roadAddress || item.address || "주소 정보 없음");
    if (imageUrl) {
      params.set("imageUrl", imageUrl);
    }
    if (roomName) {
      params.set("roomName", roomName);
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

    var pricing = getPricing(item, getSelectedNights());
    var nightlyPrice = pricing.roomPrice;
    
    // 방 종류에 따른 가격 변동 (가상)
    if (roomName && roomName.indexOf("디럭스") >= 0) nightlyPrice += 50000;
    if (roomName && roomName.indexOf("스위트") >= 0) nightlyPrice += 150000;
    
    var fee = Math.round(nightlyPrice * 0.18);
    params.set("roomPrice", String(nightlyPrice));
    params.set("fee", String(fee));

    return "/bookings/checkout?" + params.toString();
  }

  async function goToBookingCheckout(state, item, roomName) {
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
    window.location.href = buildCheckoutUrl(item, imageUrl, roomName);
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
      var address = item.roadAddress || item.address || "";
      if (address) {
        params.set("address", address);
      }

      var response = await fetch("/api/bookings/place-image?" + params.toString(), {
        method: "GET",
        headers: { Accept: "application/json" }
      });

      if (!response.ok) {
        state.imageCache[cacheKey] = "";
        return "";
      }

      var resp = await response.json();
      var data = unwrapApiResponse(resp);
      var imageUrl = data && typeof data.imageUrl === "string" ? data.imageUrl : "";
      state.imageCache[cacheKey] = imageUrl;
      return imageUrl;
    } catch (error) {
      state.imageCache[cacheKey] = "";
      return "";
    }
  }

  function unwrapApiResponse(resp) {
    if (!resp) {
      return resp;
    }
    if (typeof resp === "object" && resp) {
      if ("body" in resp) {
        return resp.body;
      }
      if (Array.isArray(resp.items)) {
        return resp.items;
      }
    }
    return resp;
  }

  function getPricing(item, nights) {
    var safeNights = Number.isFinite(nights) && nights > 0 ? Math.floor(nights) : 1;
    var roomPrice = 150000 + (hashText(item.name || item.id || "hotel") % 260000);
    var fee = Math.round(roomPrice * 0.18);
    var roomSubtotal = roomPrice * safeNights;
    var feeSubtotal = fee * safeNights;
    var total = roomSubtotal + feeSubtotal;
    return {
      roomPrice: roomPrice,
      roomSubtotal: roomSubtotal,
      fee: fee,
      feeSubtotal: feeSubtotal,
      total: total,
      nights: safeNights
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

  async function renderRoomList(state, item, container) {
    if (!container) return;
    if (item.type !== "hotel") {
      container.innerHTML = "";
      return;
    }

    // 로딩 표시
    container.innerHTML = '<div class="map-poi-panel-div-16"><p style="font-size: 0.875rem; color: #64748b;">객실 정보를 불러오는 중...</p></div>';

    var pricing = getPricing(item, 1);
    var basePrice = pricing.roomPrice;
    var rooms = [];

    try {
      // 서버 API 호출 (이름과 주소 전달)
      var params = new URLSearchParams();
      params.set("lodgingName", item.name);
      params.set("address", item.roadAddress || item.address || "");
      
      var response = await fetch("/api/bookings/rooms?" + params.toString());
      if (response.ok) {
        var resp = await response.json();
        var data = unwrapApiResponse(resp);
        if (Array.isArray(data) && data.length > 0) {
          rooms = data.map(function(r) {
            return {
              name: r.name || "객실",
              price: basePrice + (hashText(r.name) % 100000),
              img: r.imageUrl || "https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=400&h=250&fit=crop",
              desc: r.content || "상세 정보 없음"
            };
          });
        }
      }
    } catch (error) {
      console.error("TourAPI fetch error:", error);
    }

    // 데이터가 없으면 가상 데이터 사용 (폴백) - 숙소 이름을 기반으로 다양하게 생성
    if (rooms.length === 0) {
      var seed = hashText(item.name);
      var themes = ["모던 ", "클래식 ", "우드톤 ", "미니멀 ", "럭셔리 "];
      var theme = themes[seed % themes.length];
      
      var roomCount = 2 + (seed % 3); // 2~4개 객실 생성
      for (var i = 0; i < roomCount; i++) {
        var roomType = (i === 0) ? "스탠다드" : (i === 1) ? "디럭스" : (i === 2) ? "프리미엄" : "스위트";
        var viewType = (seed + i) % 2 === 0 ? " 시티뷰" : " 마운틴뷰";
        if (item.address && (item.address.indexOf("부산") >= 0 || item.address.indexOf("제주") >= 0)) {
            viewType = (seed + i) % 2 === 0 ? " 오션뷰" : " 비치뷰";
        }

        rooms.push({
          name: theme + roomType + viewType,
          price: basePrice + (i * 35000) + (seed % 5 * 5000),
          img: i === 0 ? "https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=400&h=250&fit=crop" :
               i === 1 ? "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=400&h=250&fit=crop" :
               "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=400&h=250&fit=crop",
          desc: item.name + "에서 정성껏 준비한 " + theme + "테마의 " + roomType + " 객실입니다."
        });
      }
    }

    var html = '<div class="map-poi-panel-div-16"><h3 class="map-poi-panel-h3-01">객실 선택</h3>';
    html += '<div class="room-list-group" style="display: flex; flex-direction: column; gap: 1rem; margin-top: 1rem;">';
    
    rooms.forEach(function(room) {
      html += '<div class="room-card" style="border: 1px solid #e2e8f0; border-radius: 0.75rem; overflow: hidden; background: #fff;">' +
              '<img src="' + room.img + '" style="width: 100%; height: 160px; object-fit: cover;" alt="' + room.name + '">' +
              '<div style="padding: 1rem;">' +
              '<h4 style="font-size: 1rem; font-weight: 700; color: #0f172a; margin-bottom: 0.25rem;">' + room.name + '</h4>' +
              '<p style="font-size: 0.875rem; color: #64748b; margin-bottom: 1rem; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">' + (room.desc || "최대 인원 2명 · 금연") + '</p>' +
              '<div style="display: flex; justify-content: space-between; align-items: flex-end;">' +
              '<div><span style="font-size: 1.125rem; font-weight: 800; color: #1152d4;">' + formatWon(room.price) + '</span><span style="font-size: 0.75rem; color: #94a3b8;"> / 1박</span></div>' +
              '<button class="fx-group-cta-primary room-book-btn" data-room-name="' + room.name + '" style="padding: 0.5rem 1rem; font-size: 0.875rem; height: auto; min-width: 0; width: auto;">예약하기</button>' +
              '</div></div></div>';
    });
    
    html += '</div></div>';
    container.innerHTML = html;

    // 예약 버튼 이벤트 바인딩
    container.querySelectorAll(".room-book-btn").forEach(function(btn) {
      btn.addEventListener("click", function() {
        var roomName = btn.getAttribute("data-room-name");
        goToBookingCheckout(state, item, roomName);
      });
    });
  }

  async function fillPoiPanelDynamicSections(state, item, requestSeq) {
    var panel = document.querySelector("[data-map-poi-panel]");
    if (!panel) {
      return;
    }

    updatePanelFacilities(panel, item);
    updatePanelReviewSummary(panel, item);
    
    // 방 리스트 렌더링
    var roomContainer = document.getElementById("poiRoomListContainer");
    renderRoomList(state, item, roomContainer);

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

    // Load price filter from URL
    var params = new URLSearchParams(window.location.search);
    var minPrice = parseInt(params.get("priceMin")) || 0;
    var maxPrice = parseInt(params.get("priceMax")) || Infinity;

    var filteredItems = state.items.filter(function (item) {
      if (item.type !== "hotel") return true; // 관광지는 그대로 표시 (원할 경우 관광지도 필터 가능)
      
      var pricing = getPricing(item, 1);
      return pricing.roomPrice >= minPrice && pricing.roomPrice <= maxPrice;
    });

    filteredItems.forEach(function (item) {
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
        openPoiDetailPanel(state, item);
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

  function setResultPanelExpanded(expanded) {
    var panel = document.querySelector("[data-map-result-panel]");
    var toggleButton = document.querySelector("[data-map-panel-toggle]");
    if (panel) {
      panel.hidden = !expanded;
    }
    if (toggleButton) {
      toggleButton.setAttribute("aria-expanded", String(expanded));
    }
  }

  function initializeResultPanel(state) {
    var container = document.querySelector("[data-map-drag-scroll]");
    var regionSelect = document.getElementById("mapRegion");
    setResultPanelExpanded(false);
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

    if (!state.hasSearched) {
      setResultPanelExpanded(false);
      container.hidden = true;
      updateResultCount(0);
      return;
    }

    setResultPanelExpanded(true);
    container.hidden = false;

    // Load price filter from URL
    var params = new URLSearchParams(window.location.search);
    var minPrice = parseInt(params.get("priceMin")) || 0;
    var maxPrice = parseInt(params.get("priceMax")) || Infinity;

    var listItems = state.items.filter(function (item) {
      if (item.type !== "hotel") return false;
      
      var pricing = getPricing(item, 1); // Get 1-night price for filtering
      return pricing.roomPrice >= minPrice && pricing.roomPrice <= maxPrice;
    });

    if (!listItems.length) {
      container.innerHTML = '<div class="panel-muted-center-sm">현재 설정된 가격 범위 내 숙소가 없습니다.</div>';
      updateResultCount(0);
      return;
    }

    var html = listItems
      .map(function (item) {
        var badge = item.type === "hotel" ? "숙소" : "관광지";
        var subtitle = item.roadAddress || item.address || "주소 정보 없음";
        var pricingMin = getPricing(item, 1);
        var minPriceText = item.type === "hotel" ? formatWon(pricingMin.roomPrice) + "~" : "정보 없음";
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
          '<div class="text-neutral-xs-subtle">최소가격</div>' +
          '<div class="text-right-align"><p class="price-strong">' +
          minPriceText +
          '</p><span class="text-neutral-xs-subtle">/ 1박</span></div>' +
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
      var response = await fetch("/api/bookings/map-pois/merge", {
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
      var resp = await response.json();
      var data = unwrapApiResponse(resp);
      var items = Array.isArray(data) ? data.map(normalizeMergedPoi).filter(Boolean) : [];
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
    var startDateEl = document.getElementById("mapStartDate");
    var endDateEl = document.getElementById("mapEndDate");
    if (!regionSelect || !submitButton) {
      return;
    }
    var searchForm = submitButton.closest ? submitButton.closest("form") : null;

    function canSearch() {
      return Boolean(startDateEl && endDateEl && startDateEl.value && endDateEl.value);
    }

    function reportFormValidity() {
      if (searchForm && typeof searchForm.reportValidity === "function") {
        searchForm.reportValidity();
        return;
      }
      if (startDateEl && !startDateEl.value) {
        startDateEl.focus();
        return;
      }
      if (endDateEl && !endDateEl.value) {
        endDateEl.focus();
      }
    }

    function searchBySelectedRegion(triggerValidation) {
      if (!canSearch()) {
        if (triggerValidation) {
          reportFormValidity();
        }
        return;
      }
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
      searchBySelectedRegion(true);
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

    function refreshListPriceOnly() {
      if (!state.hasSearched) {
        return;
      }
      renderList(state);
    }

    if (startDateEl) {
      startDateEl.addEventListener("change", refreshListPriceOnly);
    }
    if (endDateEl) {
      endDateEl.addEventListener("change", refreshListPriceOnly);
    }

    if (hasSearchParamsInUrl()) {
      searchBySelectedRegion(false);
    }
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
    setResultPanelExpanded(!panel.hidden);

    toggleButton.addEventListener("click", function (event) {
      event.preventDefault();
      setResultPanelExpanded(panel.hidden);
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

  var CURRENT_MAP_STATE = null;

  function initPriceRangeSheet() {
    var toggleButton = document.querySelector("[data-price-range-toggle]");
    var sheet = document.querySelector("[data-price-range-sheet]");
    var backdrop = document.querySelector("[data-price-range-backdrop]");

    if (!toggleButton || !sheet || !backdrop) {
      return;
    }

    var labelEl = toggleButton.querySelector("[data-price-range-label]");
    var closeButton = sheet.querySelector("[data-price-range-close]");
    var applyButton = sheet.querySelector("[data-price-range-apply]");
    var clearButton = sheet.querySelector("[data-price-range-clear]");
    var minInput = sheet.querySelector("#priceMin");
    var maxInput = sheet.querySelector("#priceMax");
    var minLabel = sheet.querySelector("#priceMinLabel");
    var maxLabel = sheet.querySelector("#priceMaxLabel");
    var sliderRange = sheet.querySelector("#priceSliderRange");

    var defaultLabelText = labelEl ? labelEl.textContent : toggleButton.textContent;
    var storageKey = "travel_platform_price_range_v1";

    function updateSliderUI() {
      var min = parseInt(minInput.value);
      var max = parseInt(maxInput.value);

      if (min > max - 50000) {
        if (event && event.target === minInput) {
          minInput.value = max - 50000;
          min = max - 50000;
        } else {
          maxInput.value = min + 50000;
          max = min + 50000;
        }
      }

      var minPercent = (min / minInput.max) * 100;
      var maxPercent = (max / maxInput.max) * 100;

      sliderRange.style.left = minPercent + "%";
      sliderRange.style.width = (maxPercent - minPercent) + "%";

      if (minLabel) minLabel.textContent = (min / 10000) + "만원";
      if (maxLabel) maxLabel.textContent = max >= 1000000 ? "100만원+" : (max / 10000) + "만원";
    }

    function getRangeFromInputs() {
      return {
        min: parseInt(minInput.value),
        max: parseInt(maxInput.value) >= 1000000 ? null : parseInt(maxInput.value)
      };
    }

    function setInputs(range) {
      minInput.value = range && range.min != null ? String(range.min) : "0";
      maxInput.value = range && range.max != null ? String(range.max) : "1000000";
      updateSliderUI();
    }

    function updateUrlParams(range) {
      var url = new URL(window.location.href);
      var params = url.searchParams;

      if (!range || range.min == null || range.min === 0) {
        params.delete("priceMin");
      } else {
        params.set("priceMin", String(range.min));
      }

      if (!range || range.max == null) {
        params.delete("priceMax");
      } else {
        params.set("priceMax", String(range.max));
      }

      var qs = params.toString();
      var next = url.pathname + (qs ? "?" + qs : "") + url.hash;
      window.history.replaceState(null, "", next);
    }

    function rangeToLabel(range) {
      if (!range || (range.min <= 0 && range.max == null)) {
        return defaultLabelText;
      }
      var minWon = range.min ? (range.min / 10000) + "만" : "0";
      var maxWon = range.max ? (range.max / 10000) + "만" : "제한없음";
      return "가격: " + minWon + "~" + maxWon;
    }

    function setButtonLabel(range) {
      if (labelEl) {
        labelEl.textContent = rangeToLabel(range);
      }
    }

    function saveRange(range) {
      if (!range || (range.min <= 0 && range.max == null)) {
        localStorage.removeItem(storageKey);
      } else {
        localStorage.setItem(storageKey, JSON.stringify(range));
      }
    }

    function loadRange() {
      var params = new URLSearchParams(window.location.search);
      var min = params.get("priceMin");
      var max = params.get("priceMax");
      if (min !== null || max !== null) {
        return { min: parseInt(min) || 0, max: max === null ? null : parseInt(max) };
      }
      try {
        var raw = localStorage.getItem(storageKey);
        return raw ? JSON.parse(raw) : null;
      } catch (e) { return null; }
    }

    function setOpen(open) {
      sheet.hidden = !open;
      backdrop.hidden = !open;
      toggleButton.setAttribute("aria-expanded", String(open));

      if (open) {
        var buttonRect = toggleButton.getBoundingClientRect();
        var container = document.querySelector(".map-detail-div-01");
        var containerRect = container.getBoundingClientRect();

        var top = buttonRect.bottom - containerRect.top + 8;
        var left = buttonRect.left - containerRect.left;

        var sheetWidth = 320;
        if (left + sheetWidth > containerRect.width) {
          left = containerRect.width - sheetWidth - 16;
        }

        sheet.style.top = top + "px";
        sheet.style.left = Math.max(16, left) + "px";
        sheet.style.right = "auto";
      }
    }

    minInput.addEventListener("input", updateSliderUI);
    maxInput.addEventListener("input", updateSliderUI);

    var initialRange = loadRange();
    setInputs(initialRange);
    setButtonLabel(initialRange);

    toggleButton.addEventListener("click", function (event) {
      event.preventDefault();
      setOpen(sheet.hidden);
    });

    backdrop.addEventListener("click", function (event) {
      event.preventDefault();
      setOpen(false);
    });

    if (closeButton) {
      closeButton.addEventListener("click", function (event) {
        event.preventDefault();
        setOpen(false);
      });
    }

    if (clearButton) {
      clearButton.addEventListener("click", function (event) {
        event.preventDefault();
        setInputs(null);
        saveRange(null);
        updateUrlParams(null);
        setButtonLabel(null);
        setOpen(false);
        if (CURRENT_MAP_STATE) {
          renderList(CURRENT_MAP_STATE);
          renderOverlays(CURRENT_MAP_STATE);
        }
      });
    }

    if (applyButton) {
      applyButton.addEventListener("click", function (event) {
        event.preventDefault();
        var range = getRangeFromInputs();
        saveRange(range);
        updateUrlParams(range);
        setButtonLabel(range);
        setOpen(false);
        if (CURRENT_MAP_STATE) {
          renderList(CURRENT_MAP_STATE);
          renderOverlays(CURRENT_MAP_STATE);
        }
      });
    }

    document.addEventListener("keydown", function (event) {
      if (event.key === "Escape" && !sheet.hidden) {
        setOpen(false);
      }
    });
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", function () {
      initDateMinConstraints();
      applySearchParamsFromUrl();
      initKakaoMap();
      initMapDetailPanelToggle();
      initMapPoiPanelToggle();
      initPriceRangeSheet();
      initMapDragScroll();
    });
  } else {
    initDateMinConstraints();
    applySearchParamsFromUrl();
    initKakaoMap();
    initMapDetailPanelToggle();
    initMapPoiPanelToggle();
    initPriceRangeSheet();
    initMapDragScroll();
  }
})();
