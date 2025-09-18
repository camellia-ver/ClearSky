document.addEventListener("DOMContentLoaded", () => {
    let mapInitialized = false;
    let map;

    const mapModal = document.getElementById('mapModal');

    mapModal.addEventListener('shown.bs.modal', () => {
        if (!mapInitialized) {
            map = L.map('modal-map').setView([37.5665, 126.9780], 12);

            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '&copy; OpenStreetMap contributors'
            }).addTo(map);

            map.on('click', function(e) {
                const lat = e.latlng.lat;
                const lng = e.latlng.lng;

                L.marker([lat, lng]).addTo(map)
                    .bindPopup(`위도: ${lat.toFixed(4)}, 경도: ${lng.toFixed(4)}`)
                    .openPopup();

                // Nominatim API 역지오코딩
                fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json&accept-language=ko`)
                    .then(res => res.json())
                    .then(data => {
                        const address = data.display_name;
                        document.getElementById('search-input').value = address;

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
});
