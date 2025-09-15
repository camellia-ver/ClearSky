package com.portfolio.clearSky.initializer;

import com.portfolio.clearSky.repository.AdministrativeBoundaryRepository;
import com.portfolio.clearSky.service.AdministrativeBoundaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdministrativeBoundaryInitializer implements ApplicationRunner {
    private final AdministrativeBoundaryService administrativeBoundaryService;
    private final AdministrativeBoundaryRepository administrativeBoundaryRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (administrativeBoundaryRepository.count() == 0){
            log.info("DB가 비어있습니다. CSV 데이터를 import 합니다...");
            administrativeBoundaryService.importCsvToDb();
        }else {
            log.info("DB에 이미 데이터가 존재합니다. CSV import 건너뜁니다.");
        }
    }
}
