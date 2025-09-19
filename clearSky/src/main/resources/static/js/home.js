let map; // 지도 전역 변수
let mapInitialized = false;

document.addEventListener("DOMContentLoaded", () => {
    const openMapBtn = document.getElementById('openMapBtn');
    if (!openMapBtn) return;

    const mapModalEl = document.getElementById('mapModal');

    // 모달이 완전히 열렸을 때 실행
    mapModalEl.addEventListener('shown.bs.modal', () => {
        if (!mapInitialized) {
            if (typeof kakao === 'undefined') {
                console.error("Kakao Maps SDK가 로드되지 않았습니다. 잠시 후 다시 시도하세요.");
                return;
            }

            kakao.maps.load(() => {
                initMap();
                mapInitialized = true;
            });
        } else {
            // 이미 초기화된 지도면 resize
            kakao.maps.event.trigger(map, 'resize');
        }
    });

    // 버튼 클릭 시 모달 열기
    openMapBtn.addEventListener('click', () => {
        const mapModal = new bootstrap.Modal(mapModalEl);
        mapModal.show();
    });
});

// 지도 초기화 함수
function initMap() {
    const container = document.getElementById('modal-map');
    const defaultCenter = new kakao.maps.LatLng(37.5665, 126.9780);

    map = new kakao.maps.Map(container, {
        center: defaultCenter,
        level: 14
    });

    // 줌 컨트롤 추가
    var zoomControl = new kakao.maps.ZoomControl();
    map.addControl(zoomControl, kakao.maps.ControlPosition.RIGHT);

    const geocoder = new kakao.maps.services.Geocoder();
    let marker = null;

    // 지도 클릭 이벤트
    kakao.maps.event.addListener(map, 'click', function(mouseEvent) {
        const lat = mouseEvent.latLng.getLat();
        const lng = mouseEvent.latLng.getLng();

        if (marker) marker.setMap(null);
        marker = new kakao.maps.Marker({ position: mouseEvent.latLng });
        marker.setMap(map);

        // 좌표 → 주소 변환
        geocoder.coord2Address(lng, lat, function(result, status) {
            if (status === kakao.maps.services.Status.OK) {
                const address = result[0].address;
                document.getElementById('level1-input').value = address.region_1depth_name;
                document.getElementById('level2-input').value = address.region_2depth_name;
                document.getElementById('level3-input').value = address.region_3depth_name;

                // 모달 닫기
                const modal = bootstrap.Modal.getInstance(document.getElementById('mapModal'));
                modal.hide();
            }
        });
    });
}
