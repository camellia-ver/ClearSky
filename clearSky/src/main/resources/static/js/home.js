document.addEventListener("DOMContentLoaded", () => {
    let mapInitialized = false;
    let map;

    const mapModal = document.getElementById('mapModal');

    // 모달 열릴 때 이벤트
    mapModal.addEventListener('shown.bs.modal', () => {
        // 모달 열리면 inert 제거 (포커스 허용)
        mapModal.removeAttribute('inert');

        if (!mapInitialized) {
            // Leaflet 지도 초기화
            map = L.map('modal-map', {
                keyboard: false, // 키보드 포커스 방지
                tap: false       // 모바일 터치 충돌 방지
            }).setView([37.5665, 126.9780], 12);

            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '&copy; OpenStreetMap contributors'
            }).addTo(map);

            // 지도 클릭 이벤트
            map.on('click', function(e) {
                const lat = e.latlng.lat;
                const lng = e.latlng.lng;

                L.marker([lat, lng]).addTo(map)
                    .bindPopup(`위도: ${lat.toFixed(4)}, 경도: ${lng.toFixed(4)}`)
                    .openPopup();

                // Nominatim 역지오코딩
                fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json&accept-language=ko`)
                    .then(res => res.json())
                    .then(data => {
                        const address = data.address || {};

                        // level1: 시/도
                        const level1 = address.state || address.region || '';

                        // level2: 구/군
                        let level2 = '';
                        if (address.county) {
                            level2 = address.county;
                        } else if (address.city && address.city !== level1) {
                            level2 = address.city;
                        }

                        // level3: 동
                        const level3 = address.suburb || address.city_district || address.town || address.village || '';

                        // 입력창 채우기
                        document.getElementById('level1-input').value = level1;
                        document.getElementById('level2-input').value = level2;
                        document.getElementById('level3-input').value = level3;

                        // 모달 닫기
                        const bootstrapModal = bootstrap.Modal.getInstance(mapModal);
                        bootstrapModal.hide();
                    })
                    .catch(err => {
                        console.error(err);
                        alert("주소를 가져오는 중 오류가 발생했습니다.");
                    });
            });

            mapInitialized = true;
        }
    });

    // 모달 닫힐 때 inert 추가 (포커스 차단)
    mapModal.addEventListener('hidden.bs.modal', () => {
        mapModal.setAttribute('inert', '');
    });
});
