package com.portfolio.clearSky.util;

import com.portfolio.clearSky.model.AdministrativeBoundary;
import com.portfolio.clearSky.repository.AdministrativeBoundaryRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CsvUtils {
    public void writeFailedRecord(CSVRecord record, int lineNumber, String failedCsvPath){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(failedCsvPath, true))){
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

    public void saveBatch(List<AdministrativeBoundary> buffer,
                          int lineNumber,
                          TransactionTemplate transactionTemplate,
                          AdministrativeBoundaryRepository repository,
                          EntityManager em){
        transactionTemplate.execute(status -> {
            try{
                repository.saveAll(buffer);
                em.flush();
                em.clear();
                buffer.clear();
            } catch (Exception e){
                log.error("Failed saving batch ending at line {}", lineNumber, e);
                status.setRollbackOnly();// 배치만 롤백
            }

            return Void.TYPE;
        });
    }
}
