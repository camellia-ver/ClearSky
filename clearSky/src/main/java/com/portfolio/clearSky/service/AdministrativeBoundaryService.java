package com.portfolio.clearSky.service;

import com.portfolio.clearSky.model.AdministrativeBoundary;
import com.portfolio.clearSky.model.Coordinate;
import com.portfolio.clearSky.repository.AdministrativeBoundaryRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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
    private static final String CSV_PATH = "/data/administrative_boundary.csv";
    private static final String FAILED_CSV_PATH = "/data/failed_rows.csv";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private final TransactionTemplate transactionTemplate;
    private final AdministrativeBoundaryRepository administrativeBoundaryRepository;
    private final EntityManager em;

    @Transactional
    public void importCsvToDb() {
        ClassPathResource resource = new ClassPathResource(CSV_PATH);
        int lineNumber = 0;
        int success = 0;
        int failed = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
        )){
            CSVParser parser = new CSVParser(
                    reader,
                    CSVFormat.DEFAULT.builder()
                            .setHeader("구분", "행정구역코드", "1단계", "2단계", "3단계",
                                    "격자 X", "격자 Y", "경도(시)", "경도(분)", "경도(초)",
                                    "위도(시)", "위도(분)", "위도(초)", "경도(초/100)", "위도(초/100)", "위치업데이트")
                            .setSkipHeaderRecord(true) // 첫 행은 데이터로 읽지 않음
                            .setTrim(true) // 공백 제거
                            .build()
            );

            List<AdministrativeBoundary> buffer = new ArrayList<>(BATCH_SIZE);

            for (CSVRecord record : parser){
                lineNumber++;
                try {
                    AdministrativeBoundary ab = AdministrativeBoundary.builder()
                            .category(record.get("구분"))
                            .admCode(record.get("행정구역코드"))
                            .admLevel1(record.get("1단계"))
                            .admLevel2(record.get("2단계"))
                            .admLevel3(record.get("3단계"))
                            .gridX(parseIntegerOrNull(record.get("격자 X")))
                            .gridY(parseIntegerOrNull(record.get("격자 Y")))
                            .longitude(Coordinate.builder()
                                    .deg(parseIntegerOrNull(record.get("경도(시)")))
                                    .min(parseIntegerOrNull(record.get("경도(분)")))
                                    .sec(parseBigDecimalOrNull(record.get("경도(초)")))
                                    .sec100(parseBigDecimalOrNull(record.get("경도(초/100)")))
                                    .build())
                            .latitude(Coordinate.builder()
                                    .deg(parseIntegerOrNull(record.get("위도(시)")))
                                    .min(parseIntegerOrNull(record.get("위도(분)")))
                                    .sec(parseBigDecimalOrNull(record.get("위도(초)")))
                                    .sec100(parseBigDecimalOrNull(record.get("위도(초/100)")))
                                    .build())
                            .locUpdate(parseLocalDateOrNull(record.get("위치업데이트")))
                            .build();

                    buffer.add(ab);
                    success++;
                }catch (Exception e){
                    log.warn("Failed to parse/build entity at record {}: {}", lineNumber, e.getMessage(), e);
                    failed++;
                    writeFailedRecord(record, lineNumber);
                    continue;
                }

                if (buffer.size() >= BATCH_SIZE) {
                    saveBatch(buffer, lineNumber);
                }
            }

            if (!buffer.isEmpty()) {
                saveBatch(buffer, lineNumber);
            }

            log.info("Import finished. lines={} success={} failed={}", lineNumber, success, failed);

        } catch (Exception e){
            log.error("Failed to import CSV", e);
            throw new RuntimeException(e);
        }
    }

    private void saveBatch(List<AdministrativeBoundary> buffer, int lineNumber){
        transactionTemplate.execute(status -> {
            try{
                administrativeBoundaryRepository.saveAll(buffer);
                em.flush();
                em.clear();
                buffer.clear();
            } catch (Exception e){
                log.error("Failed saving batch ending at line {}", lineNumber, e);
                status.setRollbackOnly();// 배치만 롤백
            }
            return null;
        });
    }

    private void writeFailedRecord(CSVRecord record, int lineNumber){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FAILED_CSV_PATH, true))){
            if (lineNumber == 1){
                // 첫 번째 실패라면 헤더 기록
                writer.write(String.join(",", record.getParser().getHeaderNames()));
                writer.newLine();
            }

            List<String> values = new ArrayList<>();
            record.forEach(values::add);
            writer.write(String.join(",", values));
            writer.newLine();
        } catch (IOException e){
            log.error("Failed to write failed record at line {}", lineNumber, e);
        }
    }

    private static String trimToNull(String s){
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private Integer parseIntegerOrNull(String s){
        s = trimToNull(s);

        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e){
            log.warn("parseInteger failed for value '{}'", s, e);
            return null;
        }
    }

    private BigDecimal parseBigDecimalOrNull(String s){
        s = trimToNull(s);

        try{
            return new BigDecimal(s);
        }catch (NumberFormatException e){
            log.warn("parseBigDecimal failed for value '{}'", s, e);
            return null;
        }
    }

    private LocalDate parseLocalDateOrNull(String s){
        s = trimToNull(s);
        if (s == null) return null;

        try {
            return LocalDate.parse(s,DATE_TIME_FORMATTER);
        }catch (DateTimeParseException e){
            log.error("parseLocalDate failed for value '{}'", s, e);
            return null;
        }
    }
}
