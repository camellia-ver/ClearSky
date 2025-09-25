// metal-measuring.js 파일 내부 전체 코드

// ------------------- 데이터 준비 -------------------

// 1. 모든 지역 이름(Labels) 추출
const stationNames = METAL_STATION_DATA.map(station => station.stationName);

// 2. 모든 고유한 금속 이름 (Dataset Keys) 추출
const allMetalNames = METAL_STATION_DATA
    .flatMap(station => Object.keys(station.itemData));
const uniqueMetalNames = [...new Set(allMetalNames)];

// 색상 배열 (각 금속 데이터셋에 할당할 고유한 색상)
const COLORS = [
    '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40', '#E7E9ED'
];


// 3. Chart.js Datasets 구조 생성
const chartDatasets = uniqueMetalNames.map((metalName, index) => {

    // 해당 금속에 대한 모든 지역의 농도 값 추출
    const dataValues = METAL_STATION_DATA.map(station => {
        // 값이 문자열이거나 없을 수 있으므로 처리
        const value = station.itemData[metalName];
        // 값이 '-'인 경우 null로 처리하여 차트에 표시되지 않도록 합니다.
        return value && value !== '-' ? parseFloat(value) : null;
    });

    const color = COLORS[index % COLORS.length];

    return {
        label: metalName, // "납", "니켈" 등의 금속 이름
        data: dataValues, // [1.23, 0.5, 2.1, ...]
        backgroundColor: color,
        borderColor: color,
        borderWidth: 1,
        // Chart.js에서 막대를 그룹화할 때 사용되는 추가 옵션 (선택적)
        // categoryPercentage: 0.8,
        // barPercentage: 0.9
    };
});


// ------------------- 차트 렌더링 -------------------
window.onload = function () {
    // HTML에서 캔버스 요소 가져오기
    const ctx = document.getElementById('metalChart').getContext('2d');

    // Chart.js 인스턴스 생성
    const metalChart = new Chart(ctx, {
        type: 'bar', // 그룹형 막대 차트
        data: {
            labels: stationNames, // X축 레이블 (지역 이름)
            datasets: chartDatasets // 위에서 준비한 금속별 데이터셋
        },
        options: {
            responsive: true,
            scales: {
                x: {
                    // x축 막대를 그룹화하는 옵션 (선택적)
                    // stacked: false,
                    title: {
                        display: true,
                        text: '측정 지역'
                    }
                },
                y: {
                    type: 'logarithmic',
                    title: {
                        display: true,
                        text: '농도 (µg/m³) (로그 스케일)'
                    },
                    min: 0.001
                }
            },
            plugins: {
                legend: {
                    display: true,
                    position: 'top',
                },
                title: {
                    display: true,
                    text: '지역별 대기 중 중금속 농도 비교'
                }
            }
        }
    });
};