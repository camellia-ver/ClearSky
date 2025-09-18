package com.portfolio.clearSky.initializer;

import com.portfolio.clearSky.repository.ForecastCodesRepository;
import com.portfolio.clearSky.service.ForescastCodesLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ForecastCodesInitializer implements CommandLineRunner {
    private final ForecastCodesRepository repository;
    private final ForescastCodesLoader loader;

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() == 0){
            log.info("DB가 비어있습니다. CSV 데이터를 import 합니다...");
            loader.importCsvToDb();
        }else {
            log.info("DB에 이미 데이터가 존재합니다. CSV import 건너뜁니다.");
        }
    }
}
