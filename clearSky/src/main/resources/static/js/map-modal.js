document.addEventListener("DOMContentLoaded", () => {
        const map = L.map('map').setView([37.5665, 126.9780], 12);

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; OpenStreetMap contributors'
        }).addTo(map);

        map.on('click', function(e) {
            L.marker([e.latlng.lat, e.latlng.lng]).addTo(map)
                .bindPopup(`위도: ${e.latlng.lat.toFixed(4)}, 경도: ${e.latlng.lng.toFixed(4)}`)
                .openPopup();
        });
})