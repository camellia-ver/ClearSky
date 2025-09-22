let map;
let mapInitialized = false;
let marker = null;

document.addEventListener("DOMContentLoaded", () => {
    const locationInput = document.getElementById('location-input');
    const latInput = document.getElementById('lat-input');
    const lngInput = document.getElementById('lng-input');
    const openMapBtn = document.getElementById('openMapBtn');
    const mapModalEl = document.getElementById('mapModal');
    const getCurrentLocationBtn = document.getElementById('getCurrentLocationBtn');

    const autocompleteList = document.getElementById('autocomplete-list');

    // 🔹 1. 현재 위치 자동 적용
    getCurrentLocationBtn.addEventListener('click', getCurrentLocation);

    // 🔹 2. 지도 모달 초기화
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
            focusOnInputLocation(); // 모달이 다시 열릴 때도 포커스
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

    // 🔹 3. 지도 초기화
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
                    const modal = bootstrap.Modal.getInstance(mapModalEl);
                    modal.hide();
                }
            });
        });
    }

    // 🔹 4. 마커와 입력 필드 업데이트
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
    }

    // 🔹 5. 현재 위치 가져오기
    function getCurrentLocation() {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                (pos) => {
                    const lat = pos.coords.latitude;
                    const lng = pos.coords.longitude;
                    performGeolocationAction(lat, lng);
                },
                (err) => {
                    alert("위치 정보를 가져올 수 없습니다.");
                    console.error(err);
                },
                { enableHighAccuracy: true, timeout: 5000, maximumAge: 0 }
            );
        } else {
            alert("이 브라우저에서는 Geolocation이 지원되지 않습니다.");
        }
    }

    // 🔹 6. 좌표 → 주소 변환 + 입력 필드/마커 업데이트
    function performGeolocationAction(lat, lng) {
        setLocation(lat, lng);

        // Kakao Maps SDK가 로드되지 않았다면 로드 후 재호출
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
                alert("주소 변환에 실패했지만, 좌표는 저장되었습니다.");
            }
        });
    }

    /**
     * 연속 호출을 지연시켜 마지막 호출만 실행되도록 하는 디바운스 함수
     * @param {Function} func - 디바운스할 함수
     * @param {number} delay - 지연 시간 (밀리초)
     */
    function debounce(func, delay){
        let timeoutId;

        return function(...args){
            // 이전 타이머가 있다면 취소
            if (timeoutId){
                clearTimeout(timeoutId);
            }

            // 새로운 타이머 설정
            timeoutId = setTimeout(() => {
               func.apply(this, args); 
            }, delay);
        }
    }

    // 🔹 7. 디바운스된 자동완성 검색 함수
    const debouncedAutocomplete = debounce(searchAutocomplete, 300);

    locationInput.addEventListener('keyup', (e) => {
        const query = locationInput.value.trim();

        if (query.length > 1) {
            // 2글자 이상일 때만 검색 시작
            debouncedAutocomplete(query);
        } else {
            // 입력이 짧거나 없을 경우 목록을 비웁니다.
            autocompleteList.innerHTML = '';
            autocompleteList.style.display = 'none';
        }
    });

    // 🔹 8. 서버에 자동완성 검색 요청 (여기서 서버 API를 호출합니다)
    function searchAutocomplete(query) {
        // 실제 서버 엔드포인트로 변경해야 합니다.
        const url = `/api/locations/autocomplete?query=${encodeURIComponent(query)}`;

        fetch(url)
            .then(response => {
                if (!response.ok) {
                    throw new Error('네트워크 응답이 올바르지 않습니다.');
                }
                return response.json();
            })
            .then(results => {
                displayAutocompleteResults(results);
            })
            .catch(error => {
                console.error('자동완성 검색 오류:', error);
                // 오류 발생 시 목록을 숨깁니다.
                autocompleteList.innerHTML = '';
                autocompleteList.style.display = 'none';
            });
    }

    // 🔹 9. 검색 결과를 화면에 표시
    function displayAutocompleteResults(results) {
        const autocompleteList = document.getElementById('autocomplete-list');
        autocompleteList.innerHTML = ''; // 기존 목록 초기화

        if (results.length === 0) {
            autocompleteList.style.display = 'none';
            return;
        }

        results.forEach(item => {
            const listItem = document.createElement('li');
            listItem.classList.add('list-group-item', 'list-group-item-action');
            listItem.textContent = item.full_address; // 서버에서 받은 전체 주소

            // 클릭 이벤트 리스너 추가
            listItem.addEventListener('click', () => {
                selectAutocompleteItem(item);
            });

            autocompleteList.appendChild(listItem);
        });

        autocompleteList.style.display = 'block'; // 목록을 보여줍니다.
    }

    // 🔹 10. 자동완성 항목 선택 시 처리
    function selectAutocompleteItem(item) {
        const locationInput = document.getElementById('location-input');
        const latInput = document.getElementById('lat-input');
        const lngInput = document.getElementById('lng-input');
        const autocompleteList = document.getElementById('autocomplete-list');

        // 폼 입력 필드 업데이트
        locationInput.value = item.full_address;
        latInput.value = item.lat;
        lngInput.value = item.lng;

        // 자동완성 목록 숨기기
        autocompleteList.innerHTML = '';
        autocompleteList.style.display = 'none';

        // (선택 사항) 사용자가 선택 후 바로 검색을 원하면 여기에서 폼을 제출할 수 있습니다.
        // document.getElementById('search-form').submit();
    }
});
