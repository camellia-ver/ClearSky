let map;
let mapInitialized = false;
let marker = null;
// 🟢 핵심: 위치가 유효하게 선택되었는지 추적하는 상태 변수
let isLocationSelected = false; 

document.addEventListener("DOMContentLoaded", () => {
    const locationInput = document.getElementById('location-input');
    const latInput = document.getElementById('lat-input');
    const lngInput = document.getElementById('lng-input');
    const openMapBtn = document.getElementById('openMapBtn');
    const mapModalEl = document.getElementById('mapModal');
    const getCurrentLocationBtn = document.getElementById('getCurrentLocationBtn');
    const autocompleteList = document.getElementById('autocomplete-list');
    
    // 🟢 핵심: 검색 버튼 요소 가져오기
    const searchButton = document.querySelector('#search-form button[type="submit"]');

    // 🟢 1. 초기 버튼 상태 설정 (DOM 로드 시 바로 비활성화)
    searchButton.disabled = true;

    // 🔹 1. 현재 위치 자동 적용
    getCurrentLocationBtn.addEventListener('click', getCurrentLocation);

    // 🟢 핵심: 2. location-input 값 변경 이벤트 감지 (타이핑이 시작되면 버튼 비활성화)
    locationInput.addEventListener('input', () => {
        // 타이핑을 시작하면, 이전에 자동 완성으로 선택했던 상태를 초기화합니다.
        if (isLocationSelected) {
            isLocationSelected = false;
            searchButton.disabled = true;
            // 좌표 값도 초기화할 수 있지만, 일반적으로는 유지해도 무방합니다.
            // latInput.value = '';
            // lngInput.value = '';
        }
        
        const query = locationInput.value.trim();
        if (query.length > 1) {
             // 2글자 이상일 때만 검색 시작 (디바운스된 함수 호출)
            debouncedAutocomplete(query);
        } else {
            // 입력이 짧거나 없을 경우 목록을 비웁니다.
            autocompleteList.innerHTML = '';
            autocompleteList.style.display = 'none';
        }
    });

    // 🟢 핵심: 3. location-input 키다운 이벤트 감지 (키보드 내비게이션)
    locationInput.addEventListener('keydown', (e) => {
        const activeClass = 'autocomplete-active';
        const items = autocompleteList.querySelectorAll('li');
        let currentFocus = -1; // 포커스된 항목의 인덱스 (-1은 포커스된 항목 없음)

        // 이미 포커스된 항목이 있는지 확인
        for (let i = 0; i < items.length; i++) {
            if (items[i].classList.contains(activeClass)) {
                currentFocus = i;
                break;
            }
        }

        if (e.key === "ArrowDown") {
            e.preventDefault(); // 커서 이동 방지
            currentFocus = currentFocus < items.length - 1 ? currentFocus + 1 : 0;
            addActive(items, currentFocus, activeClass);
        } else if (e.key === "ArrowUp") {
            e.preventDefault(); // 커서 이동 방지
            currentFocus = currentFocus > 0 ? currentFocus - 1 : items.length - 1;
            addActive(items, currentFocus, activeClass);
        } else if (e.key === "Enter") {
            if (autocompleteList.style.display === 'block' && currentFocus > -1) {
                e.preventDefault(); // 폼 제출 방지
                // 포커스된 항목이 있다면 클릭 이벤트 강제 실행
                items[currentFocus].click(); 
            }
            // 포커스된 항목이 없으면, locationInput의 input 이벤트 로직에 따라 버튼 비활성화 상태로 폼 제출이 방지됨.
            // 유효한 항목을 선택하면 isLocationSelected가 true가 되어 폼이 제출됨.
        }
    });

    // 포커스 관리 헬퍼 함수
    function addActive(items, index, activeClass) {
        // 모든 항목에서 활성 클래스 제거
        removeActive(items, activeClass);
        if (index >= items.length) index = 0;
        if (index < 0) index = items.length - 1;
        
        // 새 항목에 활성 클래스 추가
        items[index].classList.add(activeClass);
        
        // (선택 사항) 목록이 길 경우 포커스된 항목이 보이도록 스크롤
        items[index].scrollIntoView({ block: "nearest" });
    }

    function removeActive(items, activeClass) {
        for (let i = 0; i < items.length; i++) {
            items[i].classList.remove(activeClass);
        }
    }
    
    // 🔹 3. 지도 모달 초기화
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

    // 🔹 4. 지도 초기화
    function initMap() {
        const container = document.getElementById('modal-map');
        const defaultCenter = new kakao.maps.LatLng(37.5665, 126.9780);

        map = new kakao.maps.Map(container, { center: defaultCenter, level: 8 });
        map.addControl(new kakao.maps.ZoomControl(), kakao.maps.ControlPosition.RIGHT);

        const geocoder = new kakao.maps.services.Geocoder();

        kakao.maps.event.addListener(map, 'click', (mouseEvent) => {
            const lat = mouseEvent.latLng.getLat();
            const lng = mouseEvent.latLng.getLng();
            
            // 🟢 핵심: 지도에서 위치 선택 시 setLocation을 통해 isLocationSelected를 true로 설정
            setLocation(lat, lng); 

            geocoder.coord2Address(lng, lat, (result, status) => {
                if (status === kakao.maps.services.Status.OK && result[0]) {
                    locationInput.value = result[0].address.address_name;
                    // 🟢 핵심: 주소 변환 성공 후 버튼 활성화
                    isLocationSelected = true;
                    searchButton.disabled = false;
                    
                    const modal = bootstrap.Modal.getInstance(mapModalEl);
                    modal.hide();
                }
            });
        });
    }

    // 🔹 5. 마커와 입력 필드 업데이트
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
        
        // 🟢 핵심: 좌표가 설정되면, 일단 버튼을 활성화 (주소 변환이 실패해도 좌표 검색 가능하도록)
        isLocationSelected = true;
        searchButton.disabled = false;
    }

    // 🔹 6. 현재 위치 가져오기
    function getCurrentLocation() {
        if (navigator.geolocation) {
             // 🟢 핵심: 현재 위치 가져오는 동안 사용자에게 알림
            searchButton.disabled = true; 
            locationInput.placeholder = "현재 위치 찾는 중...";
            
            navigator.geolocation.getCurrentPosition(
                (pos) => {
                    const lat = pos.coords.latitude;
                    const lng = pos.coords.longitude;
                    performGeolocationAction(lat, lng);
                    // 🟢 핵심: 성공 시 isLocationSelected는 performGeolocationAction에서 true로 설정됩니다.
                },
                (err) => {
                    alert("위치 정보를 가져올 수 없습니다.");
                    console.error(err);
                    // 🟢 실패 시 버튼 비활성화 상태 유지 및 플레이스홀더 복구
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

    // 🔹 7. 좌표 → 주소 변환 + 입력 필드/마커 업데이트
    function performGeolocationAction(lat, lng) {
        setLocation(lat, lng); // setLocation에서 isLocationSelected=true, button.disabled=false 설정됨
        locationInput.placeholder = "지역 검색"; // 플레이스홀더 복구

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
                alert("주소 변환에 실패했지만, 좌표는 저장되었습니다. 검색이 가능합니다.");
            }
            // 🟢 핵심: 현재 위치 가져오기 성공 시 무조건 버튼 활성화
            isLocationSelected = true;
            searchButton.disabled = false;
        });
    }

    // 🔹 8. 디바운스 함수 (기존과 동일)
    function debounce(func, delay){
        let timeoutId;
        return function(...args){
            if (timeoutId){
                clearTimeout(timeoutId);
            }
            timeoutId = setTimeout(() => {
               func.apply(this, args); 
            }, delay);
        }
    }

    // 🔹 9. 디바운스된 자동완성 검색 함수 (기존과 동일)
    const debouncedAutocomplete = debounce(searchAutocomplete, 300);

    // keyup 이벤트 리스너는 input 이벤트 리스너로 대체되었으므로 삭제 (locationInput.addEventListener('input', ...))

    // 🔹 10. 서버에 자동완성 검색 요청 (기존과 동일)
    function searchAutocomplete(query) {
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
                autocompleteList.innerHTML = '';
                autocompleteList.style.display = 'none';
            });
    }

    // 🔹 11. 검색 결과를 화면에 표시 (기존과 동일)
    function displayAutocompleteResults(results) {
        // ... (이전 코드와 동일) ...
        autocompleteList.innerHTML = ''; 

        if (results.length === 0) {
            autocompleteList.style.display = 'none';
            return;
        }

        results.forEach(item => {
            const listItem = document.createElement('li');
            listItem.classList.add('list-group-item', 'list-group-item-action');
            listItem.textContent = item.full_address; 

            // 클릭 이벤트 리스너 추가
            listItem.addEventListener('click', () => {
                selectAutocompleteItem(item);
            });

            autocompleteList.appendChild(listItem);
        });

        autocompleteList.style.display = 'block'; 
    }

    // 🟢 핵심: 12. 자동완성 항목 선택 시 처리 (버튼 활성화 로직 추가)
    function selectAutocompleteItem(item) {
        // 폼 입력 필드 업데이트
        locationInput.value = item.full_address;
        latInput.value = item.lat;
        lngInput.value = item.lng;

        // 자동완성 목록 숨기기
        autocompleteList.innerHTML = '';
        autocompleteList.style.display = 'none';

        // 🟢 핵심: 유효한 항목을 선택했으므로 상태 변경 및 버튼 활성화!
        isLocationSelected = true;
        searchButton.disabled = false;
        
        // (선택 사항) 바로 검색을 원하면 아래 주석을 해제하세요.
        // document.getElementById('search-form').submit();
    }
    
    // 🟢 핵심: 13. 폼 제출 방지 (isLocationSelected가 false일 때)
    const searchForm = document.getElementById('search-form');
    searchForm.addEventListener('submit', (e) => {
        if (!isLocationSelected) {
            e.preventDefault(); // 폼 제출을 막음
            alert("자동 완성 목록, 지도 또는 현재 위치를 통해 유효한 위치를 선택해야 검색할 수 있습니다.");
            searchButton.disabled = true; // 만약을 대비해 다시 비활성화
        }
        // isLocationSelected가 true이면 폼 제출은 정상적으로 진행됩니다.
    });

});