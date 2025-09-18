package com.portfolio.clearSky.service;

import com.portfolio.clearSky.model.ForecastCodes;
import com.portfolio.clearSky.repository.ForecastCodesRepository;
import com.portfolio.clearSky.util.CsvUtils;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForescastCodesLoader {
    private static final int BATCH_SIZE = 1000;
    private static final String CSV_PATH = "/data/forecast_codes.csv";
    private static final String FAILED_CSV_PATH = "/data/failed_forecast_codes.csv";

    private final TransactionTemplate transactionTemplate;
    private final ForecastCodesRepository repository;
    private final EntityManager em;

    @Transactional
    public void importCsvToDb(){
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
                            .setHeader("예보구분","항목값","항목명","단위")
                            .setSkipHeaderRecord(true)
                            .setTrim(true)
                            .build()
            );

            List<ForecastCodes> buffer = new ArrayList<>(BATCH_SIZE);
            CsvUtils csvUtils = new CsvUtils();

            for (CSVRecord record : parser){
                lineNumber++;
                try{
                    ForecastCodes fc = ForecastCodes.builder()
                            .forecast_type(record.get("예보구분"))
                            .code_value(record.get("항목값"))
                            .code_name(record.get("항목명"))
                            .unit(record.get("단위"))
                            .build();

                    buffer.add(fc);
                    success++;
                } catch (Exception e){
                    log.warn("Failed to parse/build entity at record {}: {}", lineNumber, e.getMessage(), e);
                    failed++;
                    csvUtils.writeFailedRecord(record, lineNumber,FAILED_CSV_PATH);
                    continue;
                }

                if (buffer.size() >= BATCH_SIZE){
                    csvUtils.saveBatch(buffer, lineNumber, transactionTemplate, repository, em);
                }
            }

            if (!buffer.isEmpty()){
                csvUtils.saveBatch(buffer, lineNumber, transactionTemplate, repository, em);
            }

            log.info("Import finished. lines={} success={} failed={}", lineNumber, success, failed);
        } catch (Exception e){
            log.error("Failed to import CSV", e);
            throw new RuntimeException(e);
        }
    }
}
