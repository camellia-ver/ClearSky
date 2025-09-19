package com.portfolio.clearSky.initializer;

import com.portfolio.clearSky.model.AirEnvLabCodes;
import com.portfolio.clearSky.model.AirEnvLabSearchCodes;
import com.portfolio.clearSky.model.emuns.AirEnvLabCodesValue;
import com.portfolio.clearSky.model.emuns.AirEnvLabSearchCodesValue;
import com.portfolio.clearSky.model.emuns.AirEnvLabTimeCategory;
import com.portfolio.clearSky.repository.AirEnvLabCodesRepository;
import com.portfolio.clearSky.repository.AirEnvLabSearchCodesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AirEnvLabDataInitializer implements CommandLineRunner {
    private final AirEnvLabCodesRepository airEnvLabCodesRepository;
    private final AirEnvLabSearchCodesRepository airEnvLabSearchCodesRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeAirEnvLabCodes();
        initializeAirEnvLabSearchCodes();
    }

    private void initializeAirEnvLabCodes(){
        if (airEnvLabCodesRepository.count() > 0) {
            log.info("AirEnvLabCodes DB에 이미 데이터가 존재합니다. Insert 건너뜁니다.");
            return;
        }

        log.info("AirEnvLabCodes DB가 비어있습니다. 데이터를 Insert 합니다...");

        List<AirEnvLabCodes> airEnvLabCodes = Arrays.stream(AirEnvLabCodesValue.values())
                        .map(e -> AirEnvLabCodes.builder()
                                .labName(e.getKoreanName())
                                .labCode(e.getCode())
                                .build())
                        .toList();

        airEnvLabCodesRepository.saveAll(airEnvLabCodes);

        log.info("AirEnvLabCodes 초기화 완료: {}건 저장됨", airEnvLabCodes.size());
    }

    private void initializeAirEnvLabSearchCodes(){
        if (airEnvLabSearchCodesRepository.count() > 0) {
            log.info("AirEnvLabSearchCodes DB에 이미 데이터가 존재합니다. Insert 건너뜁니다.");
            return;
        }

        log.info("AirEnvLabSearchCodes DB가 비어있습니다. 데이터를 Insert 합니다...");

        List<AirEnvLabSearchCodes> airEnvLabSearchCodes = Arrays.stream(AirEnvLabSearchCodesValue.values())
                .map(e -> AirEnvLabSearchCodes.builder()
                        .searchName(e.getName())
                        .searchCode(e.getCode())
                        .build())
                .toList();

        airEnvLabSearchCodesRepository.saveAll(airEnvLabSearchCodes);

        log.info("AirEnvLabSearchCodes 초기화 완료: {}건 저장됨", airEnvLabSearchCodes.size());
    }
}
