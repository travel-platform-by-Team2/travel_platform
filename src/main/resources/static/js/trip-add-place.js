(function () {
  "use strict";

  var pageRoot = document.getElementById("tripPlacePage");
  if (!pageRoot) return;

  var state = {
    map: null,
    places: null,
    overlays: [],
    items: [],
    selectedPlaces: [],
    currentRegionKey: null,
    currentCategory: "",
    currentKeyword: "",
    idleTimer: null,
    loading: false,
    saving: false,
    saveUrl: pageRoot.dataset.saveUrl || "",
    detailUrl: pageRoot.dataset.detailUrl || "/trip",
    existingCount: parseInt(pageRoot.dataset.existingCount || "0", 10) || 0,
    currentDay: 1
  };

  function initDayTabs() {
    var container = document.getElementById("dayTabContainer");
    var pEl = document.querySelector(".trip-plan-add-place-div-01 p");
    if (!container || !pEl) return;

    var match = pEl.textContent.match(/(\d+)일\)/);
    var totalDays = match ? parseInt(match[1], 10) : 1;

    container.innerHTML = "";
    for (var i = 1; i <= totalDays; i++) {
      var btn = document.createElement("button");
      btn.type = "button";
      btn.textContent = i + "일차";
      btn.className = (i === state.currentDay) ? "day-tab-btn active" : "day-tab-btn";
      (function(day) {
        btn.onclick = function() {
          state.currentDay = day;
          document.querySelectorAll("#dayTabContainer button").forEach(function(b, idx) {
            b.className = (idx + 1 === state.currentDay) ? "day-tab-btn active" : "day-tab-btn";
          });
        };
      })(i);
      container.appendChild(btn);
    }
  }

  function hashText(str) {
    var hash = 0;
    for (var i = 0; i < str.length; i++) { hash = (hash << 5) - hash + str.charCodeAt(i); hash |= 0; }
    return Math.abs(hash);
  }

  function createFallbackImageDataUri(item) {
    var seed = hashText(item.name || "place");
    var isHotel = item.type === "hotel" || item.category_group_code === "AD5";
    var images = isHotel ? [
        "https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=400&h=300&fit=crop",
        "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=400&h=300&fit=crop",
        "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=400&h=300&fit=crop"
    ] : [
        "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=400&h=300&fit=crop",
        "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=400&h=300&fit=crop",
        "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=400&h=300&fit=crop"
    ];
    return images[seed % images.length];
  }

  function normalizePlace(p, categoryCode) {
    var type = (categoryCode === "AD5" || p.category_group_code === "AD5") ? "hotel" : "attraction";
    return {
      id: p.id,
      name: p.place_name,
      address: p.road_address_name || p.address_name,
      lat: parseFloat(p.y), lng: parseFloat(p.x),
      type: type, placeUrl: p.place_url || "", imgUrl: p.image_url || null,
      rating: (4.0 + Math.random()).toFixed(1),
      reviewCount: Math.floor(Math.random() * 5000)
    };
  }

  function isSelected(itemId) {
    return state.selectedPlaces.some(function (item) { return String(item.id) === String(itemId); });
  }

  function renderList() {
    var container = document.getElementById("placeListContainer");
    if (!container) return;

    if (!state.items || state.items.length === 0) {
      container.innerHTML = '<div class="empty-state">검색 결과가 없습니다.</div>';
      return;
    }

    container.innerHTML = state.items.map(function (item) {
      var selected = isSelected(item.id);
      var typeLabel = item.type === "hotel" ? "숙소" : "명소";
      return '' +
        '<div class="place-option-card" id="place-card-' + item.id + '" style="display:flex; gap:1rem; padding:1rem; border:1px solid #e2e8f0; border-radius:1rem; background:#fff; margin-bottom:1rem; position:relative;">' +
        '  <div style="width:80px; height:80px; flex-shrink:0; overflow:hidden; border-radius:0.75rem;">' +
        '    <img src="' + (item.imgUrl || createFallbackImageDataUri(item)) + '" style="width:100%; height:100%; object-fit:cover;"/>' +
        '  </div>' +
        '  <div style="flex:1; min-width:0; display:flex; flex-direction:column; justify-content:center;">' +
        '    <div style="display:flex; justify-content:space-between; align-items:flex-start;">' +
        '      <h3 style="margin:0; font-size:0.95rem; font-weight:800; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; padding-right:30px;">' + item.name + '</h3>' +
        '      <span style="font-size:0.7rem; padding:2px 6px; background:#f1f5f9; border-radius:4px; color:#64748b;">' + typeLabel + '</span>' +
        '    </div>' +
        '    <p style="margin:0.25rem 0; font-size:0.75rem; color:#64748b; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">' + item.address + '</p>' +
        '    <div style="display:flex; align-items:center; gap:4px; font-size:0.75rem; color:#f59e0b;">' +
        '      <span class="icon-ms-14-yellow-fill">star</span>' +
        '      <span style="color:#1e293b; font-weight:700;">' + item.rating + '</span>' +
        '      <span style="color:#94a3b8;">(' + item.reviewCount.toLocaleString() + ')</span>' +
        '    </div>' +
        '  </div>' +
        '  <button class="btn-round-add" data-action="add-place" data-id="' + item.id + '"' + (selected ? ' disabled' : '') + ' style="position:absolute; bottom:1rem; right:1rem; width:32px; height:32px; border-radius:50%; border:0; background:' + (selected ? '#cbd5e1' : '#2563eb') + '; color:#fff; cursor:pointer; display:flex; align-items:center; justify-content:center;">' +
        '    <span class="icon-ms-base">' + (selected ? 'check' : 'add') + '</span>' +
        '  </button>' +
        '</div>';
    }).join("");
  }

  function renderSelectedPlaces() {
    var container = document.getElementById("selectedPlaceList");
    if (!container) return;

    if (!state.selectedPlaces.length) {
      container.innerHTML = '<div class="empty-state" style="font-size:0.8rem; color:#94a3b8;">장소를 추가해 보세요.</div>';
      document.getElementById("selectedPlaceCount").textContent = "0";
      renderList();
      return;
    }

    document.getElementById("selectedPlaceCount").textContent = state.selectedPlaces.length;
    container.innerHTML = state.selectedPlaces.map(function (item) {
      return '' +
        '<div class="selected-item-card">' +
        '  <div style="position:relative;">' +
        '    <img src="' + (item.imgUrl || createFallbackImageDataUri(item)) + '" alt="Selected"/>' +
        '    <button class="remove-btn" data-action="remove-place" data-id="' + item.id + '">✕</button>' +
        '  </div>' +
        '  <span class="item-name">' + item.name + '</span>' +
        '</div>';
    }).join("");

    container.scrollLeft = container.scrollWidth;
    renderList();
  }

  function addSelectedPlace(itemId) {
    if (isSelected(itemId)) return;
    var item = state.items.find(function (v) { return String(v.id) === String(itemId); });
    if (!item) return;
    state.selectedPlaces.push(item);
    renderSelectedPlaces();
  }

  function removeSelectedPlace(itemId) {
    state.selectedPlaces = state.selectedPlaces.filter(function (v) { return String(v.id) !== String(itemId); });
    renderSelectedPlaces();
  }

  async function fetchAndRenderPois() {
    if (state.loading) return;
    state.loading = true;
    try {
      var results = [];
      var cat = state.currentCategory || "AT4";
      if (state.currentKeyword) {
        results = await new Promise(function(resolve) {
          state.places.keywordSearch(state.currentKeyword, function(data, status) {
            resolve(status === kakao.maps.services.Status.OK ? data.map(function(p) { return normalizePlace(p, p.category_group_code); }) : []);
          }, { useMapBounds: true });
        });
      } else {
        results = await new Promise(function(resolve) {
          state.places.categorySearch(cat, function(data, status) {
            resolve(status === kakao.maps.services.Status.OK ? data.map(function(p) { return normalizePlace(p, cat); }) : []);
          }, { useMapBounds: true });
        });
      }
      state.items = results;
      renderList();
      renderOverlays();
    } finally { state.loading = false; }
  }

  function renderOverlays() {
    state.overlays.forEach(function(o) { o.setMap(null); });
    state.overlays = [];
    state.items.forEach(function(item) {
      var node = document.createElement("div");
      node.className = "stay-marker stay-marker--" + (item.type === "hotel" ? "hotel" : "attraction");
      node.innerHTML = '<div class="stay-marker__pin"><span class="icon-ms-16">' + (item.type === "hotel" ? "hotel" : "attractions") + '</span></div><div class="stay-marker__label">' + item.name + '</div>';
      var overlay = new kakao.maps.CustomOverlay({ position: new kakao.maps.LatLng(item.lat, item.lng), content: node, yAnchor: 1 });
      overlay.setMap(state.map);
      state.overlays.push(overlay);
      node.onclick = function() { addSelectedPlace(item.id); };
    });
  }

  function initMap() {
    kakao.maps.load(function () {
      state.map = new kakao.maps.Map(document.getElementById("tripMap"), { center: new kakao.maps.LatLng(33.4996, 126.5312), level: 8, scrollwheel: true });
      state.places = new kakao.maps.services.Places(state.map);
      kakao.maps.event.addListener(state.map, "idle", function() { if (state.idleTimer) clearTimeout(state.idleTimer); state.idleTimer = setTimeout(fetchAndRenderPois, 200); });

      document.getElementById("placeListContainer").onclick = function(e) {
        var btn = e.target.closest("[data-action='add-place']");
        if (btn) addSelectedPlace(btn.dataset.id);
      };
      document.getElementById("selectedPlaceList").onclick = function(e) {
        var btn = e.target.closest("[data-action='remove-place']");
        if (btn) removeSelectedPlace(btn.dataset.id);
      };
      document.getElementById("categoryTabs").onclick = function(e) {
        if (e.target.tagName !== "BUTTON") return;
        this.querySelectorAll("button").forEach(function(b) { b.className = "category-tab-btn"; });
        e.target.className = "category-tab-btn active";
        state.currentCategory = e.target.dataset.category || "";
        fetchAndRenderPois();
      };
      
      document.getElementById("clearSelectedPlaces").onclick = function() { state.selectedPlaces = []; renderSelectedPlaces(); };
      document.getElementById("saveSelectedPlaces").onclick = function() { /* 저장 로직 */ };
      document.getElementById("cancelPlaceSelection").onclick = function() { location.href = state.detailUrl; };

      var searchInput = document.getElementById("placeSearchInput");
      if (searchInput) { searchInput.onkeypress = function(e) { if (e.key === "Enter") { state.currentKeyword = this.value.trim(); fetchAndRenderPois(); } }; }

      var mapEl = document.getElementById("tripMap");
      mapEl.addEventListener("wheel", function (e) {
        e.preventDefault();
        var rect = mapEl.getBoundingClientRect();
        var projection = state.map.getProjection();
        var anchor = projection.fromContainerPointToLatLng(new kakao.maps.Point(e.clientX - rect.left, e.clientY - rect.top));
        var next = e.deltaY > 0 ? state.map.getLevel() + 1 : state.map.getLevel() - 1;
        if (next >= 1 && next <= 14) state.map.setLevel(next, { anchor: anchor, animate: true });
      }, { passive: false });

      initDayTabs();
      fetchAndRenderPois();
    });
  }

  initMap();
})();
