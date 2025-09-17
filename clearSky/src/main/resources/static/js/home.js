document.addEventListener("DOMContentLoaded", () => {
    const mapButton = document.getElementById('show-map');
    const searchBox = document.querySelector('.search-box');

    mapButton.addEventListener('click', () => {
        // 검색 박스 슬라이드 업
        searchBox.classList.add('slide-up');

        // 새 창에서 지도 열기
        window.open("/map-modal", "MapWindow", "width=900,height=650,resizable=yes,scrollbars=yes");
    });
});
