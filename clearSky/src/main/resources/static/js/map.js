document.addEventListener("DOMContentLoaded", () => {
    kakao.maps.load(function() {
        var mapContainer = document.getElementById('map');

        // 지도 초기 옵션
        var mapOption = {
            center: new kakao.maps.LatLng(36.5, 127.5),
            level: 14
        };

        var map = new kakao.maps.Map(mapContainer, mapOption);

        // 줌 컨트롤 추가
        var zoomControl = new kakao.maps.ZoomControl();
        map.addControl(zoomControl, kakao.maps.ControlPosition.RIGHT);

    });
});
