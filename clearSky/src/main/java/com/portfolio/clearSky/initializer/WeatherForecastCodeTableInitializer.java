package com.portfolio.clearSky.initializer;

import com.portfolio.clearSky.model.ForecastCodes;
import com.portfolio.clearSky.model.PtyCode;
import com.portfolio.clearSky.model.SkyCode;
import com.portfolio.clearSky.model.emuns.PtyCodeValue;
import com.portfolio.clearSky.model.emuns.SkyCodeValue;
import com.portfolio.clearSky.repository.ForecastCodesRepository;
import com.portfolio.clearSky.repository.PtyCodeRepository;
import com.portfolio.clearSky.repository.SkyCodeRepository;
import com.portfolio.clearSky.service.ForescastCodesLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherForecastCodeTableInitializer implements CommandLineRunner {
    private final SkyCodeRepository skyCodeRepository;
    private final PtyCodeRepository ptyCodeRepository;
    private final ForecastCodesRepository forecastCodesRepository;
    private final ForescastCodesLoader forescastCodesLoader;

    @Override
    public void run(String... args) throws Exception {
        initializeForecastCodes();
        initializeSkyCodes();
        initializePtyCodes();
    }

    private void initializeForecastCodes(){
        if (forecastCodesRepository.count() == 0){
            log.info("DB가 비어있습니다. CSV 데이터를 import 합니다...");
            forescastCodesLoader.importCsvToDb();
        }else {
            log.info("DB에 이미 데이터가 존재합니다. CSV import 건너뜁니다.");
        }
    }

    private void initializeSkyCodes(){
        if (skyCodeRepository.count() > 0) {
            log.info("SKY Code DB에 이미 데이터가 존재합니다. Insert 건너뜁니다.");
            return;
        }

        log.info("SKY Code DB가 비어있습니다. 데이터를 Insert 합니다...");

        List<SkyCode> skyCodes = Arrays.stream(SkyCodeValue.values())
                .map(e -> SkyCode.builder()
                        .codeValue(e.getCodeValue())
                        .codeName(e.getCodeName())
                        .build())
                .toList();

        skyCodeRepository.saveAll(skyCodes);
        log.info("SKY Code 초기화 완료: {}건 저장됨", skyCodes.size());
    }

    private void initializePtyCodes() {
        if (ptyCodeRepository.count() > 0) {
            log.info("PTY Code DB에 이미 데이터가 존재합니다. Insert 건너뜁니다.");
            return;
        }

        log.info("PTY Code DB가 비어있습니다. 데이터를 Insert 합니다...");

        List<PtyCode> ptyCodes = Arrays.stream(PtyCodeValue.values())
                .map(e -> PtyCode.builder()
                        .type(e.getType())
                        .codeValue(e.getCodeValue())
                        .codeName(e.getCodeName())
                        .build())
                .toList();

        ptyCodeRepository.saveAll(ptyCodes);
        log.info("PTY Code 초기화 완료: {}건 저장됨", ptyCodes.size());
    }
}
