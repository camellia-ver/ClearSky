package com.portfolio.clearSky.model.emuns;

public enum WeatherCategory {
    // 초단기실황
    T1H,   // 기온 (Temperature)
    RN1,   // 1시간 강수량 (1h Rainfall)
    PTY,   // 강수 형태 (Precipitation Type)
    REH,   // 습도 (Relative Humidity)
    UUU,   // 풍속 U 성분 (U-component wind)
    VVV,   // 풍속 V 성분 (V-component wind)
    VEC,   // 풍향 (Wind Direction)
    WSD,   // 풍속 (Wind Speed)

    // 초단기예보
    POP,   // 강수확률 (Precipitation Probability)
    PCP,   // 1시간 강수량 (1h Rainfall Forecast)
    SNO,   // 1시간 신적설 (Snow)
    SKY,   // 하늘 상태 (Sky Condition)
    TMP,   // 기온 (Temperature)
    TMN,   // 일 최저기온 (Min Temp)
    TMX    // 일 최고기온 (Max Temp)
}