package org.example.fileprocessingservice.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.fileprocessingservice.dto.ParsedResult;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class FileStorageService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Загружает временные результаты из JSON-файла
     *
     * @param filePath путь к JSON-файлу
     * @return список объектов ParsedResult
     */
    public List<ParsedResult> loadTemporaryResults(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new RuntimeException("Файл с временными результатами не найден: " + filePath);
            }

            // Чтение и десериализация данных
            return Arrays.asList(objectMapper.readValue(file, ParsedResult[].class));
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки временных результатов: " + e.getMessage());
        }
    }
}
