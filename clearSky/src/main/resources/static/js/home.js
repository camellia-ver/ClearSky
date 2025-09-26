let map;
let mapInitialized = false;
let marker = null;
let isLocationSelected = false; // 위치가 유효하게 선택되었는지 추적
let currentFocus = -1; // 🔹 자동완성 포커스 관리 전역 변수

document.addEventListener("DOMContentLoaded", () => {
    const locationInput = document.getElementById('location-input');
    const latInput = document.getElementById('lat-input');
    const lngInput = document.getElementById('lng-input');
    const openMapBtn = document.getElementById('openMapBtn');
    const mapModalEl = document.getElementById('mapModal');
    const getCurrentLocationBtn = document.getElementById('getCurrentLocationBtn');
    const autocompleteList = document.getElementById('autocomplete-list');
    const searchButton = document.querySelector('#search-form button[type="submit"]');

    // 초기 버튼 상태: 비활성화
    searchButton.disabled = true;

    // 🔹 현재 위치 버튼
    getCurrentLocationBtn.addEventListener('click', getCurrentLocation);

    // 🔹 입력 이벤트
    locationInput.addEventListener('input', () => {
        if (isLocationSelected) {
            isLocationSelected = false;
            searchButton.disabled = true;
        }

        currentFocus = -1; // 입력 바뀔 때 포커스 초기화

        const query = locationInput.value.trim();
        if (query.length > 1) {
            debouncedAutocomplete(query);
        } else {
            autocompleteList.innerHTML = '';
            autocompleteList.style.display = 'none';
        }
    });

    // 🔹 키보드 이벤트
    locationInput.addEventListener('keydown', (e) => {
        const items = autocompleteList.querySelectorAll('li');
        const activeClass = 'autocomplete-active';

        if (e.key === "ArrowDown") {
            e.preventDefault();
            if (items.length > 0) {
                currentFocus = (currentFocus + 1) % items.length;
                addActive(items, currentFocus, activeClass);
            }
        } else if (e.key === "ArrowUp") {
            e.preventDefault();
            if (items.length > 0) {
                currentFocus = (currentFocus - 1 + items.length) % items.length;
                addActive(items, currentFocus, activeClass);
            }
        } else if (e.key === "Enter") {
            if (autocompleteList.style.display === 'block' && currentFocus > -1) {
                e.preventDefault();
                items[currentFocus].click(); // 선택
            }
        }
    });

    function addActive(items, index, activeClass) {
        removeActive(items, activeClass);
        if (index >= 0 && index < items.length) {
            items[index].classList.add(activeClass);
            items[index].scrollIntoView({ block: "nearest" });
        }
    }

    function removeActive(items, activeClass) {
        items.forEach(item => item.classList.remove(activeClass));
    }

    // 🔹 지도 모달 초기화
    mapModalEl.addEventListener('shown.bs.modal', () => {
        if (!mapInitialized) {
            if (typeof kakao === 'undefined') {
                console.error("Kakao Maps SDK 로드 실패");
                return;
            }
            kakao.maps.load(() => {
                initMap();
                mapInitialized = true;
                focusOnInputLocation();
            });
        } else {
            kakao.maps.event.trigger(map, 'resize');
            focusOnInputLocation();
        }
    });

    function focusOnInputLocation() {
        const lat = parseFloat(latInput.value);
        const lng = parseFloat(lngInput.value);

        if (!isNaN(lat) && !isNaN(lng)) {
            const latLng = new kakao.maps.LatLng(lat, lng);
            map.setCenter(latLng);

            if (!marker) {
                marker = new kakao.maps.Marker({ map: map, position: latLng });
            } else {
                marker.setPosition(latLng);
                marker.setMap(map);
            }
        }
    }

    openMapBtn.addEventListener('click', () => {
        const modal = new bootstrap.Modal(mapModalEl);
        modal.show();
    });

    // 🔹 지도 초기화
    function initMap() {
        const container = document.getElementById('modal-map');
        const defaultCenter = new kakao.maps.LatLng(37.5665, 126.9780);

        map = new kakao.maps.Map(container, { center: defaultCenter, level: 8 });
        map.addControl(new kakao.maps.ZoomControl(), kakao.maps.ControlPosition.RIGHT);

        const geocoder = new kakao.maps.services.Geocoder();

        kakao.maps.event.addListener(map, 'click', (mouseEvent) => {
            const lat = mouseEvent.latLng.getLat();
            const lng = mouseEvent.latLng.getLng();
            setLocation(lat, lng);

            geocoder.coord2Address(lng, lat, (result, status) => {
                if (status === kakao.maps.services.Status.OK && result[0]) {
                    locationInput.value = result[0].address.address_name;
                    isLocationSelected = true;
                    searchButton.disabled = false;

                    const modal = bootstrap.Modal.getInstance(mapModalEl);
                    modal.hide();
                }
            });
        });
    }

    // 🔹 마커/입력 필드 업데이트
    function setLocation(lat, lng) {
        latInput.value = lat;
        lngInput.value = lng;

        if (!mapInitialized) return;

        const latLng = new kakao.maps.LatLng(lat, lng);
        if (!marker) {
            marker = new kakao.maps.Marker({ map: map, position: latLng });
        } else {
            marker.setPosition(latLng);
            marker.setMap(map);
        }
        map.setCenter(latLng);

        isLocationSelected = true;
        searchButton.disabled = false;
    }

    // 🔹 현재 위치 가져오기
    function getCurrentLocation() {
        if (navigator.geolocation) {
            searchButton.disabled = true;
            locationInput.placeholder = "현재 위치 찾는 중...";

            navigator.geolocation.getCurrentPosition(
                (pos) => {
                    const lat = pos.coords.latitude;
                    const lng = pos.coords.longitude;
                    performGeolocationAction(lat, lng);
                },
                (err) => {
                    alert("위치 정보를 가져올 수 없습니다.");
                    console.error(err);
                    searchButton.disabled = true;
                    locationInput.placeholder = "지역 검색"; 
                },
                { enableHighAccuracy: true, timeout: 5000, maximumAge: 0 }
            );
        } else {
            alert("이 브라우저에서는 Geolocation이 지원되지 않습니다.");
            searchButton.disabled = true;
        }
    }

    // 🔹 좌표 → 주소 변환
    function performGeolocationAction(lat, lng) {
        setLocation(lat, lng);
        locationInput.placeholder = "지역 검색";

        if (typeof kakao === 'undefined' || !kakao.maps.services) {
            kakao.maps.load(() => {
                performGeolocationAction(lat, lng);
            });
            return;
        }

        const geocoder = new kakao.maps.services.Geocoder();
        geocoder.coord2Address(lng, lat, (result, status) => {
            if (status === kakao.maps.services.Status.OK && result[0]) {
                locationInput.value = result[0].address.address_name;
                alert(`현재 위치가 설정되었습니다: ${result[0].address.address_name}`);
            } else {
                locationInput.value = `위도: ${lat.toFixed(4)}, 경도: ${lng.toFixed(4)}`;
                alert("주소 변환에 실패했지만, 좌표는 저장되었습니다. 검색이 가능합니다.");
            }
            isLocationSelected = true;
            searchButton.disabled = false;
        });
    }

    // 🔹 디바운스
    function debounce(func, delay) {
        let timeoutId;
        return function(...args) {
            if (timeoutId) clearTimeout(timeoutId);
            timeoutId = setTimeout(() => func.apply(this, args), delay);
        }
    }

    const debouncedAutocomplete = debounce(searchAutocomplete, 300);

    // 🔹 자동완성 API 호출
    function searchAutocomplete(query) {
        const url = `/api/locations/autocomplete?query=${encodeURIComponent(query)}`;

        fetch(url)
            .then(response => {
                if (!response.ok) throw new Error('네트워크 응답이 올바르지 않습니다.');
                return response.text();
            })
            .then(xmlString => {
                const results = parseXmlToLocations(xmlString);
                displayAutocompleteResults(results);
            })
            .catch(error => {
                console.error('자동완성 검색 오류:', error);
                autocompleteList.innerHTML = '';
                autocompleteList.style.display = 'none';
            });
    }

    /**
     * XML 문자열을 LocationDTO 객체 배열로 파싱하는 함수
     * @param {string} xmlString - 서버에서 받은 XML 응답 문자열
     * @returns {Array<Object>} - { full_address, lat, lng } 형태의 객체 배열
     */
    function parseXmlToLocations(xmlString) {
        const parser = new DOMParser();
        const xmlDoc = parser.parseFromString(xmlString, "text/xml");
        const locationNodes = xmlDoc.getElementsByTagName('item');
        const locations = [];

        // 각 <location> 노드를 순회하며 데이터를 추출합니다.
        for (let i = 0; i < locationNodes.length; i++) {
            const node = locationNodes[i];

            // 태그 이름으로 내부 데이터를 추출합니다.
            const fullAddressNode = node.getElementsByTagName('full_address')[0];
            const latNode = node.getElementsByTagName('lat')[0];
            const lngNode = node.getElementsByTagName('lng')[0];

            // 노드가 존재하고 텍스트 내용이 있을 때만 데이터를 추가합니다.
            if (fullAddressNode && latNode && lngNode) {
                locations.push({
                    full_address: fullAddressNode.textContent,
                    lat: parseFloat(latNode.textContent), // 숫자형으로 변환
                    lng: parseFloat(lngNode.textContent)  // 숫자형으로 변환
                });
            }
        }

        return locations;
    }

    // 🔹 자동완성 목록 표시
    function displayAutocompleteResults(results) {
        autocompleteList.innerHTML = ''; 

        if (results.length === 0) {
            autocompleteList.style.display = 'none';
            return;
        }

        results.forEach(item => {
            const listItem = document.createElement('li');
            listItem.classList.add('list-group-item', 'list-group-item-action');
            listItem.textContent = item.full_address; 

            listItem.addEventListener('click', () => {
                selectAutocompleteItem(item);
            });

            autocompleteList.appendChild(listItem);
        });

        autocompleteList.style.display = 'block'; 
    }

    // 🔹 자동완성 항목 선택
    function selectAutocompleteItem(item) {
        locationInput.value = item.full_address;
        latInput.value = item.lat;
        lngInput.value = item.lng;

        autocompleteList.innerHTML = '';
        autocompleteList.style.display = 'none';

        isLocationSelected = true;
        searchButton.disabled = false;
    }

    // 🔹 폼 제출 방지
    const searchForm = document.getElementById('search-form');
    searchForm.addEventListener('submit', (e) => {
        if (!isLocationSelected) {
            e.preventDefault();
            alert("자동 완성 목록, 지도 또는 현재 위치를 통해 유효한 위치를 선택해야 검색할 수 있습니다.");
            searchButton.disabled = true;
        }
    });
});
