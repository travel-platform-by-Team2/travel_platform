(function () {
  "use strict";

  // State management
  var state = {
    map: null,
    places: null,
    overlays: [],
    items: [],
    currentRegionKey: null,
    currentCategory: "", // empty means '추천' (both AD5 and AT4)
    currentKeyword: "",
    idleTimer: null,
    loading: false
  };

  // 1-night base pricing estimation fallback
  function getFakePricing(item) {
    var num = parseInt(item.id, 10);
    if (isNaN(num)) num = Math.floor(Math.random() * 1000);
    return 50000 + (num % 50) * 5000;
  }

  async function fetchPlaceImageFromServer(item) {
    try {
      var params = new URLSearchParams();
      if (item.placeUrl) params.set("placeUrl", item.placeUrl);
      if (item.name) params.set("name", item.name);

      var response = await fetch("/api/bookings/place-image?" + params.toString(), {
        method: "GET",
        headers: { Accept: "application/json" }
      });

      if (!response.ok) return null;
      var data = await response.json();
      return data && typeof data.imageUrl === "string" ? data.imageUrl : null;
    } catch (error) {
      console.error(error);
      return null;
    }
  }

  function createFallbackImageDataUri(item) {
    var isHotel = item.type === "hotel";
    var start = isHotel ? "#1d4ed8" : "#ea580c";
    var end = isHotel ? "#0ea5e9" : "#f59e0b";
    var icon = isHotel ? "\ud83c\udfe8" : "\ud83d\udccd";
    var svg =
      '<svg xmlns="http://www.w3.org/2000/svg" width="1200" height="800">' +
      '<defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1">' +
      '<stop offset="0%" stop-color="' + start + '"/><stop offset="100%" stop-color="' + end + '"/>' +
      '</linearGradient></defs><rect width="1200" height="800" fill="url(#g)"/>' +
      '<circle cx="600" cy="325" r="120" fill="rgba(255,255,255,0.2)"/>' +
      '<text x="600" y="350" text-anchor="middle" font-size="96" fill="#ffffff">' + icon + '</text>' +
      '</svg>';
    return "data:image/svg+xml;charset=UTF-8," + encodeURIComponent(svg);
  }

  function normalizePlace(p, categoryCode) {
    var type = "attraction";
    if (categoryCode === "AD5" || p.category_group_code === "AD5") {
      type = "hotel";
    }

    return {
      id: p.id,
      name: p.place_name,
      address: p.road_address_name || p.address_name,
      lat: parseFloat(p.y),
      lng: parseFloat(p.x),
      categoryName: p.category_name,
      type: type,
      placeUrl: p.place_url || "",
      imgUrl: p.image_url || null, // Will be hydrated
      rating: (4.0 + Math.random()).toFixed(1),
      reviewCount: Math.floor(Math.random() * 5000)
    };
  }

  // Check if a place is within the initial selected region's text
  function isInSelectedRegion(item) {
    if (!state.currentRegionKey || !window.TRAVEL_PLATFORM || !window.TRAVEL_PLATFORM.REGION_KEYWORDS) {
      return true; // No restriction if region not detected
    }
    var keywords = window.TRAVEL_PLATFORM.REGION_KEYWORDS[state.currentRegionKey];
    if (!keywords || !keywords.length) return true;

    var haystack = String(item.address).toLowerCase();
    return keywords.some(function (k) {
      return haystack.indexOf(String(k).toLowerCase()) >= 0;
    });
  }

  // Map searches
  function searchCategoryInView(categoryCode) {
    return new Promise(function (resolve) {
      state.places.categorySearch(
        categoryCode,
        function (data, status) {
          if (status !== kakao.maps.services.Status.OK || !Array.isArray(data)) {
            resolve([]);
            return;
          }
          resolve(data.map(function (p) { return normalizePlace(p, categoryCode); }));
        },
        { useMapBounds: true, size: 15 }
      );
    });
  }

  function searchKeywordInView(keyword) {
    return new Promise(function (resolve) {
      state.places.keywordSearch(
        keyword,
        function (data, status) {
          if (status !== kakao.maps.services.Status.OK || !Array.isArray(data)) {
            resolve([]);
            return;
          }
          resolve(data.map(function (p) { return normalizePlace(p, p.category_group_code); }));
        },
        { useMapBounds: true, size: 15 }
      );
    });
  }

  function dedupeById(arr) {
    var seen = {};
    return arr.filter(function (item) {
      if (!item || !item.id || seen[item.id]) return false;
      seen[item.id] = true;
      return true;
    });
  }

  async function fetchAndRenderPois() {
    if (state.loading) return;
    state.loading = true;

    try {
      var results = [];
      if (state.currentKeyword) {
        results = await searchKeywordInView(state.currentKeyword);
      } else {
        if (!state.currentCategory) {
          var r1 = await searchCategoryInView("AT4"); 
          var r2 = await searchCategoryInView("AD5"); 
          results = dedupeById([].concat(r1, r2));
        } else {
          results = await searchCategoryInView(state.currentCategory);
        }
      }

      state.items = results.filter(isInSelectedRegion);

      // Hydrate images
      await Promise.all(state.items.map(async function(item) {
        if (!item.imgUrl && item.placeUrl) {
           var serverImg = await fetchPlaceImageFromServer(item);
           item.imgUrl = serverImg || createFallbackImageDataUri(item);
        } else if (!item.imgUrl) {
           item.imgUrl = createFallbackImageDataUri(item);
        }
      }));

      renderOverlays();
      renderList();
    } finally {
      state.loading = false;
    }
  }

  function escapeHtml(str) {
    if (!str) return "";
    return String(str)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/\"/g, "&quot;")
      .replace(/'/g, "&#39;");
  }

  function renderList() {
    var container = document.getElementById("placeListContainer");
    if (!container) return;

    if (!state.items || state.items.length === 0) {
      container.innerHTML = '<div class="panel-muted-center-sm" style="padding:2rem; text-align:center;">해당 지역에 검색 결과가 없습니다.</div>';
      return;
    }

    var html = state.items.map(function(item) {
      var typeLabel = item.type === "hotel" ? "숙소" : "명소";
      return `
        <div class="place-option-card" id="place-card-${item.id}">
          <div class="place-option-thumb">
            <img alt="Place" class="media-cover-zoom110" src="${escapeHtml(item.imgUrl)}"/>
          </div>
          <div class="place-option-content">
            <div>
              <div class="row-between-start">
                <h3 class="text-main-base-strong">${escapeHtml(item.name)}</h3>
                <span class="tag-surface-xs">${escapeHtml(typeLabel)}</span>
              </div>
              <p class="text-sub-xs-truncate-mt1">${escapeHtml(item.address)}</p>
              <div class="inline-row-gap1-mt1">
                <span class="icon-ms-14-yellow-fill">star</span>
                <span class="trip-plan-add-place-span-02">${item.rating}</span>
                <span class="trip-plan-add-place-span-03">(${item.reviewCount.toLocaleString()})</span>
              </div>
            </div>
          </div>
          <button class="btn-round-add" data-action="add-place" data-id="${item.id}">
            <span class="icon-ms-xl">add</span>
          </button>
        </div>
      `;
    }).join("");

    container.innerHTML = html;
  }

  function clearOverlays() {
    state.overlays.forEach(function (overlay) {
      overlay.setMap(null);
    });
    state.overlays = [];
  }

  function renderOverlays() {
    clearOverlays();

    state.items.forEach(function (item, index) {
      var position = new kakao.maps.LatLng(item.lat, item.lng);
      var el = document.createElement("div");
      
      el.style.cssText = "transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1); cursor: pointer;";
      
      // Use standard material symbols mapping but with nicer colors
      var icon = item.type === "hotel" ? "bed" : "attractions";
      var bgColor = item.type === "hotel" ? "#1e40af" : "#c2410c"; // deep blue or deep orange
      
      el.innerHTML = `
        <div class="fx-map-pin-custom" style="position:relative; background: ${bgColor}; border: 2px solid #ffffff; border-radius: 50%; width: 40px; height: 40px; box-shadow: 0 4px 8px rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; z-index: 2;">
          <span class="material-symbols-outlined" style="color: white; font-size: 20px;">${icon}</span>
          <div style="position: absolute; bottom: -6px; left: 50%; transform: translateX(-50%); width: 0; height: 0; border-left: 6px solid transparent; border-right: 6px solid transparent; border-top: 6px solid ${bgColor};"></div>
          
          <!-- Tooltip on hover -->
          <div class="pin-tooltip" style="position: absolute; bottom: 50px; left: 50%; transform: translateX(-50%); background: white; padding: 4px 8px; border-radius: 4px; box-shadow: 0 2px 4px rgba(0,0,0,0.2); font-size: 12px; font-weight: bold; white-space: nowrap; color: #1e293b; opacity: 0; transition: opacity 0.2s; pointer-events: none;">
            ${escapeHtml(item.name)}
          </div>
        </div>
      `;

      var tooltip = el.querySelector(".pin-tooltip");

      el.addEventListener("mouseenter", function() {
        el.style.transform = "translateY(-10px) scale(1.1)";
        el.style.zIndex = "10";
        if (tooltip) tooltip.style.opacity = "1";
      });
      el.addEventListener("mouseleave", function() {
        el.style.transform = "translateY(0) scale(1)";
        el.style.zIndex = "2";
        if (tooltip) tooltip.style.opacity = "0";
      });

      var overlay = new kakao.maps.CustomOverlay({
        position: position,
        content: el,
        yAnchor: 1.0, 
        zIndex: 2
      });

      overlay.setMap(state.map);
      state.overlays.push(overlay);

      el.addEventListener("click", function () {
        var card = document.getElementById("place-card-" + item.id);
        if (card) {
          card.scrollIntoView({ behavior: "smooth", block: "center" });
          card.style.transition = "background-color 0.3s";
          card.style.backgroundColor = "var(--surface-color-hover, #f3f4f6)";
          setTimeout(function() { card.style.backgroundColor = ""; }, 1500);
        }
        state.map.panTo(position);
      });
    });
  }

  // Setup Event Listeners
  function bindEvents() {
    // 1. Search Input
    var searchInput = document.getElementById("placeSearchInput");
    if (searchInput) {
      searchInput.addEventListener("keypress", function (e) {
        if (e.key === "Enter") {
          e.preventDefault();
          state.currentKeyword = this.value.trim();
          // Reset category visually
          document.querySelectorAll("#categoryTabs button").forEach(function(b) {
            b.className = "tab-link-muted";
          });
          fetchAndRenderPois();
        }
      });
    }

    // 2. Category Tabs
    var tabContainer = document.getElementById("categoryTabs");
    if (tabContainer) {
      tabContainer.addEventListener("click", function(e) {
        if (e.target.tagName === "BUTTON") {
          var btns = tabContainer.querySelectorAll("button");
          btns.forEach(function(b) { b.className = "tab-link-muted"; });
          e.target.className = "trip-plan-add-place-button-03 active";
          
          state.currentCategory = e.target.getAttribute("data-category") || "";
          state.currentKeyword = "";
          if (searchInput) searchInput.value = "";
          
          fetchAndRenderPois();
        }
      });
    }

    // 3. Map Sync
    kakao.maps.event.addListener(state.map, "idle", function () {
      if (state.idleTimer) clearTimeout(state.idleTimer);
      state.idleTimer = setTimeout(fetchAndRenderPois, 300);
    });

    // 4. Zoom & Location Controls
    var btnZoomIn = document.getElementById("btnZoomIn");
    var btnZoomOut = document.getElementById("btnZoomOut");
    if (btnZoomIn) {
      btnZoomIn.onclick = function () { state.map.setLevel(state.map.getLevel() - 1); };
    }
    if (btnZoomOut) {
      btnZoomOut.onclick = function () { state.map.setLevel(state.map.getLevel() + 1); };
    }

    var btnMyLocation = document.getElementById("btnMyLocation");
    if (btnMyLocation) {
      btnMyLocation.onclick = function () {
        if (navigator.geolocation) {
          navigator.geolocation.getCurrentPosition(function (position) {
            var lat = position.coords.latitude;
            var lng = position.coords.longitude;
            state.map.panTo(new kakao.maps.LatLng(lat, lng));
            // Trigger fetch implicitly via 'idle'
          });
        }
      };
    }
  }

  // Initialization
  function initMap() {
    var mapContainer = document.getElementById("tripMap");
    if (!mapContainer) return;

    kakao.maps.load(function () {
      var defaultCenter = new kakao.maps.LatLng(33.4996, 126.5312);
      var defaultLevel = 8;

      state.map = new kakao.maps.Map(mapContainer, {
        center: defaultCenter,
        level: defaultLevel
      });
      state.places = new kakao.maps.services.Places(state.map);

      // Try to find region from title
      var titleEl = document.querySelector(".trip-plan-add-place-h2-01");
      if (titleEl && window.TRAVEL_PLATFORM) {
        var titleText = titleEl.textContent;
        var regionKeywords = window.TRAVEL_PLATFORM.REGION_KEYWORDS;
        var regionViews = window.TRAVEL_PLATFORM.REGION_VIEW;

        for (var regionKey in regionKeywords) {
          var keywords = regionKeywords[regionKey];
          var match = keywords.some(function (k) {
            return titleText.indexOf(k) >= 0;
          });

          if (match && regionViews[regionKey]) {
            var view = regionViews[regionKey];
            state.currentRegionKey = regionKey; // Store to enforce boundary limit
            state.map.setCenter(new kakao.maps.LatLng(view.lat, view.lng));
            state.map.setLevel(view.level);
            break;
          }
        }
      }

      bindEvents();
      fetchAndRenderPois(); // Initial Load
    });
  }

  if (typeof kakao !== "undefined" && kakao.maps) {
    initMap();
  } else {
    var tries = 0;
    var interval = setInterval(function () {
      if (typeof kakao !== "undefined" && kakao.maps) {
        clearInterval(interval);
        initMap();
      }
      if (++tries > 50) clearInterval(interval);
    }, 100);
  }
})();