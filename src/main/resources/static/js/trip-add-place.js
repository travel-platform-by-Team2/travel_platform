(function () {
  "use strict";

  var pageRoot = document.getElementById("tripPlacePage");
  if (!pageRoot) return;

  var state = {
    map: null,
    places: null,
    hotelClusterer: null,
    attrClusterer: null,
    overlays: [],
    markers: [],
    items: [],
    selectedPlaces: [],
    currentRegionKey: null,
    regionLabel: pageRoot.dataset.region || "",
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
    
    // map-detail.mustache 등에서 참조된 고품질 이미지 풀
    var hotelImages = [
        "https://lh3.googleusercontent.com/aida-public/AB6AXuC-JAUxidY-8eAcp_IDpWDkgrUGMXIGAlPy1BJwza3o8VSKr55RAv9mLt9GMN6aPzSqUaGSwFoL74EWZKG0EtV9HzVx5i_KbnAraqlodDx-GlN53CzIIcHWdA6Ookubfk19r7qR7DWsq3neb-etxuMxfQnAjmDHvE9QnncbJ2VNnStRshVdPe9zBeLMiBxAsuQIkYIiMzR6gRiw9KI84TWahGVSWjxkaS9308j5YVRMy6BQS1lwXSpj13SwdZmAlCmkkIwlpo1W9rA",
        "https://lh3.googleusercontent.com/aida-public/AB6AXuC5HkQwgzyLa00qm4kJK8AntO2ohMwlggRjDKOLbAr-VuVZaW85t84Qg56mlYmwmNjAv9juzqcKjyymqjhbKRJF4YWTS6zPuGQs47NhHldI1a9kOEUWW9WRNwQxKJ3oPyAHrgmudLH2GDtU4wxGE34BYVcq161Lf1rmKY4x98zy3NaqZT9QJb8Maldzovyq4zCkxfgqycRYpIJ3RgFqvtONxkOrnspgSgW8rjDHtNC_mVCd_QtL2VID4YErblfOFPAaJDDo7-sPyok",
        "https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=600&h=400&fit=crop",
        "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600&h=400&fit=crop"
    ];
    
    var attrImages = [
        "https://lh3.googleusercontent.com/aida-public/AB6AXuCVgQdNeNWAKvF_XEmrvmeXZhDlZEkrsaT5L1HswoFEJqscQS4AFi3qZHQi23dm2ZJkqGyL2eLQGHruSaxy8w-VVD7ATptLBCOHPwnmn6ADoO2ih0ChSHfWp5oL88gqhBFdXzJH5GvXK6Jz4nvszn01k9ihI4LeOPRDKQxIqxS62edpm1n2UKy6XNlmYASt0mJo-Ehk7FmD9Ho8BjPIGseLR12D9G3rz118NEFOwx452_ocUKPMXRl_zfaNbMVYZPpzvET2mFTbii8",
        "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600&h=400&fit=crop",
        "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=600&h=400&fit=crop",
        "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=600&h=400&fit=crop"
    ];

    var pool = isHotel ? hotelImages : attrImages;
    return pool[seed % pool.length];
  }

  function normalizePlace(p, categoryCode) {
    var type = (categoryCode === "AD5" || p.category_group_code === "AD5") ? "hotel" : "attraction";
    return {
      id: p.id,
      name: p.place_name,
      address: p.road_address_name || p.address_name,
      lat: parseFloat(p.y), lng: parseFloat(p.x),
      type: type, placeUrl: p.place_url || "", imgUrl: p.image_url || null,
      rating: (4.0 + (hashText(p.id) % 10) / 10).toFixed(1), // 일관된 랜덤 평점
      reviewCount: Math.floor((hashText(p.id) % 1000) + 10)
    };
  }

  function isSelected(itemId) {
    return state.selectedPlaces.some(function (item) { return String(item.id) === String(itemId); });
  }

  function unwrapRespBody(data) {
    if (data && typeof data === "object" && Object.prototype.hasOwnProperty.call(data, "body")) {
      return data.body;
    }
    return data;
  }

  function normalizeNameKey(name) {
    return String(name || "").replace(/\s+/g, "").toLowerCase();
  }

  async function fetchPlaceImageFromServer(item) {
    var cacheKey = (item.placeUrl || "") + "|" + normalizeNameKey(item.name);
    if (state.imageCache && cacheKey in state.imageCache) {
      return state.imageCache[cacheKey];
    }
    if (!state.imageCache) state.imageCache = {};

    try {
      var params = new URLSearchParams();
      if (item.placeUrl) params.set("placeUrl", item.placeUrl);
      if (item.name) params.set("name", item.name);
      if (item.address) params.set("address", item.address);

      var response = await fetch("/api/bookings/place-image?" + params.toString(), {
        method: "GET",
        headers: { Accept: "application/json" }
      });

      if (!response.ok) return "";

      var data = unwrapRespBody(await response.json());
      var imageUrl = data && typeof data.imageUrl === "string" ? data.imageUrl : "";
      state.imageCache[cacheKey] = imageUrl;
      return imageUrl;
    } catch (error) {
      return "";
    }
  }

  async function hydrateListImages() {
    var images = document.querySelectorAll("[data-place-image]");
    await Promise.all(
      Array.from(images).map(async function (imgEl) {
        var id = imgEl.getAttribute("data-id");
        var item = state.items.find(function(v) { return String(v.id) === String(id); });
        if (!item) return;
        
        var serverImage = await fetchPlaceImageFromServer(item);
        if (serverImage) {
          imgEl.src = serverImage;
          item.imgUrl = serverImage; // 실제 이미지 URL을 데이터 객체에 업데이트
          
          // 선택된 목록에 있는 이미지와 데이터도 업데이트
          var selectedItem = state.selectedPlaces.find(function(v) { return String(v.id) === String(id); });
          if (selectedItem) selectedItem.imgUrl = serverImage;

          var selectedImg = document.querySelector("#selected-img-" + id);
          if (selectedImg) selectedImg.src = serverImage;
        }
      })
    );
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
      var fallbackImg = createFallbackImageDataUri(item);
      
      return '' +
        '<div class="place-option-card" id="place-card-' + item.id + '" style="display:flex; gap:1rem; padding:1rem; border:1px solid #e2e8f0; border-radius:1rem; background:#fff; margin-bottom:1rem; position:relative;">' +
        '  <div style="width:80px; height:80px; flex-shrink:0; overflow:hidden; border-radius:0.75rem;">' +
        '    <img src="' + fallbackImg + '" data-place-image data-id="' + item.id + '" style="width:100%; height:100%; object-fit:cover;"/>' +
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
    
    // 비동기적으로 실제 이미지 로드
    hydrateListImages();
  }

  function renderSelectedPlaces() {
    var container = document.getElementById("selectedPlaceList");
    if (!container) return;

    if (!state.selectedPlaces.length) {
      container.innerHTML = '<div class="empty-state" style="font-size:0.8rem; color:#94a3b8;">장소를 추가해 보세요.</div>';
      document.getElementById("selectedPlaceCount").textContent = "0";
      return;
    }

    document.getElementById("selectedPlaceCount").textContent = state.selectedPlaces.length;
    container.innerHTML = state.selectedPlaces.map(function (item) {
      var fallbackImg = createFallbackImageDataUri(item);
      // 캐시된 이미지가 있으면 사용
      var cachedImg = (state.imageCache && state.imageCache[(item.placeUrl || "") + "|" + normalizeNameKey(item.name)]) || fallbackImg;
      
      return '' +
        '<div class="selected-item-card">' +
        '  <div style="position:relative;">' +
        '    <img src="' + cachedImg + '" id="selected-img-' + item.id + '" alt="Selected"/>' +
        '    <button class="remove-btn" data-action="remove-place" data-id="' + item.id + '">✕</button>' +
        '  </div>' +
        '  <span class="item-name">' + item.name + '</span>' +
        '</div>';
    }).join("");

    container.scrollLeft = container.scrollWidth;
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

  function searchByCategory(cat) {
    return new Promise(function(resolve) {
      state.places.categorySearch(cat, function(data, status) {
        resolve(status === kakao.maps.services.Status.OK ? data.map(function(p) { return normalizePlace(p, cat); }) : []);
      }, { useMapBounds: true });
    });
  }

  async function fetchAndRenderPois() {
    if (state.loading) return;
    state.loading = true;
    try {
      var results = [];
      if (state.currentKeyword) {
        results = await new Promise(function(resolve) {
          state.places.keywordSearch(state.currentKeyword, function(data, status) {
            resolve(status === kakao.maps.services.Status.OK ? data.map(function(p) { return normalizePlace(p, p.category_group_code); }) : []);
          }, { useMapBounds: true });
        });
      } else if (state.currentCategory) {
        results = await searchByCategory(state.currentCategory);
      } else {
        // "전체"인 경우 명소(AT4)와 숙소(AD5) 병렬 검색
        var [resAt4, resAd5] = await Promise.all([
          searchByCategory("AT4"),
          searchByCategory("AD5")
        ]);
        results = resAt4.concat(resAd5);
      }
      state.items = results;
      
      // 지역 필터링: 설정된 지역명(예: 부산, 서울)이 주소에 포함된 경우만 남김
      if (state.regionLabel && state.regionLabel !== "전국" && state.regionLabel !== "지역 정보 없음") {
        state.items = state.items.filter(function(item) {
          // 주소(address)에 지역명(regionLabel)이 포함되어 있는지 확인
          return item.address && item.address.indexOf(state.regionLabel) !== -1;
        });
      }

      renderList();
      renderOverlays();
    } finally { state.loading = false; }
  }

  function renderOverlays() {
    state.overlays.forEach(function(o) { o.setMap(null); });
    state.overlays = [];
    
    // 줌 레벨에 따라 동적으로 gridSize 계산
    var zoomLevel = state.map.getLevel();
    var dynamicGridSize = 60;
    if (zoomLevel >= 10) dynamicGridSize = 120;
    else if (zoomLevel >= 8) dynamicGridSize = 90;
    else if (zoomLevel >= 6) dynamicGridSize = 70;

    // 클러스터러 초기화/재설정 함수
    function createClusterer(color) {
      return new kakao.maps.MarkerClusterer({
        map: state.map,
        averageCenter: true,
        minLevel: 5,
        gridSize: dynamicGridSize,
        styles: [{
          width: '40px', height: '40px',
          background: color,
          borderRadius: '20px',
          color: '#fff',
          textAlign: 'center',
          fontWeight: '800',
          lineHeight: '41px',
          fontSize: '14px',
          border: '2px solid #fff',
          boxShadow: '0 2px 6px rgba(0,0,0,0.3)'
        }]
      });
    }

    // 클러스터러가 없거나 gridSize가 변경되었다면 재설정
    if (!state.hotelClusterer || state.lastGridSize !== dynamicGridSize) {
      if (state.hotelClusterer) state.hotelClusterer.clear();
      if (state.attrClusterer) state.attrClusterer.clear();
      
      state.hotelClusterer = createClusterer('rgba(37, 99, 235, 0.9)'); // 숙소: Blue
      state.attrClusterer = createClusterer('rgba(234, 88, 12, 0.9)');  // 명소: Orange
      state.lastGridSize = dynamicGridSize;
    } else {
      state.hotelClusterer.clear();
      state.attrClusterer.clear();
    }
    
    var hotelMarkers = [];
    var attrMarkers = [];

    state.items.forEach(function(item) {
      var node = document.createElement("div");
      node.className = "stay-marker stay-marker--" + (item.type === "hotel" ? "hotel" : "attraction");
      node.innerHTML = '<div class="stay-marker__pin"><span class="icon-ms-16">' + (item.type === "hotel" ? "hotel" : "attractions") + '</span></div><div class="stay-marker__label">' + item.name + '</div>';
      
      var position = new kakao.maps.LatLng(item.lat, item.lng);
      var overlay = new kakao.maps.CustomOverlay({ position: position, content: node, yAnchor: 1 });
      overlay.setMap(state.map);
      state.overlays.push(overlay);
      
      node.onclick = function() { addSelectedPlace(item.id); };

      // 투명 마커 생성
      var marker = new kakao.maps.Marker({
        position: position,
        image: new kakao.maps.MarkerImage(
          'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7',
          new kakao.maps.Size(1, 1)
        )
      });

      if (item.type === "hotel") {
        hotelMarkers.push(marker);
      } else {
        attrMarkers.push(marker);
      }
    });

    if (state.hotelClusterer) state.hotelClusterer.addMarkers(hotelMarkers);
    if (state.attrClusterer) state.attrClusterer.addMarkers(attrMarkers);
  }

  function initMap() {
    kakao.maps.load(function () {
      state.map = new kakao.maps.Map(document.getElementById("tripMap"), { center: new kakao.maps.LatLng(33.4996, 126.5312), level: 8, scrollwheel: true });
      state.places = new kakao.maps.services.Places(state.map);
      
      // 초기 렌더링을 호출하여 클러스터러 생성
      renderOverlays();

      if (state.regionLabel) {
        state.places.keywordSearch(state.regionLabel, function(data, status) {
          if (status === kakao.maps.services.Status.OK && data.length > 0) {
            var first = data[0];
            state.map.setCenter(new kakao.maps.LatLng(first.y, first.x));
          }
        });
      }

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
      
      document.getElementById("saveSelectedPlaces").onclick = async function() {
        if (state.saving || state.selectedPlaces.length === 0) {
          if (state.selectedPlaces.length === 0) alert("장소를 하나 이상 선택해주세요.");
          return;
        }

        state.saving = true;
        this.disabled = true;
        this.textContent = "저장 중...";

        try {
          var payload = {
            tripDay: state.currentDay,
            places: state.selectedPlaces.map(function(p) {
              return {
                placeName: p.name,
                address: p.address,
                latitude: p.lat,
                longitude: p.lng,
                placeUrl: p.placeUrl,
                imgUrl: p.imgUrl,
                type: p.type
              };
            })
          };

          var response = await fetch(state.saveUrl, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
          });

          if (response.ok) {
            location.href = state.detailUrl;
          } else {
            alert("저장에 실패했습니다.");
            state.saving = false;
            this.disabled = false;
            this.textContent = "완료";
          }
        } catch (e) {
          console.error(e);
          alert("에러가 발생했습니다.");
          state.saving = false;
          this.disabled = false;
          this.textContent = "완료";
        }
      };

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
