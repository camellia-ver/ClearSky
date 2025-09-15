package com.portfolio.clearSky.service;

import com.portfolio.clearSky.model.AdministrativeBoundary;
import com.portfolio.clearSky.model.Coordinate;
import com.portfolio.clearSky.repository.AdministrativeBoundaryRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdministrativeBoundaryService {
    private static final int BATCH_SIZE = 1000;
    private final AdministrativeBoundaryRepository administrativeBoundaryRepository;
    private final EntityManager em;

    @Transactional
    public void importCsvToDb() {
        try {
            ClassPathResource resource = new ClassPathResource("/data/administrative_boundary.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));

            List<AdministrativeBoundary> buffer = new ArrayList<>(BATCH_SIZE);

            String line;
            boolean firstLine = true; // 헤더 건너뛰기
            while ((line = reader.readLine()) != null){
                if (firstLine){
                    firstLine = false;
                    continue;
                }

                String[] fields = line.split(",",-1);

                buffer.add(AdministrativeBoundary.builder()
                        .category(fields[0])
                        .admCode(fields[1])
                        .admLevel1(fields[2])
                        .admLevel2(fields[3])
                        .admLevel3(fields[4])
                        .gridX(parseIntegerOrNull(fields[5]))
                        .gridY(parseIntegerOrNull(fields[6]))
                        .longitude(Coordinate.builder()
                                .deg(parseIntegerOrNull(fields[7]))
                                .min(parseIntegerOrNull(fields[8]))
                                .sec(parseBigDecimalOrNull(fields[9]))
                                .sec100(parseBigDecimalOrNull(fields[13]))
                                .build())
                        .latitude(Coordinate.builder()
                                .deg(parseIntegerOrNull(fields[10]))
                                .min(parseIntegerOrNull(fields[11]))
                                .sec(parseBigDecimalOrNull(fields[12]))
                                .sec100(parseBigDecimalOrNull(fields[14]))
                                .build())
                        .locUpdate(parseLocalDataOrNull(fields[15]))
                        .build()
                );

                if (buffer.size() >= BATCH_SIZE){
                    administrativeBoundaryRepository.saveAll(buffer);
                    em.flush();
                    em.clear();
                    buffer.clear();
                }
            }

            if (!buffer.isEmpty()){
                administrativeBoundaryRepository.saveAll(buffer);
                em.flush();
                em.clear();
                buffer.clear();
            }
        } catch (Exception e){
            log.error(e.getMessage());
        }
    }

    private Integer parseIntegerOrNull(String s){
        if (s == null || s.trim().isEmpty()) return null;

        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e){
            log.error(e.getMessage());
            return null;
        }
    }

    private BigDecimal parseBigDecimalOrNull(String s){
        if (s == null || s.trim().isEmpty()) return null;

        try{
            return new BigDecimal(s);
        }catch (NumberFormatException e){
            log.error(e.getMessage());
            return null;
        }
    }

    private LocalDate parseLocalDataOrNull(String s){
        if (s == null || s.trim().isEmpty()) return null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        try {
            return LocalDate.parse(s,formatter);
        }catch (DateTimeParseException e){
            log.error(e.getMessage());
            return null;
        }
    }
}
