package org.example.fileprocessingservice.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileProcessingService {

    public List<String> processFileFromStream(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            List<String> lines = reader.lines()
                    .filter(line -> line.contains("NStr"))
                    .collect(Collectors.toList());

            if (lines.isEmpty()) {
                throw new RuntimeException("Файл не содержит строк с NStr.");
            }

            return lines;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при обработке файла: " + e.getMessage(), e);
        }
    }
}