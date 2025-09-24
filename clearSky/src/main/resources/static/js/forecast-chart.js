// forecast-chart.js

/**
 * 'yyyyMMdd' 형식의 날짜 문자열을 받아 요일 문자열(예: '화')을 반환합니다.
 * @param {string} dateString - '20250923' 형태의 날짜 문자열
 * @returns {string} 요일 문자열 (예: '월', '화', '수')
 */
function getDayOfWeek(dateString) {
    // 1. 문자열에서 숫자만 추출합니다. (예: "2025년 9월 23일" -> "2025923")
    const numbersOnly = dateString.replace(/[^0-9]/g, '');

    // 2. 연, 월, 일로 분리합니다.
    const year = numbersOnly.substring(0, 4);
    let month = numbersOnly.substring(4, numbersOnly.length - 2); // 월 (길이가 1일 수도 있음)
    let day = numbersOnly.substring(numbersOnly.length - 2);       // 일 (길이가 1일 수도 있음)

    // 3. 월과 일의 자릿수를 2자리로 강제 보정
    if (month.length === 1) {
        month = '0' + month; // 예: '9' -> '09'
    }
    if (day.length === 1) {
        day = '0' + day;   // 예: '3' -> '03'
    }

    // 4. 'yyyy/MM/dd' 형식으로 Date 객체를 생성
    const date = new Date(`${year}/${month}/${day}`);

    // 5. 요일 배열 정의 및 반환
    const days = ['일', '월', '화', '수', '목', '금', '토'];

    if (isNaN(date.getTime())) { // getTime()을 사용하여 Invalid Date 체크를 더 명확히
        console.error("Invalid Date Object after parsing:", dateString);
        return "";
   }

    return days[date.getDay()];
}

/**
 * WeatherDisplayDto 리스트를 받아 Chart.js 데이터 형식으로 변환하고 그래프를 그립니다.
 * @param {Array<Object>} rawData - 컨트롤러에서 전달받은 예보 데이터 리스트 (forecastDataList)
 * @param {string} canvasId - 그래프를 그릴 캔버스 요소의 ID (예: 'forecastChart')
 */
function initializeForecastChart(rawData, canvasId) {
    // 1. 데이터 처리 함수
    function processWeatherDataForDualAxis(data) {
        const labels = [];
        const datasets = {};

        // 사용할 카테고리 정의 및 Y축 ID 지정 (T1H, RN1, POP만 사용)
        const categoryMap = {
            'T1H': {
                label: '기온 (°C)',
                yAxisID: 'temp-axis',
                color: '#ff6384',
                type: 'line'
            },
            'RN1': {
                label: '1시간 강수량 (mm)',
                yAxisID: 'rain-axis',
                color: '#36a2eb',
                type: 'bar'
            }
        };

        // 데이터 리스트를 순회하며 분류
        data.forEach(item => {
            try {
                const categoryCode = item.category;
                const rawFcstDate = item.fcstDate; // ex) 2025년 9월 23일
                const rawFcstTime = item.fcstTime; // ex) 0000
                const dayOfWeek = getDayOfWeek(rawFcstDate);
                const formattedTime = rawFcstTime.substring(0, 2) + ':00';
                const timeLabel = dayOfWeek + ' ' + formattedTime;

                // 레이블(시간) 중복 없이 추가
                if (!labels.includes(timeLabel)) {
                    labels.push(timeLabel);
                }

                // 원하는 카테고리 데이터만 데이터셋에 추가
                if (categoryMap[categoryCode]) {
                    const config = categoryMap[categoryCode];

                    // 해당 카테고리 데이터셋 초기화
                    if (!datasets[categoryCode]) {
                        datasets[categoryCode] = {
                            label: config.label,
                            data: [],
                            backgroundColor: config.color,
                            borderColor: config.color,
                            yAxisID: config.yAxisID,
                            type: config.type,
                            fill: false,
                            tension: 0.4,
                            borderDash: config.borderDash || []
                        };
                    }

                    // 예측 값 추가 (숫자로 변환)
                    datasets[categoryCode].data.push(parseFloat(item.fcstValue));
                }
            } catch (error) {
                // 파싱 중 오류가 발생하면 건너뜁니다. (안정성 확보)
                console.error("Chart Data Processing Error:", error, "Item:", item);
            }
        });

        // 시간순으로 레이블 정렬 (시간순서가 꼬일 경우를 대비하여)
        labels.sort();

        return {
            labels: labels,
            datasets: Object.values(datasets)
        };
    }

    const chartData = processWeatherDataForDualAxis(rawData);


    // 카테고리별 최대값 계산
    const maxRain = Math.max(
        ...rawData.filter(item => item.category === "RN1").map(item => parseFloat(item.fcstValue) || 0),
        0
    );
    const maxTemp = Math.max(
        ...rawData.filter(item => item.category === "T1H").map(item => parseFloat(item.fcstValue) || 0),
        0
    );

    // 데이터가 없으면 차트를 그리지 않습니다. (선택적)
    if (chartData.labels.length === 0) {
        console.log("No time labels generated. Cannot draw chart.");
        // 사용자에게 '데이터 없음' 메시지를 표시하는 코드를 여기에 추가할 수 있습니다.
        return;
    }

    // 2. Chart.js 인스턴스 생성
    const ctx = document.getElementById(canvasId).getContext('2d');

    new Chart(ctx, {
        type: 'line', // 기본 타입
        data: chartData,
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'top',
                },
                title: {
                    display: true,
                    text: '시간별 기온, 강수확률 및 강수량 예보'
                }
            },
            // 세 가지 데이터 처리를 위한 두 개의 Y축 정의
            scales: {
                x: { // X축 (시간 축)을 명시적으로 정의
                    type: 'category',
                    labels: chartData.labels, // 레이블을 직접 지정
                    ticks: {
                        autoSkip: false, // 레이블이 하나여도 건너뛰지 않도록 강제
                        maxRotation: 0
                    }
                },
                'temp-axis': { // 기온 축 (왼쪽)
                    type: 'linear',
                    position: 'left',
                    title: {
                        display: true,
                        text: '기온 (°C)'
                    },
                    beginAtZero: true,
                    suggestedMax: maxTemp + 2
                },
                'rain-axis': { // 강수량 축 (오른쪽)
                    type: 'linear',
                    position: 'right',
                    title: {
                        display: true,
                        text: '강수량 (mm)'
                    },
                    beginAtZero: true,
                    suggestedMax: maxRain + 2,
                    grid: {
                        drawOnChartArea: false,
                    }
                }
            }
        }
    });
}