package com.portfolio.clearSky.initializer;

import com.portfolio.clearSky.service.AdministrativeBoundaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdministrativeBoundaryInitializer implements ApplicationRunner {
    private final AdministrativeBoundaryService administrativeBoundaryService;

    @Override
    public void run(ApplicationArguments args) throws Exception {

    }
}
