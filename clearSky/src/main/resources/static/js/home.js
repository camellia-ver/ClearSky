    document.addEventListener("DOMContentLoaded", () => {
        const openMapBtn = document.getElementById('openMapBtn');
        if (!openMapBtn) return;

        openMapBtn.addEventListener('click', () => {
            window.open(
                '/map-popup',      // 팝업 페이지 URL
                'mapWindow',
                'width=800,height=600,resizable=yes'
            );
        });
    });