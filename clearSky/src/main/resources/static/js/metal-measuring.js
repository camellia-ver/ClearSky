// ------------------- 기준치 및 건강 영향 -------------------
const THRESHOLDS = {
    "납": 0.5, "니켈": 0.02, "망간": 0.15,
    "아연": 5.0, "칼슘": 20.0, "칼륨": 10.0, "황": 2.0
};

const HEALTH_IMPACT = {
    "납": "신경계, 혈액, 신장 기능에 영향 가능",
    "니켈": "피부 알레르기, 호흡기 자극 가능",
    "망간": "신경계 이상, 운동 기능 저하 가능",
    "아연": "고농도 노출 시 위장 장애 발생 가능",
    "칼슘": "과다 노출 시 신장 결석 위험",
    "칼륨": "심장 및 신장 기능에 영향 가능",
    "황": "호흡기 자극, 천식 악화 가능"
};

function getRiskLevel(metal, value) {
    const threshold = THRESHOLDS[metal];
    if (!threshold || isNaN(value)) return "정보 없음";
    if (value > threshold) return "❌ 초과";
    if (value > threshold * 0.7) return "⚠ 주의";
    return "✅ 적정";
}

// ------------------- 데이터 준비 -------------------
const stationNames = METAL_STATION_DATA.map(station => station.stationName);
const allMetalNames = METAL_STATION_DATA.flatMap(station => Object.keys(station.itemData));
const uniqueMetalNames = [...new Set(allMetalNames)];

const COLORS = [
    '#FFB3BA', '#BAE1FF', '#FFFFBA', '#BAFFC9',
    '#E2BAFF', '#FFD6A5', '#C9F0FF'
];

const chartDatasets = uniqueMetalNames.map((metalName, index) => {
    const dataValues = METAL_STATION_DATA.map(station => {
        const value = station.itemData[metalName];
        return value && value !== '-' ? parseFloat(value) : null;
    });
    const color = COLORS[index % COLORS.length];
    return {
        label: metalName,
        data: dataValues,
        backgroundColor: color,
        borderColor: color,
        borderWidth: 1,
    };
});

// 금속별 색상 (히트맵)
const METAL_COLORS = {
    "납": '255, 99, 132',      "니켈": '54, 162, 235',  "망간": '255, 206, 86',
    "아연": '75, 192, 192',     "칼슘": '153, 102, 255', "칼륨": '255, 159, 64',
    "황": '199, 199, 199'
};

// ------------------- 페이지 로드 시 렌더링 -------------------
window.onload = function () {
    // 1. 막대 차트
    const ctxBar = document.getElementById('metalChart').getContext('2d');
    new Chart(ctxBar, {
        type: 'bar',
        data: { labels: stationNames, datasets: chartDatasets },
        options: {
            responsive: true,
            scales: {
                x: { title: { display: true, text: '측정 지역' } },
                y: { type: 'logarithmic', min: 0.001, title: { display: true, text: '농도 (µg/m³) (로그 스케일)' } }
            },
            plugins: {
                legend: { display: true, position: 'top' },
                title: { display: true, text: '지역별 대기 중 중금속 농도 비교' },
                tooltip: {
                    callbacks: {
                        label: function(ctx) {
                            const metal = ctx.dataset.label;
                            const value = ctx.raw;
                            const risk = getRiskLevel(metal, value);
                            const threshold = THRESHOLDS[metal] ? `기준치 ${THRESHOLDS[metal]} µg/m³` : '기준치 없음';
                            const impact = HEALTH_IMPACT[metal] || '건강 영향 정보 없음';
                            return `${metal}: ${value} µg/m³ (${risk}, ${threshold}) - ${impact}`;
                        }
                    }
                }
            }
        }
    });

    // 2. 요약 카드
    const summaryBox = document.getElementById("summaryBox");
    if (summaryBox) {
        uniqueMetalNames.forEach(metal => {
            const values = METAL_STATION_DATA.map(st => parseFloat(st.itemData[metal] || NaN));
            const validValues = values.filter(v => !isNaN(v));
            if (!validValues.length) return;

            const maxValue = Math.max(...validValues);
            const minValue = Math.min(...validValues);
            const maxRegion = stationNames[values.indexOf(maxValue)];
            const minRegion = stationNames[values.indexOf(minValue)];
            const riskMax = getRiskLevel(metal, maxValue);
            const riskMin = getRiskLevel(metal, minValue);
            const threshold = THRESHOLDS[metal] || '정보 없음';
            const healthImpact = HEALTH_IMPACT[metal] || '건강 영향 정보 없음';

            const card = document.createElement("div");
            card.className = "col";
            card.innerHTML = `
                <div class="card h-100 shadow-sm border">
                    <div class="card-body">
                        <h6 class="card-title fw-bold">${metal}</h6>
                        <p class="card-text mb-1"><span class="text-muted">기준치:</span> <strong>${threshold}</strong> µg/m³</p>
                        <p class="card-text mb-1">최고: <strong>${maxRegion}</strong> (${maxValue} µg/m³) ${riskMax}</p>
                        <p class="card-text mb-1">최저: <strong>${minRegion}</strong> (${minValue} µg/m³) ${riskMin}</p>
                        <p class="card-text text-truncate"><span class="fw-semibold">건강 영향:</span> ${healthImpact}</p>
                    </div>
                </div>`;
            summaryBox.appendChild(card);
        });
    }

    // 3. 히트맵
    const ctxHeat = document.getElementById('metalHeatmap').getContext('2d');
    new Chart(ctxHeat, {
        type: 'matrix',
        data: {
            datasets: [{
                label: '중금속 농도 히트맵',
                data: METAL_STATION_DATA.flatMap(station =>
                    Object.entries(station.itemData).map(([metal, value]) => ({
                        x: metal,
                        y: station.stationName,
                        v: parseFloat(value) || 0
                    }))
                ),
                width: ({chart}) => chart.chartArea ? (chart.chartArea.width / uniqueMetalNames.length - 2) : 0,
                height: ({chart}) => chart.chartArea ? (chart.chartArea.height / stationNames.length - 2) : 0,
                backgroundColor: ctx => {
                    const cell = ctx.dataset.data[ctx.dataIndex];
                    const color = METAL_COLORS[cell.x] || '0,0,0';
                    const max = 10;
                    const intensity = Math.min(cell.v / max, 1);
                    return `rgba(${color}, ${intensity})`;
                }
            }]
        },
        options: {
            layout: {
                padding: { top: 20, right: 20, bottom: 50, left: 60 }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(ctx) {
                            const cell = ctx.dataset.data[ctx.dataIndex];
                            const risk = getRiskLevel(cell.x, cell.v);
                            const impact = HEALTH_IMPACT[cell.x] || '건강 영향 정보 없음';
                            return `${cell.y} - ${cell.x}: ${cell.v} µg/m³ (${risk}) - ${impact}`;
                        }
                    }
                },
                legend: { display: true, position: 'top' }
            },
            scales: {
                x: { type: 'category', labels: uniqueMetalNames, offset: true },
                y: { type: 'category', labels: stationNames, offset: true }
            }
        }
    });
};
